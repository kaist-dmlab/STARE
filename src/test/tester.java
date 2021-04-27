package test;
import java.io.IOException;

public class tester {
	public static void main(String[] args) throws IOException, CloneNotSupportedException {
		String D = "YahooA1"; //data set
		int W = 1415; //window size
		int S = 71; //slide size
		double R = 60; //distance threshold
		int K = 140; //number of neighbors
		double T = -1; //skip threshold
		String raw = "--D "+D+" --R "+R+" --K "+K+" --W "+W+" --S "+S+" --T "+T;
		simulator.main(raw.split(" "));
		
		for (int i = 0 ; i <= 10; i++) {
			T = 0.1*i;
			raw = "--D "+D+" --R "+R+" --K "+K+" --W "+W+" --S "+S+" --T "+T;
			simulator.main(raw.split(" "));
			System.gc();
		}
		
	}
}
