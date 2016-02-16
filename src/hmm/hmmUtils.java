package hmm;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

public class hmmUtils {
	
	public static void refineTransitions(Hmm<?> hmm, int sequenceSize) {
	    double bucketSize = (double) sequenceSize / hmm.nbStates();
	    double p = 1.0 / bucketSize;
	    for (int i = 0; i < hmm.nbStates(); i++){
	    	hmm.setAij(i, i, 1 - p);
	        for (int j = i + 1; j < hmm.nbStates(); ++j) {
                hmm.setAij(i, j, p);
                hmm.setAij(j, i, p);
            }
	    }
	}
        
	public static String getCsvFormat(ObservationVector o) {
        List<Double> values = new ArrayList<Double>();
        for (Double v : o.values()) {
            values.add(v);
        }
        return StringUtils.collectionToCommaDelimitedString(values);
    }

	private hmmUtils() {
    
	}
}