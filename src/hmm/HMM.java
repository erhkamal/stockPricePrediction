package hmm;

import be.ac.ulg.montefiore.run.distributions.MultiGaussianDistribution;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.Opdf;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationSequencesWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;
import be.ac.ulg.montefiore.run.distributions.MultiGaussianMixtureDistribution;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianMixture;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.math3.linear.MatrixUtils;

public class HMM{
        
	private static final String FROM1 = "30-04-2009";
	private static final String TO1 = "02-06-2009";
    private static final String FROM2 = "01-06-2009";
    private static final String TO2 = "02-07-2009";
    private static final String FROM3 = "01-07-2009";
    private static final String TO3 = "02-08-2009";
    private static final String FROM4 = "01-08-2009";
    private static final String TO4 ="02-09-2009";

    private int N = 4;//number of states
    private int D = 4;//dimensions for the gaussian distribution
    private int M = 3;//number of mixtures in the gaussian distribution
    private int delta = 3;//delta value in the left right hmm

    private List<Sensex> data1, data2, data3, data4;
    private int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    int LIKELIHOOD_INTERVAL = 60;
    double LIKELIHOOD_TOLERANCE = 0.01;
    String predictionDate = "24-06-2009";
    String yesterdayDate = "21-06-2009";
    String likelihoodDate = "07-09-2009";
    private static final double PREDICTION_RANGE = 0;
    private List <likelihoodData> likelihoodData = new ArrayList <likelihoodData> ();

    public void testHmmBWL() throws Exception{
    	
        initializeData();
        Hmm <ObservationVector> hmm = (Hmm <ObservationVector> ) initializeHmm();
        Hmm <ObservationVector> learnt = learnBWL(hmm);
        computeLikelihoods(learnt);
        predict();
    }

     Date getDate(String date) throws ParseException{
    	 DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
         double open = 0,high = 0,close = 0,low = 0;
         Date tempDate = new Date();
         tempDate = dateFormat.parse(date);
         return tempDate;
    }

    String dateToString(Date date){
    	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    private void computeLikelihoods(Hmm<ObservationVector> learnt) throws ParseException, FileNotFoundException, IOException{
    	Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.setTime(getDate(likelihoodDate));
        likelihoodData llhoodInstance = new likelihoodData();
        for(int i = 0; i < LIKELIHOOD_INTERVAL;){
        	System.out.println("iteration " + i);
            calendarInstance.add(Calendar.DAY_OF_MONTH, -1);
            String dateString = dateToString(calendarInstance.getTime());
            List<ObservationVector> observations = findObservations(dateString, 2);
            if (observations != null && !observations.isEmpty()){
                double likelihood = Math.log10(learnt.probability(observations));
                llhoodInstance.likelihoodValue = likelihood;
                llhoodInstance.date = dateString;
                likelihoodData.add(new likelihoodData(llhoodInstance));
                ++i;
	        }
        }
    }

    Sensex findByDate(Date dateInstance){
    	Sensex sensexInstance;
        for(int i=0; i < data2.size(); i++){
        	if((data2.get(i).getDate()).equals(dateInstance) == true)
                return new Sensex(data2.get(i));
        }
        return null;
    }

    private void predict() throws ParseException{
    	int count = 0;
        Date predDate = getDate(predictionDate);
        double sum = 0.;
        double n = 0.0;
        do{
        	try{
        		Date yesterPredDate = getDate(yesterdayDate);
                Sensex yesterSensex = findByDate(yesterPredDate);
                Sensex realPredSensex = findByDate(predDate);
                Double yesterPredDateLikelihood = findLikelihoodByDate(yesterPredDate);
                List<Sensex> guesses = findByLikelihoodTolerance(yesterPredDate,yesterPredDateLikelihood, LIKELIHOOD_TOLERANCE);
                if (!guesses.isEmpty()){
                    Sensex bestGuess = findBestGuess(guesses);
                    Sensex bestGuessTomorrow = findNextByDate(getTomorrrow(bestGuess.getDate()));
                    Double predictedClose = yesterSensex.getClose() + (bestGuessTomorrow.getClose() - bestGuess.getClose());
                    sum = sum + Math.abs((realPredSensex.getClose() - predictedClose) / realPredSensex.getClose());
                    n++;
                    System.out.println(dateToString(predDate) + "," + realPredSensex.getClose() + "," + predictedClose);
                }
       		} 
        	catch (Exception e) {
                        // logger.error("Skipped prediction for: " +
                        // FtseUtils.getMySqlDateString(predDate));
            }
            predDate = getTomorrrow(predDate);
        }
        while (++count <= PREDICTION_RANGE);
        getPredicted();
    }

    private Sensex findBestGuess(List<Sensex> guesses){
            double max = Double.MIN_VALUE;
            Sensex best = guesses.get(0);
            for (Sensex g : guesses){
            	Double likelihood = findLikelihoodByDate(g.getDate());
                if (likelihood > max){
                	max = likelihood;
                    best = g;
                }
            }
            return best;
    }
   
    private void generateEstimates(Hmm<ObservationVector> learnt) throws IOException{
        MarkovGenerator <ObservationVector> mg = new MarkovGenerator <ObservationVector> (learnt);
        List <List <ObservationVector> > estimateSeq = new ArrayList<List <ObservationVector> > ();
        List<ObservationVector> sample = mg.observationSequence(600);
        Collections.reverse(sample);
        estimateSeq.add(sample);
        ObservationSequencesWriter.write(new BufferedWriter(new FileWriter("estimates3.csv")),
                            new csvObservationVectorWriter(), estimateSeq);
    }

    private Hmm<?> initializeHmm(){
    	
        int sequenceSize = data1.size() + data2.size() + data3.size() + data4.size();

        OpdfMultiGaussianMixture<ObservationVector> opdf1 = createOpdfMultiGaussianMixture(0, 1, D, M, null);
        OpdfMultiGaussianMixture<ObservationVector> opdf2 = createOpdfMultiGaussianMixture(0, 1, D, M, null);
        OpdfMultiGaussianMixture<ObservationVector> opdf3 = createOpdfMultiGaussianMixture(0, 1, D, M, null);
        OpdfMultiGaussianMixture<ObservationVector> opdf4 = createOpdfMultiGaussianMixture(0, 1, D, M, null);

        opdf1.fit(data1);
        opdf2.fit(data2);
        opdf3.fit(data3);
        opdf4.fit(data4);

        List <Opdf <ObservationVector> > opdfs = new ArrayList<Opdf <ObservationVector> > ();

        opdfs.add(opdf1);
        opdfs.add(opdf2);
        opdfs.add(opdf3);
        opdfs.add(opdf4);

        leftRightHmm<ObservationVector> hmm = new leftRightHmm<ObservationVector>(N, delta, opdfs);
        hmmUtils.refineTransitions(hmm, sequenceSize);
        hmm.setPis(new double[] { 1., 0., 0., 0. });
        return hmm;
    }

	private MultiGaussianDistribution createMultiGaussianDistribution(double mean, double covariance, int dim){
        double[] means = new double[dim];
        Arrays.fill(means, mean);
        double[][] covs = MatrixUtils.createRealIdentityMatrix(dim).scalarMultiply(covariance).getData();
        return new MultiGaussianDistribution(means, covs);
    }

     private MultiGaussianMixtureDistribution createMultiGaussianMixtureDistribution(double mean, double covariance,int dim, int mixtures, double[] mixtureProps){
        if (mixtureProps == null){
    	    mixtureProps = new double[mixtures];
            Arrays.fill(mixtureProps, 1.0 / mixtures);
        }
        mixtures = mixtureProps.length;
        MultiGaussianDistribution[] mgds = new MultiGaussianDistribution[mixtures];
        for (int i = 0; i < mgds.length; i++){
        	mgds[i] = createMultiGaussianDistribution(mean, covariance, dim);
        }
        return new MultiGaussianMixtureDistribution(mgds, mixtureProps);
    }

    private OpdfMultiGaussianMixture<ObservationVector> createOpdfMultiGaussianMixture(double mean, double covariance,int dim, int mixtures, double[] mixtureProps)
    {
            return new OpdfMultiGaussianMixture<ObservationVector>(createMultiGaussianMixtureDistribution(mean, covariance,dim, mixtures, mixtureProps));
    }

    private Hmm <ObservationVector> learnBWL(Hmm<ObservationVector> hmm) throws IOException{
	
        BaumWelchLearner bwl = new BaumWelchLearner();
        bwl.setNbIterations(55);
        List<List<ObservationVector>> sequences = getSequences(hmm);
        display(hmm, "before3.dot");
        Hmm<ObservationVector> learnt = bwl.learn(hmm, sequences);
        display(learnt, "after3.dot");
        return learnt;
    }

    private List<List <ObservationVector> > getSequences(Hmm <ObservationVector> hmm){
        List <ObservationVector> obs1 = new ArrayList <ObservationVector> ();
        obs1.addAll(data1);
        List <ObservationVector> obs2 = new ArrayList <ObservationVector> ();
        obs2.addAll(data2);
        List <ObservationVector> obs3 = new ArrayList <ObservationVector> ();
        obs3.addAll(data3);
        List <ObservationVector> obs4 = new ArrayList <ObservationVector> ();
        obs4.addAll(data4);
        List <List <ObservationVector> > obs = new ArrayList <List <ObservationVector> > ();
        obs.add(obs1);
        obs.add(obs2);
        obs.add(obs3);
        obs.add(obs4);
        return obs;
    }

    private void display(Hmm<?> hmm, String filename) throws IOException{
        System.out.println("------------------------------");
        (new GenericHmmDrawerDot()).write(hmm, filename);
        System.out.println(hmm);
    }

    private  List<Sensex> getData(String from ,String to) throws FileNotFoundException, IOException, ParseException{
    	boolean valid = false;
        BufferedReader bufferedReader = new BufferedReader(new FileReader("C:/Users/Kamal/Downloads/hmm/table.csv"));
        List <Sensex> data = new ArrayList();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        double open = 0,high = 0,close = 0,low = 0;
        Date dateInstance = new Date();
        long id = 1;
        int colID = 0;
        StringTokenizer tokenizer;
        String line;
        String word;
        while((line = bufferedReader.readLine())!= null){
        	tokenizer = new StringTokenizer(line, ",");
            colID = 0;
            while(tokenizer.hasMoreTokens()){
            	word = tokenizer.nextToken();
                if(colID == 0){
                	dateInstance=dateFormat.parse(word);
                    if(dateInstance.after(getDate(from)) == true && dateInstance.before(getDate(to)) == true){
                        valid = true;
                    }
                    else{
                        valid = false;
                    }
                }
                if(colID == 1){
                	open = (Double.parseDouble(word));
                }
                if(colID == 2){
                	low = (Double.parseDouble(word));
                }
                if(colID == 3){
                    high = (Double.parseDouble(word));
                }
                if(colID==4){
                    close = (Double.parseDouble(word));
                }
              colID++;
            }
            if(valid == true){
            	data.add(new Sensex(dateInstance, open, low, high, close));
            	id++;
            }
        }
        return (new ArrayList(data));
    }
    

    private void initializeData() throws FileNotFoundException, IOException, ParseException {
	    data1 = getData(FROM1,TO1);
	    data2 = getData(FROM2,TO2);
	    data3 = getData(FROM3,TO3);
	    data4 = getData(FROM4,TO4);
    }

    private List <ObservationVector> findObservations(String today, int interval) throws ParseException, FileNotFoundException, IOException {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        String from = getDateDaysBeforeString(today, interval);
        List <Sensex> data = getData(from, today);
        List <ObservationVector> obsVector = new ArrayList <ObservationVector> ();
        obsVector.addAll(data);
        return obsVector;
    }
    
	public Date getDateDaysBefore(String today, int interval) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Date date = getDate(today);
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -(interval % 30));
        calendar.add(Calendar.MONTH, -(interval / 30));
        return calendar.getTime();
	}

    public String getDateDaysBeforeString(String today, int interval) throws ParseException {
    	return dateToString(getDateDaysBefore(today, interval));
    }

	private Double findLikelihoodByDate(Date yesterPredDate){
	    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String dateString = dateFormat.format(yesterPredDate);
	    for(int i = 0; i < this.likelihoodData.size(); i++){
	    	if(likelihoodData.get(i).date.equals(dateString)==true)
	            return likelihoodData.get(i).likelihoodValue;
	    }
	    return -1.00;
	}

	public static Date getTomorrrow(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        return calendar.getTime();
    }

	private List<Sensex> findByLikelihoodTolerance(Date yesterPredDate, Double yesterPredDateLikelihood, double LIKELIHOOD_TOLERANCE) throws ParseException{
	    double tempLikelihood = 0;
	    String dateString = this.dateToString(yesterPredDate);
	    for(int i=0; i < this.likelihoodData.size(); i++){
	        if(likelihoodData.get(i).date.equals(dateString) == true){
	        	tempLikelihood = likelihoodData.get(i).likelihoodValue;
	        }
	    }
	    double leftLimit = tempLikelihood - this.LIKELIHOOD_TOLERANCE;
	    double rightLimit = tempLikelihood + this.LIKELIHOOD_TOLERANCE;
	    ArrayList <Sensex> guessList = new ArrayList <Sensex> ();
	    for(int i = 0; i < likelihoodData.size(); i++){
	    	if(((likelihoodData.get(i).likelihoodValue) >= leftLimit) && (likelihoodData.get(i).likelihoodValue <= rightLimit)){
	            dateString = likelihoodData.get(i).date;
	            for(int j = 0; j < data1.size(); j++){
	            	if(this.getDate(dateString).equals(data1.get(j).getDate()) == true)
	                    guessList.add(new Sensex(data1.get(j)));
	            }
	            for(int j = 0; j < data2.size(); j++){
	                if(this.getDate(dateString).equals(data2.get(j).getDate()) == true)
	                    guessList.add(new Sensex(data2.get(j)));
	            }
	            for(int j = 0; j < data3.size(); j++){
	                if(this.getDate(dateString).equals(data3.get(j).getDate()) == true)
	                    guessList.add(new Sensex(data3.get(j)));
	            }
	            for(int j = 0; j < data4.size(); j++){
	                if(this.getDate(dateString).equals(data4.get(j).getDate()) == true)
	                    guessList.add(new Sensex(data4.get(j)));
	            }
	    	}
	    }
	    return guessList;
	}
	
	private Sensex findNextByDate(Date tomorrow){
		Sensex nextEntry = null;
		for(int i = 0; i < data1.size(); i++){
			if(tomorrow.equals(data1.get(i).getDate()) == true && i != data1.size()){
               nextEntry = new Sensex(data1.get(i));
            }
        }
        for(int i = 0; i < data2.size(); i++){
            if(tomorrow.equals(data2.get(i).getDate()) == true && i != data2.size()){
               nextEntry = new Sensex(data2.get(i));
            }
        }
        for(int i = 0; i < data3.size(); i++){
            if(tomorrow.equals(data3.get(i).getDate()) == true && i != data3.size()){
               nextEntry = new Sensex(data3.get(i));
            }
        }
        for(int i = 0; i < data4.size(); i++){
            if(tomorrow.equals(data4.get(i).getDate()) == true && i != data4.size()){
               nextEntry = new Sensex(data4.get(i));
            }
        }
        return new Sensex(nextEntry);
    }

    void getPredicted() throws ParseException{
        Sensex sensexInstance = new Sensex();
        for(int i = 0; i < data2.size(); i++){
            sensexInstance = data2.get(i);
            if(sensexInstance.getDate().equals(getDate(predictionDate)) == true){
            	if(i != 0){
                    if(data2.get(i-1).getOpen()< data2.get(i).getOpen())
                    	data2.get(i).find(0);
                    else
                    	data2.get(i).find(1);
                }
            }
        }
    }
}