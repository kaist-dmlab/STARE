package main;

import java.util.ArrayList;

public class Tuple implements Comparable<Tuple>{
		public int id;
		public int slideID;
		public double[] value;
		public ArrayList<Integer> cellIdx;
		public boolean outlier;
		public double outlierScore;
		public double localDensity;
		public Double prevDensity;
		public int lastUpdatedWindow;
	
		public Tuple(int id, int slideID, double[] value, boolean outlier) {
			this.id = id;
			this.slideID = slideID;
			this.value = value;
			this.outlier = outlier;
			this.localDensity = Double.POSITIVE_INFINITY; //initialized to check if it has been computed previously.
		}
		
		//comparing tuples 
		@Override
		public int compareTo(Tuple other) {
			return this.outlierScore <= other.outlierScore ? 1 : - 1; // a higher outlier score gives a higher priority (score is not inversed)
		}
	}
