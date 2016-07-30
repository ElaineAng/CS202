package banker;

import java.io.IOException;

/*
 *  Contains the main method that runs the two algorithm.
 */
public class Manage {
	public static void main(String[] args){
		boolean verbose = false;
		String fn = "";
		if (args.length == 2){
			if (args[0].equals("--v") || args[0].equals("--verbose")){
				verbose = true;
				fn = args[1];
			}		
		}
		else fn = args[0];
		
		try {
			FIFO fifo = new FIFO(fn, verbose);
			System.out.println("FIFO:");
			int fifoTotalUsed = 0;
			int fifoTotalBlock = 0;
			for (int i=0; i<fifo.re.general[0]; i++){
				Task curTask = fifo.re.tasks.get(i+1);
				if (curTask.aborted == false){
					System.out.printf("Task%d\t%d\t%d\t%1.1f%%\n", i+1, curTask.usedTime, curTask.blockTime, 
							curTask.blockTime*100/(double)curTask.usedTime);
					fifoTotalUsed += curTask.usedTime;
					fifoTotalBlock += curTask.blockTime;
				}
				else{
					System.out.printf("Task%d\tAborted\n", i+1);
				}			
			}
			System.out.printf("Total\t%d\t%d\t%1.1f%%\n", fifoTotalUsed, fifoTotalBlock,
					fifoTotalBlock * 100/(double)fifoTotalUsed);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		System.out.println();
		try {
			Banker banker = new Banker(fn, verbose);
			System.out.println("Banker: ");
			int bankerTotalUsed = 0;
			int bankerTotalBlock = 0;
			for (int i=0; i<banker.re.general[0]; i++){
				Task curTask = banker.re.tasks.get(i+1);
				if (curTask.aborted == false){
					System.out.printf("Task%d\t%d\t%d\t%1.1f%%\n", i+1, curTask.usedTime, curTask.blockTime, 
							curTask.blockTime*100/(double)curTask.usedTime);
					bankerTotalUsed += curTask.usedTime;
					bankerTotalBlock += curTask.blockTime;
				}
				else{
					System.out.printf("Task%d\tAborted\n", i+1);
				}			
			}
			System.out.printf("Total\t%d\t%d\t%1.1f%%\n", bankerTotalUsed, bankerTotalBlock,
					bankerTotalBlock * 100/(double)bankerTotalUsed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
