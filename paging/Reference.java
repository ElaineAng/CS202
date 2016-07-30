package paging;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Reference {
	int jobMix;
	BufferedReader br;
	int processSize;
	boolean v;
	public Reference(int jm, int ps, boolean verbose){
		jobMix = jm;
		String fn = System.getProperty("user.dir")+"/paging/random.txt";
		processSize = ps;
		v = verbose;
		try {
			br = new BufferedReader(new FileReader(fn));			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public int getNext(int curRef, int process){
		double MAX = Integer.MAX_VALUE + 1d;
		double y;
		double A,B,C;
		A = 0;
		B = 0;
		C = 0;	
		int ref = 0;
		
		if (jobMix == 1 || jobMix == 2){
			A = 1;
		}
		if (jobMix == 4){
			if (process == 1){
				A = 0.75;
				B = 0.25;
			}
			if (process == 2){
				A = 0.75;
				C = 0.25;
			}
			if (process == 3){
				A = 0.75;
				B = 0.125;
				C = 0.125;
			}
			if (process == 4){
				A = 0.5;
				B = 0.125;
				C = 0.125;
			}
		}
		
		try {
			double rand = Double.parseDouble(br.readLine());
			if (v) System.out.println(process + " uses random number: "+ (long) rand);
			y = rand / MAX;
			if (y < A){
				ref = (curRef + 1) % processSize;
			}
			else{
				if (y < A+B){
					ref = (curRef + processSize - 5) % processSize;
				} 
				else{
					if (y < A+B+C){
						ref = (curRef + 4) % processSize;
					}
					else{
						rand = Double.parseDouble(br.readLine());
						if (v) System.out.println(process + " uses random number: "+ (long) rand);
						double ref2 = rand % processSize;
						ref = (int) ref2;
					}
				}
			}		
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ref;
	}
	
	int getRemovedPage(int numOfFrames, int process){
		int removedPage = 0;
		try {
			double rand = Double.parseDouble(br.readLine());
			if (v) System.out.print(process + " uses random number: "+ (long) rand + ", ");
			double rp = rand % numOfFrames;
			removedPage = (int) rp;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return removedPage;
	}
}
