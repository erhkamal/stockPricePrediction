package hmm;

import java.io.IOException;
import java.io.Writer;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationVectorWriter;

public class csvObservationVectorWriter extends ObservationVectorWriter {
	public void write(ObservationVector observation, Writer writer) throws IOException {
		writer.write(hmmUtils.getCsvFormat(observation) + "\n");
	}
}