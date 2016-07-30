package banker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/*
 * Implementation of Banker's algorithm.
 */
public class Banker {
	Reader re;
	int[] curResUse; //situation of resources for current cycle
	boolean v; //verbose or not
	
	public Banker(String fn, boolean verbose) throws IOException{
		re = new Reader(fn);
		curResUse = new int[re.general[1]];
		for (int i=0; i<re.general.length-2; i++){
			curResUse[i] = re.general[i+2];
		}
		v = verbose;
		process();
	}
	
	/*
	 * isSafe is a recursive function which takes a task that requests certain resources and return a boolean value
	 * return true for safe and return false for unsafe
	 */
	public boolean isSafe(Task reqtask, int[] req, ArrayList<Integer> remainTask, int[] resUse){		
		//grant the request!
		for (int i=0; i<req.length; i++){				
			if (resUse[i] + req[i] < 0) return false;
			resUse[i] += req[i];
			reqtask.recHold[i] -= req[i];
		}

		//check safety
		boolean safe = false;
		
		//check if the task could meet its initial claim if the request is granted
		boolean release = true;
		for (int i=0; i<req.length; i++){
			if (reqtask.recHold[i] != reqtask.initial[i]){
				release = false;
				break;
			}
		}
		// release the resource if the above state is true, because this task is terminate
		if (release){
			for (int i=0; i<req.length; i++){
				resUse[i] += reqtask.recHold[i];
			}
			remainTask.remove(remainTask.indexOf(reqtask.tNumber));
		}
				
		if (remainTask.size() == 0){
			//done with all tasks, successfully find a process where all tasks can terminate normally
			return true;
		}
		else{
			//check all possible following options
			for (int i: remainTask){
				reqtask = re.tasks.get(i);		
				int[] newReq = new int[req.length];
				
				for (int j=0; j<req.length; j++){
					newReq[j] = reqtask.recHold[j] - reqtask.initial[j];
				}
				safe |= isSafe(reqtask, newReq, remainTask, resUse); //it will be safe as long as any branch is safe
				if (safe) return true;			
			}
			return safe;
		}					
	}
	
	public void process(){
		LinkedList<Integer> blockList = new LinkedList<Integer>();
		boolean processing = true;
		int count = 0;
		int [] nextResUse = curResUse.clone(); //situation of resources for next cycle
		int terminateNum = 0;
		int abortNum = 0;
		if (v == true){
			System.out.println();
			System.out.println("Verbose information for Banker's algorithm:");
			System.out.printf("During cycle %d-%d each task complete its initiate.\n",count, count+re.general[1]);
			System.out.println("The manager have: "+ Arrays.toString(curResUse));
			System.out.println();
		}
		
		//Check if any tasks initial claim exceeds the maximum resources available
		for (int i=0; i<re.general[0]; i++){
			Task curTask = re.tasks.get(i+1);
			for (int j=0; j<re.general[1]; j++){
				if (curTask.initial[j] > curResUse[j]){
					curTask.aborted = true;
					curTask.terminate = true;
					abortNum += 1;
					System.out.printf("Task %d is aborted before run begins. ", curTask.tNumber);
					System.out.printf("It claims %d units for resource %d, but we only have %d unit.\n",
							curTask.initial[j], j+1, curResUse[j]);
				}
			}
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
							if (curTimeLine[1] > 0){
								nextResUse[curTimeLine[0]-1] += curTimeLine[1];
								curTask.curHold[curTimeLine[0]-1] -= curTimeLine[1];
								curTask.curTime += 1;
								if (v == true) System.out.printf("Task %d released %d unit of resource %d.\n", 
										curTask.tNumber, curTimeLine[1], curTimeLine[0]);			
							}	
							else{
								//if request exceeds its initial claim, abort the task and release all its resources
								if (Math.abs(curTimeLine[1]) + curTask.curHold[curTimeLine[0]-1] 
										> curTask.initial[curTimeLine[0]-1]){
									curTask.terminate = true;
									curTask.aborted = true;
									abortNum += 1;
									for (int j=0; j<re.general[1]; j++){					
										nextResUse[j] += curTask.curHold[j];
										curTask.curHold[j] = 0;
									}		
									if (v == true) System.out.printf("Task %d is aborted.\n", curTask.tNumber);
								}
								
								//potential legal requests, fake put into blockList, do nothing else for now
								//probably will be removed later in this cycle when loop through the blockList
								else{
									blockList.add(curTask.tNumber);
									curTask.blocked = true;
								}
							}
						}
					}
				}
			}
			
			//Go through the blockList, deal with requests
			//ONLY actually deals with requests here
			if (blockList.size()>0){
				int taskNum = 0;
				while (taskNum < blockList.size()){
					Task curTask = re.tasks.get(blockList.get(taskNum));
					int[] curTimeLine = curTask.timeLine.get(curTask.curTime);
					ArrayList<Integer> remainTask = new ArrayList<Integer>();
					
					//Get all other tasks that are neither aborted nor terminated
					for (int i=0; i<re.general[0]; i++){
						if (re.tasks.get(i+1).aborted == false && re.tasks.get(i+1).terminate == false){
							remainTask.add(i+1);
						}
					}
					
					int [] req = new int[re.general[1]];
					req[curTimeLine[0]-1] = curTimeLine[1];
					
					for (int t: remainTask){
						re.tasks.get(t).recHold = re.tasks.get(t).curHold.clone();
					}
					
					// whether or not to grant the request based on whether or not it is safe
					if (isSafe(curTask, req, remainTask, curResUse.clone())){
						//safe
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
						//not safe
						taskNum += 1;
						curTask.blockTime += 1;
						if (v == true) System.out.printf("Task %d's request for %d unit of resource %d cannot be granted, it is blocked.\n", 
								curTask.tNumber, Math.abs(curTimeLine[1]), curTimeLine[0]);
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
