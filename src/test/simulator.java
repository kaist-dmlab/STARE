package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

import main.Detector;
import main.StreamGenerator;
import main.Tuple;

public class simulator {
	static String dataset = "YahooA2"; //dataset name
	static int W = 1421; //window size
	static int S = 71; //slide size
	static double R = 65; // size of a grid cell
	static int K = 50; // number of neighbors
	static int nW = 10000; // number of windows to process
	static int fixedN = -1; // fix N by a positive integer value  
	static double skipThred = 0.1; // between 0 and 1. the default optimal value is 0.1
	static String printType = "Console"; // "Console" or "File"
	static boolean reportOutlierList = false; // true or false
	static BufferedWriter fw;
	static MemoryThread memThread = new MemoryThread();
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException, CloneNotSupportedException {
		loadArgs(args);

		if(printType.equals("File")){
			String fileName = "Result_"+dataset+".txt";
			fw = new BufferedWriter(new FileWriter(fileName, true));
		}
				
		int nS = W/S;
		StreamGenerator streamGen = new StreamGenerator(dataset);
		int dim = streamGen.getMinValues().length;
		
		Detector detector = new Detector(dim, nS, R, K, S, streamGen.getMinValues(), skipThred);
		int numWindows = 0;
		int numRealOutliersSum = 0;
		double numFoundOutliersSum = 0;
		double totalTime = 0;
		double APSum = 0;
		
		memThread.start();
		for (int i = 0; i< nW+nS-1; i++) {
			//Get the new slide
			ArrayList<Tuple> newTuples = streamGen.getTuples(i*S ,(i+1)*S);
			if(newTuples.size() < S) break;
			
			int newOutliers = 0;
			for(Tuple t:newTuples) if(t.outlier) newOutliers++;
			
			//Slide the window
			double startTime = getCPUTime();
			detector.slide(newTuples, i, newOutliers);
			
			//Check if a whole window can be prepared
			if(i >= nS-1) {
				numWindows++;
				int N = (fixedN > 0 ? fixedN : detector.window.getNumOutliers()); // given N or the true N 
				
				//Get Top-N outliers
				PriorityQueue<Tuple> topNOut = detector.getOutliers(N, numWindows);
			
				//Update stats
				double numFoundOutliers = 0;
				double AP = 0;
				Stack<Tuple> orderedTopNOut = new Stack<Tuple>();	
				while(!topNOut.isEmpty()) orderedTopNOut.push(topNOut.poll());
				
				//Console or File output
				if(reportOutlierList & printType.equals("Console")) {
					System.out.print("At window "+numWindows+", detected top-N outliers IDs: ");
				}else if(reportOutlierList) {
					fw.write("At window "+numWindows+", detected top-N outliers IDs: ");
				}
				
				for(int rank = 1; rank<N+1; rank++) {
					Tuple t = orderedTopNOut.pop();
					if(t.outlier) {
						numFoundOutliers++;
						AP += numFoundOutliers/(rank*1.0);
					}
					if(reportOutlierList & printType.equals("Console")) {
						System.out.print(t.id + " ");
					}else if(reportOutlierList) {
						fw.write(t.id + " ");
					}	
				}
				if(reportOutlierList & printType.equals("Console")) {
					System.out.println();
				}else if(reportOutlierList) {
					fw.newLine();
				}
				
				
				if(N > 0) APSum += AP;
				numRealOutliersSum += N;
				numFoundOutliersSum += numFoundOutliers;
				totalTime += getCPUTime() - startTime;

			}
		}
	
		if(printType.equals("Console")) {
			System.out.println("Dataset: "+dataset+"\tW: "+W+"\tS: "+S+"\tR: "+R+"\tK: "+K+"\tskipThred: "+skipThred
					+"\t=>\tRP: "+Math.round((numFoundOutliersSum/numRealOutliersSum)*1000)/1000.0
					+"\tAP: "+Math.round((APSum/numRealOutliersSum)*1000)/1000.0
					+"\tavgCPUTime: "+Math.round((totalTime/numWindows)/1000)/1000.0
					+"\tPeakMem: "+Math.round(memThread.maxMemory*100)/100.0); //max memory
		}else {
			fw.write("Dataset: "+dataset+"\tW: "+W+"\tS: "+S+"\tR: "+R+"\tK: "+K+"\tskipThred: "+skipThred
					+"\t=>\tRP: "+Math.round((numFoundOutliersSum/numRealOutliersSum)*1000)/1000.0
					+"\tAP: "+Math.round((APSum/numRealOutliersSum)*1000)/1000.0
					+"\tavgCPUTime: "+Math.round((totalTime/numWindows)/1000)/1000.0
					+"\tPeakMem: "+Math.round(memThread.maxMemory*100)/100.0);
			fw.newLine();
			fw.flush();
			fw.close();
		}
		memThread.stop();	
	}
		

    public static long getCPUTime(){
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()? bean.getCurrentThreadCpuTime(): 0L;
    }
    
	public static void loadArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].indexOf("--") == 0) {
                switch (args[i]) {
	                case "--D": // data set
	                    dataset = args[i + 1];
	                    break;
                    case "--R": // grid cell size
                        R = Double.valueOf(args[i + 1]);
                        break;
                    case "--K": // number of neighbors
                        K = Integer.valueOf(args[i + 1]);
                        break;                    
                    case "--W": // window size
                        W = Integer.valueOf(args[i + 1]);
                        break;
                    case "--S": // slide size
                        S = Integer.valueOf(args[i + 1]);
                        break;
                    case "--T": // skip threshold
                    	skipThred = Double.valueOf(args[i + 1]);
                        break;   
                    case "--N": // Top-N
                    	fixedN = Integer.valueOf(args[i + 1]);
                    	break;
                    case "--nW": // Number of windows to process
                    	nW = Integer.valueOf(args[i + 1]);
                    	break;
                    case "--P": // print type
                    	printType = args[i + 1];
                    	break;
                    case "--O": // report outliers IDs
                    	reportOutlierList = (Integer.valueOf(args[i + 1]) == 0 ? false : true); 
                }
            }
        }
    }
	//The default paraemter values for each data set
	public static void loadDefaultArgs(String dataset) {
		switch (dataset) {
	         case "YahooA1":
	             W = 1415;
	             S = 71;
	             R = 60;
	             K = 140;
	             break;
	         case "YahooA2":
	        	 W = 1421;
	             S = 71;
	             R = 65;
	             K = 50;
	             break;
	         case "HTTP":
	        	 W = 6000;
	             S = 300;
	             R = 24;
	             K = 5;
	             break;                    
	         case "DLR":
	        	 W = 1000;
	             S = 50;
	             R = 18.8;
	             K = 2;
	             break;
	         case "ECG":
	        	 W = 2337;
	             S = 117;
	             R = 13.57;
	             K = 2;
	             break;   
		}
	}
}
