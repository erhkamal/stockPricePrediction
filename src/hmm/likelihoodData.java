package hmm;

public class likelihoodData{
	String date;
    double likelihoodValue;

    likelihoodData(likelihoodData instance){
    	this.date = instance.date;
        this.likelihoodValue = instance.likelihoodValue;
    }

    likelihoodData(){
    	
    }
}
