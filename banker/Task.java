package banker;

import java.util.ArrayList;

public class Task{
	int tNumber; 	//task number
	int[] initial; 	//initial claim;
	int curTime;	//which index of timeLine this task will be at for next cycle;
	int[] curHold; 	//current #of resources (corresponding to index) hold
	int[] recHold; 	//current #of resources hold for recursion and changing value in Banker
	
	ArrayList<int[]> timeLine; 	//timeLine of the request/release/compute operate, 
								//ArrayList of integer array of length 2. 
								//[x,y], where x is the resource type, and y is #of this resources request/release
								//y>0 for release; y<0 for request; x=0 and y=0 for compute
								//will be initiated in the Reader class, run Reader class's unit test to see an example of timeLine
	boolean terminate;
	boolean blocked;
	boolean aborted;
	
	int usedTime;
	int blockTime;
	
	public Task(int t, int res){
		tNumber = t;
		initial = new int[res];
		curTime = 0;
		curHold = new int[res];
		recHold = new int[res];
		terminate = false;
		blocked = false;
		aborted = false;
		usedTime = 0;
		blockTime = 0;
	}	
}
