package banker;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/*
 * Implementation of FIFO algorithm.
 */
public class FIFO {
	Reader re;
	int[] curResUse; //situation of resources for current cycle
	boolean v; //verbose or not
	
	public FIFO(String fn, boolean verbose) throws IOException{
		re = new Reader(fn);
		curResUse = new int[re.general[1]];
		for (int i=0; i<re.general.length-2; i++){
			curResUse[i] = re.general[i+2];
		}
		v = verbose;
		process();
	}
	
	public void process(){
		LinkedList<Integer> blockList = new LinkedList<Integer>();
		boolean processing = true;
		int count = 0;
		int [] nextResUse = curResUse.clone(); //situation of resources for next cycle
		int terminateNum = 0;
		int abortNum = 0;
		if (v == true){
			System.out.println("Verbose information for FIFO algorithm:");
			System.out.printf("During cycle %d-%d each task complete its initiate.\n",count, count+re.general[1]);
			System.out.println("The manager have: "+ Arrays.toString(curResUse));
			System.out.println();
		}
		count += re.general[1];
		while (processing){
			if (v == true) System.out.printf("During cycle %d-%d:\n", count, count+1);
			
			// for new release and new request
			for (int i=0; i<re.general[0]; i++){ //i+1 is task number
				Task curTask = re.tasks.get(i+1);
				
				if (curTask.curTime >= curTask.timeLine.size() && curTask.terminate == false){
					curTask.terminate = true;
					curTask.usedTime = count;
					terminateNum += 1;
					if (v == true) System.out.printf("Task %d terminates.\n", curTask.tNumber);
				}
				
				if (curTask.terminate == false){					
					if (curTask.blocked == false){						
						int[] curTimeLine = curTask.timeLine.get(curTask.curTime);
						
						if (curTimeLine[1] == 0){
							curTask.curTime += 1;
							if (v == true) System.out.printf("Task %d is doing compute.\n",curTask.tNumber);
						}
						else{
							//release
							if (curTimeLine[1] > 0){
								nextResUse[curTimeLine[0]-1] += curTimeLine[1];
								curTask.curHold[curTimeLine[0]-1] -= curTimeLine[1];
								curTask.curTime += 1;
								if (v == true) System.out.printf("Task %d released %d unit of resource %d.\n", 
										curTask.tNumber, curTimeLine[1], curTimeLine[0]);			
							}	
							
							//fake put into blockList, probably will be removed later in this cycle
							else{
								blockList.add(curTask.tNumber);
								curTask.blocked = true;
							}
						}
					}
				}
			}
			
			//Go through the block list, deal with requests
			if (blockList.size()>0){
				int taskNum = 0;
				while (taskNum < blockList.size()){
					Task curTask = re.tasks.get(blockList.get(taskNum));
					int[] curTimeLine = curTask.timeLine.get(curTask.curTime);
					
					if (curResUse[curTimeLine[0]-1] + curTimeLine[1] >= 0){
						curResUse[curTimeLine[0]-1] += curTimeLine[1];
						nextResUse[curTimeLine[0]-1] += curTimeLine[1];
						curTask.curHold[curTimeLine[0]-1] -= curTimeLine[1];
						curTask.curTime += 1;
						blockList.remove(blockList.indexOf(curTask.tNumber));
						curTask.blocked = false;
						if (v == true) System.out.printf("Task %d's request for %d unit of resource %d is granted.\n",
								curTask.tNumber, Math.abs(curTimeLine[1]), curTimeLine[0]);
					}
					else{
						taskNum += 1;
						curTask.blockTime += 1;
						if (v == true) System.out.printf("Task %d's request for %d unit of resource %d cannot be granted, it is blocked.\n", 
								curTask.tNumber, Math.abs(curTimeLine[1]), curTimeLine[0]);
					}
				}
			}
			
			// If deadlocked
			if (blockList.size() == re.general[0] - terminateNum - abortNum && (blockList.size() != 0)){
				int abort = 1; //which process to abort
				boolean deadlocked = true;
				while (deadlocked){
					Task curTask = re.tasks.get(abort);
					if (curTask.terminate != true){
						//abort a process;
						curTask.aborted = true;
						curTask.terminate = true;
						abortNum += 1;
						for (int i=0; i<re.general[1]; i++){					
							nextResUse[i] += curTask.curHold[i];
							curTask.curHold[i] = 0;
						}
						int rmIndex = blockList.indexOf(curTask.tNumber);
						if(rmIndex != -1) {
							blockList.remove(rmIndex);
						}
						if (v == true) System.out.printf("Task %d is aborted.\n", curTask.tNumber);
						//check if we need to abort more process;
						for (int i=0; i<blockList.size(); i++){
							Task checkTask = re.tasks.get(blockList.get(i));
							int[] checkTimeLine = checkTask.timeLine.get(checkTask.curTime);
							if (nextResUse[checkTimeLine[0]-1] + checkTimeLine[1] >= 0){
								deadlocked = false;
								break;
							}
						}	
						//start next cycle or keep abort more process;
						if (deadlocked == false){
							break;
						}
						abort += 1;
					}
					else{
						abort += 1;
					}
				}				
			}
			if (v == true) System.out.printf("Manager now have: %s\n\n", Arrays.toString(nextResUse));
			curResUse = nextResUse.clone();
			count += 1;
			if (re.general[0] - terminateNum - abortNum == 0){
				processing = false;
				break;
			}
		}
	}
}
