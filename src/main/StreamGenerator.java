//package
package main;

//import
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class StreamGenerator {
	private double[] maxValues; //max values
	private double[] minValues; //min values
	private BufferedReader br; //buffer reader
	private String filePath; //file path
	
	public StreamGenerator (String dataset) throws IOException {
		filePath = "datasets/"+dataset+".csv";
		this.br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		int dim = line.split(",").length-1;
		this.maxValues = new double[dim];
		this.minValues = new double[dim];
		for(int i = 0; i < dim; i ++) {
			this.maxValues[i] = Double.MIN_VALUE;
			this.minValues[i] = Double.MAX_VALUE;
		}
		
		while(line!=null) {
			String[] rawValues = line.split(",");
			
			for(int i = 0 ; i< dim; i++) {
				double value = Double.parseDouble(rawValues[i]);
				if (this.maxValues[i] < value) this.maxValues[i] = value;
				if (this.minValues[i] > value) this.minValues[i] = value;
			}

			line = br.readLine();
		}
	}
	
	//get new slide
	public ArrayList<Tuple> getNewSlideTuples(int itr, int S) throws IOException {
		ArrayList<Tuple> newSlide = new ArrayList<Tuple>();
		this.br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		int tid = 0;
		
		while(line!=null) {
			if(tid>=itr*S) {
				String[] rawValues = line.split(",");
				double[] value = new double[rawValues.length];
				
				for(int i = 0 ; i< rawValues.length; i++) {
					value[i] = Double.parseDouble(rawValues[i]);
				}
				boolean outlier = (rawValues[rawValues.length].equals('0') ? false : true);
				Tuple tuple = new Tuple(tid, itr, value, outlier);
				newSlide.add(tuple);
			}
			tid++;
			if(tid==(itr+1)*S) break;
			line = br.readLine();
		}
		return newSlide;
	}
	
	//get new tuples whose ids are between start and end
	public ArrayList<Tuple> getTuples(int start, int end) throws IOException {
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		this.br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		int tid = 0;
		
		while(line!=null) {
			if(tid>=start) {
				String[] rawValues = line.split(",");
				double[] value = new double[rawValues.length];
				for(int i = 0 ; i< rawValues.length; i++) {
					value[i] = Double.parseDouble(rawValues[i]);
				}
				boolean outlier = (Double.parseDouble(rawValues[rawValues.length-1]) < 1 ? false : true);
				Tuple tuple = new Tuple(tid, 0, value, outlier);
				tuples.add(tuple);
			}
			tid++;
			if(tid==end) break;
			line = br.readLine();
		}
		return tuples;
	}


	public double[] getMaxValues() {
		return this.maxValues;
	}
	public double[] getMinValues(){
		return this.minValues;
	}
}
