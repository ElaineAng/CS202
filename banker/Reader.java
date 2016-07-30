package banker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * Reader takes a file path as input string, and construct a Reader class. 
 */
public class Reader {
	HashMap<Integer, Task> tasks;	//mapping a task number to a Task object
	String fn;		//file name
	int[] general; 	//0:#tasks; 1:#resources; 2-general.length:#each resource
	
	public Reader(String f) throws IOException{
		fn = f;
		constructTasks();
	}
	
	public ArrayList<String> fileToArray() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fn));
	    String line = br.readLine();
	    ArrayList<String> infoList = new ArrayList<String>();
		
	    while (line != null) {
	    	infoList.add(line);
	        line = br.readLine();
	    }
	    br.close(); 	    
		return infoList;
	}
	
	public void constructTasks() throws IOException{
		tasks = new HashMap<Integer, Task>();
		ArrayList<String> info = fileToArray();
		String[] gens = info.get(0).split("\\s+");
		general = new int[gens.length];
		for (int i=0; i<gens.length; i++){
			general[i] = Integer.parseInt(gens[i]);
		}
		int taskCount = general[0]; //how many task
		int resNum = general[1]; 	//how many resource
		
		//Initialize all new empty tasks
		for (int i=1; i<=taskCount; i++){
			Task newTask = new Task(i, resNum);
			tasks.put(i, newTask);
		}
		
		//Start processing
		for (String li: info.subList(1, info.size())){
			String[] line = li.split("\\s+");

			if (line.length > 1){
				String opt = line[0]; 					//all 5 possible option
				int tn = Integer.parseInt(line[1]); 	//task name
				int rtnc = Integer.parseInt(line[2]); 	//resource type | number of cycles for "compute"
				int rn = Integer.parseInt(line[3]); 	//the number of resources
				if (opt.equals("initiate")){
					//initiate
					tasks.get(tn).initial[rtnc-1] = rn;
				}
				else{
					if (tasks.get(tn).timeLine == null){
						tasks.get(tn).timeLine = new ArrayList<int[]>();
					}
					int[] curCycle = new int[2];
					
					if (opt.equals("terminate")){
						//do not add a cycle for terminate
					}	
					
					if (opt.equals("compute")){
						curCycle[0] = 0;
						curCycle[1] = 0;
						for (int i=0; i<rtnc; i++){			
							tasks.get(tn).timeLine.add(curCycle);
						}	//add n [0,0] to the timeLine representing n cycle of compute
					}
					if (opt.equals("request")){
						curCycle[0] = rtnc;
						curCycle[1] = 0 - rn;
						tasks.get(tn).timeLine.add(curCycle);
					}
					if (opt.equals("release")){
						curCycle[0] = rtnc;
						curCycle[1] = rn;
						tasks.get(tn).timeLine.add(curCycle);
					}
				}
			}
		}
	}
	
	//unit test
	public static void main(String[] args){
		try {
			Reader re = new Reader(args[0]);
			System.out.println(Arrays.toString(re.general));
			for (int i=1; i<=re.tasks.size(); i++){
				System.out.println("task "+i+":");
				ArrayList<int[]> curTimeLine = re.tasks.get(i).timeLine;
				for (int[] cycle : curTimeLine){
					System.out.println(Arrays.toString(cycle));
				}
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
