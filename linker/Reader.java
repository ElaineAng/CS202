package linker;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import linker.Module;

public class Reader {	
	public String fileName;
	public Map<String, Integer> symbolTable;
	public ArrayList<Module> modules;
	public ArrayList<String> warnings;
	public Map<String, String> slError; //second level error
	public ArrayList<String> defined;
	public ArrayList<String> used; //symbols been used
	
	public Reader(){		
	}
	
	public Reader(String fn) throws IOException{
		fileName = fn;
		symbolTable = new HashMap<String, Integer>();
		modules = new ArrayList<Module>();
		warnings = new ArrayList<String>();
		slError = new HashMap<String, String>();
		defined = new ArrayList<String>();
		used = new ArrayList<String>();
		
		modules = processModules(fileToArray());
	}
	
	public String[] fileToArray() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
	    String line = br.readLine();
	    String infoString = "";
		
	    while (line != null) {
	    	line += " ";
	    	infoString += line;
	        line = br.readLine();
	    }
	    br.close(); 
	    
	    infoString = infoString.replaceAll("^\\s+",""); //get rid of the white space before first digit.
	    String[] infoList = infoString.split("\\s+");
	    
		return infoList;
	}
	
	public ArrayList<Module> processModules(String[] infoList){
		boolean newLine = true;
		boolean newModule = true;
		Module singleModule = new Module();
		int line = 0;
		int d = 0; // the number of definition in definitionList
		int n = 0; // the number of -1 in useList
		int t = 0; // the number of program text in programText
		int baseAddr = 0;
		
		for (int i=0;i<infoList.length;i++){
			
			if (newModule == true) {
				singleModule = new Module();
				singleModule.baseAddr += baseAddr;
				newModule = false;
				d = 0;
				n = 1;
				t = 0;
			}
			if (newLine == true) {
				newLine = false;
				if (line == 0) {
					singleModule.definitionList.add(infoList[i]);
					if (Integer.parseInt(infoList[i]) == 0){
						line += 1;
						newLine = true;
					}
				}
				else if (line == 1) {
					singleModule.useList.add(infoList[i]);
					if (Integer.parseInt(infoList[i]) == 0){
						line += 1;
						newLine = true;
					}
				}
				else if (line ==2) {
					singleModule.programText.add(infoList[i]);	
					if (Integer.parseInt(infoList[i]) == 0){
						line += 1;
						newLine = true;
					}
				}
			}
			else{
				if (line == 0){
					if (!infoList[i].matches("\\d+") && infoList[i].matches("[a-zA-Z0-9]+")){
						if (defined.contains(infoList[i])){
							slError.put(infoList[i], "This variable is multiply defined,"
									+ "last value used");
							symbolTable.put(infoList[i], baseAddr+Integer.parseInt(infoList[i+1]));
						}
						else{
							symbolTable.put(infoList[i], baseAddr+Integer.parseInt(infoList[i+1]));
							defined.add(infoList[i]);
						}
						
					}
					
					int dl = 2*Integer.parseInt(singleModule.definitionList.get(0));
					if (d < dl){
						d += 1;
						singleModule.definitionList.add(infoList[i]);	
						if (d == dl){
							newLine = true;
							line += 1;
						}					
					} // process definitionList; fixed number in this line depend on the first digit.
				}
				else{
					if (line == 1){
						int mo = Integer.parseInt(singleModule.useList.get(0)); // number of -1
						if (infoList[i].equals("-1")){
							n += 1;
							singleModule.useList.add(infoList[i]);
							if (n > mo){
								newLine = true;
								line += 1;
							}
						}
						else{
							if (mo >= n){
								singleModule.useList.add(infoList[i]);
								if (!infoList[i].matches("\\d+") && infoList[i].matches("[a-zA-Z0-9]+")){
									if (!used.contains(infoList[i])){
										used.add(infoList[i]);
									}								
								}
							}
						}
					} // process useList
					else {
						if (line == 2){
							int pt = Integer.parseInt(singleModule.programText.get(0)); // number of text
							if (pt > t){
								singleModule.programText.add(infoList[i]);
								t += 1;
								if (t == pt) {
									newLine = true;
									newModule = true;
									line = 0;
									baseAddr += pt;
								}
							}
						}// process programText
					}
				}											
			} // process whole single module
			if (newModule){
				modules.add(singleModule);
			}
		}		
		return modules;
	}
	
	//	unit test
	public static void main(String args[]){
		String fn = args[0];

		try {
			Reader m1 = new Reader(fn);		
			for (Module i : m1.modules){
				System.out.println(i.baseAddr);
				System.out.println(i.definitionList.toString());
				System.out.println(i.useList.toString());
				System.out.println(i.programText.toString());
				System.out.println();
			}
			System.out.println(m1.modules.size());
			for (String i: m1.symbolTable.keySet()){
				System.out.println(i + " " + m1.symbolTable.get(i));
			}
			for (String i: m1.defined){
				System.out.print(i+" ");
			}
			System.out.println();
			for (String i: m1.used){
				System.out.print(i+" ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


