package paging;

public class Simulation {
	public static void main(String[] args){
		int machineSize = Integer.parseInt(args[0]);
		int pageSize = Integer.parseInt(args[1]);
		int processSize = Integer.parseInt(args[2]);
		int jobMix = Integer.parseInt(args[3]);
		int refNum = Integer.parseInt(args[4]);
		String algorithm = args[5];
		boolean verbose = false;
		if (args.length > 6 && args[6].equals("--v")){
			verbose = true;
		}

		int frameNum = machineSize / pageSize;
		System.out.printf("The machine size is %d.\n", machineSize);
		System.out.printf("The page size is %d.\n", pageSize);
		System.out.printf("The process size is %d.\n", processSize);
		System.out.printf("The job mix number is %d.\n", jobMix);
		System.out.printf("The number of references per process is %d.\n", refNum);
		System.out.printf("The replacement algorithm is %s.\n\n", algorithm);
		
		if (algorithm.toLowerCase().equals("lifo")){
			new LIFO(frameNum, pageSize, processSize, jobMix, refNum, verbose);
		}
		
		if (algorithm.toLowerCase().equals("random")){
			new Rand(frameNum, pageSize, processSize, jobMix, refNum, verbose);
		}
		if (algorithm.toLowerCase().equals("lru")){
			new LRU(frameNum, pageSize, processSize, jobMix, refNum, verbose);
		}	
	}
}
