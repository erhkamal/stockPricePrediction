package hmm;

import java.util.Random;
class randomRange {
	
	long showRandomInteger(double aStart, double aEnd, Random aRandom){
		if ( aStart > aEnd ) {
			throw new IllegalArgumentException("Start cannot exceed End.");
		}
		long range = (long)aEnd - (long)aStart + 1;
		long fraction = (long)(range * aRandom.nextDouble());
		long randomNumber =  (int)(fraction + aStart);
		return randomNumber;
	}
}