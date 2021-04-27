//package
package main;

//import
import java.util.ArrayList;

public class KernelCenter implements Cloneable, Comparable<KernelCenter>{
	public ArrayList<Integer> id;
	public double[] value;
	public int card;
	public double comparedValue;
	public Double prevDensity;
	public Double localDensity;
	public double localDensityUp;
	public double localDensityLow;
	public double outlierScore;
	public double outlierScoreUp;
	public double outlierScoreLow;
	public ArrayList<KernelCenter> kNNKCs;
	public double kNNKCCardTotal;
	public KernelDensityEstimator BalloonKDE; 
	public double density_mean;
	public double density_sd;
	public double kdist;
	public double accumDeltaWeightRatio;
	
	public KernelCenter(ArrayList<Integer> id, double[] value, int card) {
		this.id = id;
		this.value = value;
		this.card = card;
		this.kNNKCs = new ArrayList<KernelCenter>();
		this.accumDeltaWeightRatio = 0;
	}
	
	public KernelCenter clone() throws CloneNotSupportedException {
		return (KernelCenter) super.clone();
	}

	@Override
	public int compareTo(KernelCenter other) {
		return this.comparedValue <= other.comparedValue ? 1 : - 1; // a higher distance gives a higher priority
	}
	
}
