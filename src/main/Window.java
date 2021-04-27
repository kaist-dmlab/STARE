//package
package main;

//import
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class Window {
	//windowCells = {Cell idx:{slideID:Cell}}
	public HashMap<ArrayList<Integer>, HashMap<Integer, Cell>> windowCells;
	public HashMap<ArrayList<Integer>, KernelCenter> currentKCs;
	public HashMap<KernelCenter, Integer> currentNetCards;
	public HashSet<KernelCenter> chgdCurrentKCs;
	public LinkedList<Integer> realOutliers;
	public HashSet<KernelCenter> newKernelCenters;


	public Window(){
		this.windowCells = new HashMap<ArrayList<Integer>, HashMap<Integer, Cell>>();
		this.currentKCs = new HashMap<ArrayList<Integer>, KernelCenter>();
		this.currentNetCards = new HashMap<KernelCenter, Integer>();
		this.chgdCurrentKCs = new HashSet<KernelCenter>();
		this.realOutliers = new LinkedList<Integer>();
		this.newKernelCenters = new HashSet<KernelCenter>();
	}
	
	//update window
	public void update(HashMap<ArrayList<Integer>,Cell> slideIn, int slideInID, int nS, int newOutliers) throws CloneNotSupportedException {
		currentNetCards.clear();
		newKernelCenters.clear();
		addSlide(slideIn, slideInID);
			
		if(slideInID >= nS) {
			removeSlideID(slideInID-nS);
			realOutliers.poll();
		}
		realOutliers.add(newOutliers);
		
		chgdCurrentKCs.clear();
		for(KernelCenter kc:currentNetCards.keySet()) if(currentKCs.containsValue(kc)) chgdCurrentKCs.add(kc);			
		
	}
	
	//add the new slide and corresponding cells
	private void addSlide(HashMap<ArrayList<Integer>,Cell> slide, int slideID) {
		for(ArrayList<Integer> cellIdx:slide.keySet()) {
			if(!windowCells.containsKey(cellIdx)) {
				windowCells.put(cellIdx, new HashMap<Integer, Cell>());
				KernelCenter kc = new KernelCenter(cellIdx, slide.get(cellIdx).cellCenter, 0);
				currentKCs.put(cellIdx,kc);
				newKernelCenters.add(kc);
			}
				
			windowCells.get(cellIdx).put(slideID,slide.get(cellIdx));
			int card = slide.get(cellIdx).getCard();
			currentKCs.get(cellIdx).card += card;
			
			//add to delta G (net cardinality between two consecutive windows)
			currentNetCards.put(currentKCs.get(cellIdx), card);
			
			}
		}

	//remove the expired slide and corresponding cells using just slide ID
	
	private void removeSlideID(int slideID) {
		
		ArrayList<ArrayList<Integer>> emptyCellIdx = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> cellIdx:windowCells.keySet()) {
			KernelCenter kc = currentKCs.get(cellIdx);
			if(windowCells.get(cellIdx).containsKey(slideID)) {
				int card = windowCells.get(cellIdx).get(slideID).getCard();
				kc.card -= card;
				
				//remove from delta G (net cardinality between two consecutive windows)
				if(currentNetCards.containsKey(kc)) {
					int deltaCard = currentNetCards.get(kc)-card;
					if(deltaCard == 0) {
						currentNetCards.remove(kc);
					}else {
						currentNetCards.put(kc, deltaCard);
					}
				}else {
					currentNetCards.put(kc, -card);
				}
				
				windowCells.get(cellIdx).remove(slideID);
				if(windowCells.get(cellIdx).isEmpty()) {
					emptyCellIdx.add(cellIdx);					
				}
			}
		}
				
		if(!emptyCellIdx.isEmpty()) {
			for(ArrayList<Integer> cellIdx: emptyCellIdx) {
				windowCells.remove(cellIdx);
				currentKCs.remove(cellIdx);
			}
		}
	}
	
		
	public int getNumOutliers() {
		int numOutliers = 0;
		for(int o:realOutliers) numOutliers +=o;
		return numOutliers;
	}
	
	public Double idxDist(ArrayList<Integer> center, ArrayList<Integer> kcIdx) {
		double ss = 0;
		for (int i = 0; i< kcIdx.size(); i++) {
			ss += Math.pow((kcIdx.get(i) - center.get(i)), 2);
		}
		return Math.sqrt(ss);
	}
	
}
