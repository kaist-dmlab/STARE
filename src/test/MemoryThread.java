package test;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is from Luan's work (http://infolab.usc.edu/Luan/Outlier/)
 * @author Luan
 */

public class MemoryThread extends Thread {
	private static final double MegaBytes = 1024*1024;
    public double maxMemory = 0;
    public void computeMemory() {
        Runtime.getRuntime().gc();
        double used = (Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/MegaBytes;
        if(maxMemory < used)
            maxMemory = used;
    }

    @Override
    public void run() {
        while (true) {
            computeMemory();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(MemoryThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
