package paging;

public class Process {
	int cr;		//current reference;
	int ps;		//process size i.e. number of pages;
	int num; 	//process number;
	int pfn; 	//number of page faults;
	int rs;		//running sum;
	int es;		//eviction sum; how many times a page been evicted
	int[] et;	//eviction time; for recording and adding to rs;
	int[] pif;	//page in frame. index is the page number, corresponding value is the frame number.
				//-1 indicates that the page does not exist in frame.
	
	boolean hasEvict;
	public Process(int processNum, int pss){
		cr = 0;
		ps = pss;
		num = processNum;
		pfn = 0;
		rs = 0;
		es = 0;
		pif = new int[ps];
		et = new int[ps];
		for (int i=0; i<ps; i++){
			pif[i] = -1;
			et[i] = 0;
		}
		hasEvict = false;
	}
}
