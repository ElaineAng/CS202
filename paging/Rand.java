package paging;

import java.util.ArrayList;
import java.util.LinkedList;

public class Rand {
	int fn; 		//number of frames i.e. machine size/page size;
	int pas;		//page size;
	int ps; 		//process size;
	int jm;			//job mix;
	int rn;			//number of references per process;
	int tf;			//total number of fault.
	
	ArrayList<Process> processes;	//an arrayList of process;
	LinkedList<int[]> frames;	//int[0]: frame number;
								//int[1]: which process is using it;
								//int[2]: which page in that process;
	Reference ref;
	boolean v;
	public Rand(int frameNum, int pageSize, int processSize, int jobMix, int referenceNum, boolean verbose){
		fn = frameNum;
		pas = pageSize;
		ps = processSize;
		jm = jobMix; 
		rn = referenceNum;
		processes = new ArrayList<Process>();
		frames = new LinkedList<int[]>();
		v = verbose;
		ref = new Reference(jm, ps, v);
		
		run();
	}
	
	void run(){	
		if (jm == 1){
			Process curPro = new Process(1, ps/pas);
			curPro.cr = 111 % ps;
			processes.add(curPro);
			for (int i=0; i<rn; i++){
				sim(curPro.cr, curPro, i+1);
			}
		}
		else{
			for (int i=0; i<4; i++){
				Process curPro = new Process(i+1, ps/pas);
				curPro.cr = (111 * (i+1)) % ps;
				processes.add(curPro);			
			}
			
			int p=1;	//process
			int q=1;	//quantum
			for (int i=0; i<4*rn; i++){
				if (i <= 4*rn - 4 * (rn % 3)){
					if (q == 4){
						q = 1;
						p += 1;		
					}
					if (p == 5){
						p = 1;
					}
				}
				else{
					if (q == (rn % 3) + 1){
						q = 1;
						p += 1;
					}
				}
				Process curPro = processes.get(p-1);
				sim (curPro.cr, curPro, i+1);
				q += 1;			
			}		
		}
		int totalRunningSum = 0;
		int totalEvictSum = 0;
		System.out.println();
		for (int i=0; i<processes.size(); i++){
			Process curPro = processes.get(i);
			System.out.printf("Process %d had %d faults ", curPro.num, curPro.pfn);
			if (curPro.hasEvict){
				float ratio = (float)curPro.rs/(float)curPro.es;
				System.out.printf("and %f average residency.\n", ratio);
			}
			else{
				System.out.printf("with no evictions, the average residence is undefined.\n");
			}
			totalRunningSum += curPro.rs;
			totalEvictSum += curPro.es;			
		}
		System.out.printf("\nThe total number of faults is %d ", tf);
		
		if (totalEvictSum == 0){
			System.out.printf("with no evictions, the overall average residence is undefined.\n");
		}
		else{
			System.out.printf("and the overall average residency is %f.\n", 
					(float) totalRunningSum/totalEvictSum);
		}
	}
	
	void sim(int curRef, Process curPro, int time){
//		for (int i=0; i<frames.size(); i++){
//			System.out.println("{"+ Arrays.toString(frames.get(i))+"}");
//		}
		int curPage = curRef/pas;
		if (v) System.out.printf("%d references word %d (page %d) at time %d: ",curPro.num, curRef, curPage, time);
		int where = curPro.pif[curPage];
		if (where == -1){	//page fault
			tf += 1;
			curPro.pfn += 1;
			if (v) System.out.printf("Fault, ");
			int[] newFrame = new int[3];
			newFrame[1] = curPro.num;
			newFrame[2] = curPage;
			if (frames.size() == fn){	//does not have empty, require eviction
				int pageToRemove = ref.getRemovedPage(fn, curPro.num);
				int ind = 0;
				for (int i=0; i<frames.size(); i++){
					int[] f = frames.get(i);
					if (f[0] == pageToRemove){
						ind = i;
						break;
					}
				}
				int[] removed = frames.remove(ind); 	//remove the lru, use its frame number as the number for new frame,
				newFrame[0] = removed[0];		//add the new frame to the start of the frame queue	
							 
				Process removedProcess = processes.get(removed[1]-1);
				removedProcess.pif[removed[2]] = -1;
				removedProcess.es += 1;
				int rt = time - removedProcess.et[removed[2]];	//residence time
				removedProcess.rs += rt;
				removedProcess.hasEvict = true;
				
				curPro.et[curPage] = time;			
				if (v) System.out.printf("evicting page %d of process %d from frame %d\n",
						removed[2], removed[1], removed[0]);
			}
			else{	//has empty
				curPro.et[curPage] = time;
				newFrame[0] = fn-1-frames.size();
				if (v) System.out.printf("using free frame %d\n", newFrame[0]);
			}
			curPro.pif[curPage] = newFrame[0];
			frames.addLast(newFrame);							
		}
		else{	//page hit
			if (v) System.out.printf("Hit in frame %d\n", where);
		}
		int nextRef = ref.getNext(curPro.cr, curPro.num);
		curPro.cr = nextRef;
	}
}
