package test;

import java.io.IOException;

public class tester {
	public static void main(String[] args) throws IOException, CloneNotSupportedException {
		String D = "YahooA1";
		int W = 1415;
		int S = 71;
		double R = 60;
		int K = 140;
		double T = -1;
		String raw = "--D "+D+" --R "+R+" --K "+K+" --W "+W+" --S "+S+" --T "+T;
		simulator.main(raw.split(" "));
		
		for (int i = 0 ; i <= 10; i++) {
			T = 0.1*i;
			//R = 5*i;
			raw = "--D "+D+" --R "+R+" --K "+K+" --W "+W+" --S "+S+" --T "+T;
			simulator.main(raw.split(" "));
			System.gc();
		}
		
	}
}
