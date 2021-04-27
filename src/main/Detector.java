//package
package main;

//import
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Detector {
	public Window window;
	public int dim;
	public int nS;
	public int K;
	public int S;
	public double[] minValues;
	public double[] dimLength;
	public PriorityQueue<KernelCenter> candOutKCs;
	public HashSet<KernelCenter> updatedKCs;
	public HashMap<ArrayList<Integer>, Double> accumDenChg;
	public double skipThred;
	
	public Detector(int dim, int nS, double R, int K, int S, double[] minValues, double skipThred) {		
		this.dim = dim;
		this.nS = nS;
		this.K = K;
		this.S = S;
		
		this.minValues = minValues;
		this.dimLength = new double[dim];
		for(int i = 0;i<dim;i++) dimLength[i] = Math.sqrt(R*R/dim);
		this.updatedKCs = new HashSet<KernelCenter>();
		this.candOutKCs = new PriorityQueue<KernelCenter>();
		this.accumDenChg = new HashMap<ArrayList<Integer>, Double>();
		this.skipThred = skipThred;
		this.window = new Window();
	}
	
	//Process new slide
	public void slide(ArrayList<Tuple> newTuples, int slideID, int newOutliers) throws CloneNotSupportedException {
		HashMap<ArrayList<Integer>,Cell> slideIn = indexingSlide(newTuples);
		window.update(slideIn, slideID, nS, newOutliers);
	}

	public PriorityQueue<Tuple> getOutliers(int N, int numWindows) {		
		//Clear updatedKCs
		updatedKCs.clear();
		
		if(numWindows == 1 || skipThred < 0) {
			//if first window or noskip, conduct full procedures
			updatedKCs.addAll(window.currentKCs.values());
		}else {
			//Add new kernel centers into the (to be) updated kernel centers
			updatedKCs.addAll(window.newKernelCenters);
						
			//Find the KCs to update.
			for(ArrayList<Integer> key: window.currentKCs.keySet()) {
				KernelCenter kc = window.currentKCs.get(key);

				if(window.newKernelCenters.contains(kc)) continue;
				for(KernelCenter chgedKC: window.currentNetCards.keySet()) {
					double dist = dist(kc.value, chgedKC.value);
					if(kc.kdist >= dist) {
						kc.accumDeltaWeightRatio += Math.abs(window.currentNetCards.get(chgedKC))/kc.kNNKCCardTotal;
						if(kc.accumDeltaWeightRatio > skipThred) {
							kc.accumDeltaWeightRatio = 0;
							updatedKCs.add(kc);
							break;
						}
					}
				//kc.accumDeltaWeightRatio = 0; //uncomment this to consider only instant error	
				}
			}
		}
		cellLevelDetect(this.updatedKCs, N); //ick top-N KCs
		
		PriorityQueue<Tuple> outliers = pointLevelDetect(N, numWindows); //pick top-N outliers from candOutKCs
		return outliers;
	}

	//Cell-level detection
	public void cellLevelDetect(HashSet<KernelCenter> poolOfupdateKCs, int N){
		for(KernelCenter kc:poolOfupdateKCs) {
			updateLocalDensity(kc);
		}
		
		//Update outlier score
		for(KernelCenter kc:window.currentKCs.values()) {
			double[] LDSet = new double[kc.kNNKCs.size()];
			for(int i= 0; i<kc.kNNKCs.size(); i++) LDSet[i] = kc.kNNKCs.get(i).localDensity;
			kc.density_mean = mean(LDSet); 
			kc.density_sd = sd(LDSet, kc.density_mean);
			
			//Scores are not inversed.
			kc.outlierScore = (kc.localDensity - kc.density_mean)/kc.density_sd; 
			kc.outlierScoreUp =(kc.localDensityUp - kc.density_mean)/kc.density_sd; 
			kc.outlierScoreLow = (kc.localDensityLow - kc.density_mean)/kc.density_sd;
			
			kc.comparedValue = kc.outlierScoreUp;
		}
		
		//Clear candidate outliers
		this.candOutKCs.clear();
		
				
		//Pick top N Kernel centers using priority queue. (scores are not inversed)
		for (KernelCenter kc:window.currentKCs.values()) { 
			kc.comparedValue = kc.outlierScoreUp;
			if (this.candOutKCs.size() < N){
				this.candOutKCs.add(kc);
			}else if(N > 0 && kc.outlierScoreLow > this.candOutKCs.peek().outlierScoreUp) {
				continue; 
			}else if(N > 0 && kc.outlierScoreUp < this.candOutKCs.peek().outlierScoreLow){
				this.candOutKCs.poll();
				this.candOutKCs.add(kc);
			}else if (N > 0) {
				this.candOutKCs.add(kc);
			}
		}
	}
	
	//Point-level detection
	public PriorityQueue<Tuple> pointLevelDetect(int N, int numWindows) {
		PriorityQueue<Tuple> topNOutliers = new PriorityQueue<Tuple>();
		
		for(KernelCenter kc:this.candOutKCs){
			Boolean updatedKCFlag = this.updatedKCs.contains(kc);
			for (Cell c:window.windowCells.get(kc.id).values()) {
				for (Tuple t:c.tuples) {
					//Check if the last updated density is still feasible. If not, update the density
					if(updatedKCFlag || t.lastUpdatedWindow != numWindows-1) {
						t.prevDensity = t.localDensity;
						t.localDensity = kc.BalloonKDE.getDensity(t.value);
					}
					t.lastUpdatedWindow = numWindows;
					t.outlierScore = (t.localDensity - kc.density_mean)/kc.density_sd; //scores are not inversed
					if (topNOutliers.size() < N){
						topNOutliers.add(t);
					}else if(N > 0 && t.outlierScore <= topNOutliers.peek().outlierScore){
						topNOutliers.poll();
						topNOutliers.add(t);
					}
				}
			}
		}	

		return topNOutliers;
	}
	
	//Index new tuples 
	public HashMap<ArrayList<Integer>,Cell> indexingSlide(ArrayList<Tuple> newTuples){
		HashMap<ArrayList<Integer>,Cell> indexedSlide = new HashMap<ArrayList<Integer>,Cell>();
		
		for(Tuple t:newTuples) {
			ArrayList<Double> id = new ArrayList<Double>();
			id.add(t.value[0]);
			ArrayList<Integer> cellIdx = new ArrayList<Integer>();
			for (int j = 0; j<dim; j++) { 
				int dimIdx = (int) ((t.value[j]-minValues[j])/dimLength[j]);
				cellIdx.add(dimIdx);
			}
			t.cellIdx = cellIdx;
			
			if(!indexedSlide.containsKey(cellIdx)) {
				double[] cellCenter = new double[dim];
				for (int j = 0; j<dim; j++) cellCenter[j] = minValues[j] + cellIdx.get(j)*dimLength[j]+dimLength[j]/2;
				indexedSlide.put(cellIdx, new Cell(cellIdx, cellCenter, 0));
			}
			indexedSlide.get(cellIdx).addTuple(t);
		}

		return indexedSlide;
	}


	//Get local density of a kernel center
	public void updateLocalDensity(KernelCenter kc) {
		double cardTotal = 0;
		Collection<KernelCenter> neighborCandidate = window.currentKCs.values();
		PriorityQueue<KernelCenter> kNNKCs = new PriorityQueue<KernelCenter>();
		
		if(kc.kNNKCs.size()>0) {
			//Restore only existing previous neighbor kernel centers
			for(KernelCenter knnKC: kc.kNNKCs) {
				if(knnKC.card > 0) {
					cardTotal += knnKC.card;
					knnKC.comparedValue = dist(kc.value, knnKC.value);
					kNNKCs.add(knnKC);
				}
			}
			
			if(kNNKCs.size() == K && skipThred >=0) {
				neighborCandidate = window.chgdCurrentKCs;
			}
		}
			
		for(KernelCenter kcOther : neighborCandidate){
			if(kNNKCs.contains(kcOther)) continue;
			
			kcOther.comparedValue = dist(kc.value, kcOther.value);
			if (kNNKCs.size() < K){
				kNNKCs.add(kcOther);
				cardTotal += kcOther.card;
			}else if(kcOther.comparedValue < kNNKCs.peek().comparedValue){
				kNNKCs.add(kcOther);
				cardTotal = cardTotal + kcOther.card - kNNKCs.poll().card;
			}
		}
		
		kc.kNNKCs = new ArrayList<KernelCenter>(kNNKCs);
		kc.kNNKCCardTotal = cardTotal;
		kc.kdist = kNNKCs.peek().comparedValue;
		
		KernelDensityEstimator BalloonKDE = new KernelDensityEstimator(new ArrayList<KernelCenter>(kNNKCs), dim, cardTotal);
		kc.BalloonKDE = BalloonKDE;
		
		if(kc.localDensity != null) kc.prevDensity = kc.localDensity;
		kc.localDensity = BalloonKDE.getDensity(kc.value); 
		kc.localDensityUp = BalloonKDE.getDensityUp(kc.value, dimLength);
		kc.localDensityLow = BalloonKDE.getDensityLow(kc.value, dimLength);
		
	}
	
	/* Utilities */
	//distance b/w kc and kc
	public static double dist(KernelCenter kc1, KernelCenter kc2) {
		double ss = 0;
		for(int i = 0; i<kc1.id.size(); i++) { 
			ss += Math.pow((kc1.id.get(i) - kc2.id.get(i)),2);
		}
		 return Math.sqrt(ss);
	}	
	
	//distance b/w values and values
	public static double dist(double[] d1, double[] d2) {
		double ss = 0;
		for(int i = 0; i<d1.length; i++) { 
			ss += Math.pow((d1[i]*1.0 - d2[i]*1.0),2);
		}
		 return Math.sqrt(ss);
	}	
	
	public static double mean(double[] X) {
        double sum = 0.0;
        for (int i = 0; i < X.length; i++) {
        	sum += X[i];
        }
        return sum/X.length;
    }
		
	public static double sd(double[] X, double mean) {
		double sum = 0.0;
		for (int i = 0; i < X.length; i++) {
			sum += Math.pow((X[i]-mean),2);
        }
		if(sum == 0) {
			return Double.MIN_VALUE;
		}else {
			return Math.sqrt(sum/X.length);
		}
		
	}
	
	public double getAvgBandwidth() {
		double sum = 0;
		double cnt = 0;
		double max = 0;
		for(KernelCenter kc: this.window.currentKCs.values()) {
			for(int i = 0; i <dim; i++) {
				sum += kc.BalloonKDE.h[i];
				cnt += 1;
				if (max<kc.BalloonKDE.h[i]) max = kc.BalloonKDE.h[i];
			}
		}
		double avgh = sum/cnt; //average bandwidth
		//double avgh = max; //conservative bandwdith (maximum bandwidth)
		
		return avgh;
	}
	
}
