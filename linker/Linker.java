package linker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import linker.Reader;
import linker.Module;

public class Linker {
	public static void main(String[] args){
		String fileName = args[0];
		Map<String, String> tlError = new HashMap<String, String>(); // map a useList relative address to an error message
		Reader reader = new Reader();
		int modCount = 0;
		ArrayList<String> newlyAdded = new ArrayList<String>();
		
		try {
			reader = new Reader(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		// update the symbolTable to make sure there is no definition exceeds module size
		for (Module m : reader.modules){
			
			for (int i=1; i<m.definitionList.size(); i++){
				String curEle = m.definitionList.get(i); // get current element in definitionList
				int moduleSize = Integer.parseInt(m.programText.get(0));
				if (curEle.matches("\\d+")){
					if (Integer.parseInt(curEle) > moduleSize){
						String symbol = m.definitionList.get(i-1); //symbol always 1 index before digit
						tlError.put(symbol, "Definition exceeds module size, last word in module used.");
						reader.symbolTable.put(symbol, m.baseAddr+moduleSize);						
					}
				}
			}
		}
		
		// second pass starts	
		for (Module m: reader.modules){
			String curSym = "";
			for (int i=1; i<m.useList.size(); i++){
				String curEle = m.useList.get(i); // could be a symbol or an address
				
				if (curEle.matches("\\d+") && curEle!="-1"){
					int addr = Integer.parseInt(curEle)+1; // relative address to be resolved or relocated
					if (addr >= m.programText.size()){
						tlError.put("afterMap", 
								String.format("Error: Use of %s in module %d "
										+ "exceeds the size of the module, "
										+ "use ignored \n", curSym, modCount));
					}
					else{
						
						if (m.useAddr.contains(addr)){
							tlError.put(curEle+"_"+String.valueOf(modCount), 
									"Error: Multiple symbols are listed in the same instruction, use the last usage");
						}
						else{
							m.useAddr.add(addr);
						}
						
						String pt = m.programText.get(addr);
						int newAddr = 0;
						int symAddr = reader.symbolTable.get(curSym);

						if (pt.charAt(4) == '4'){
							newAddr = symAddr;  // resolved
							String newStrAddr = String.valueOf(newAddr+1000).substring(1,4);
							m.update(addr, pt.substring(0, 1)+newStrAddr+pt.substring(4, 5));
						}					
					}				
				}
				else if (curEle.matches("[a-zA-Z0-9]+") && curEle!="-1"){
					curSym = curEle; // current symbol that we are dealing with
					if (!reader.defined.contains(curEle)){
						int n = i+1;
						while (Integer.parseInt(m.useList.get(n))!=-1){
							tlError.put(m.useList.get(n)+"_"+String.valueOf(modCount), 
									String.format("Error: the symbol %s is used but not defined, "
											+ "use the value 111", curEle));
							n += 1;
						}
						
						newlyAdded.add(curEle);
						reader.symbolTable.put(curEle, 111); // add the current element into symbolTable
					}	
				}
			}
			
			for (int i=1;i<m.programText.size();i++){
				String pt = m.programText.get(i);
				int oldAddr = Integer.parseInt(pt.substring(1, 4));
				int newAddr = 0;
				
				if (pt.charAt(4) == '3'){
					int moduleSize = Integer.parseInt(m.programText.get(0));
					if (oldAddr >= moduleSize){
						tlError.put(String.valueOf(i-1)+"_"+String.valueOf(modCount), 
								"Error: relative address exceeds the size of machine, largest module address used");
						newAddr = m.baseAddr + moduleSize - 1;
					}
					else{ 
						newAddr = m.baseAddr + oldAddr;//relocate
						}
					String newStrAddr = String.valueOf(newAddr+1000).substring(1, 4);
					m.update(i, pt.substring(0, 1)+newStrAddr+pt.substring(4, 5));						
				}
				if (pt.charAt(4) == '2'){
					if (oldAddr >= 300){
						tlError.put(String.valueOf(i-1)+"_"+String.valueOf(modCount), "Error: absolute address "
								+ "exceeds the size of machine, largest address used");
						newAddr = 299;
						m.update(i, pt.substring(0, 1)+String.valueOf(newAddr)+pt.substring(4, 5));
					}
				}  
			}
			modCount += 1;
		}
		
		String oldValue = "";
		for (String d : reader.defined){
			if (!reader.used.contains(d)){
				String errMessage = tlError.get("afterMap");
				if (errMessage!=null){
					oldValue = errMessage;
				}
				else{
					tlError.put("afterMap", oldValue);
				}
				String newValue = String.format("Warning: symbol %s was defined but never used\n", d);
				//System.out.println(newValue);
				tlError.put("afterMap", oldValue+newValue);
			}
		}
		// Second pass ends here
		
		int count = 0;
		int mod = 0; // count which module that we are dealing with
		
		// Print the symbol table
		System.out.println("Symbol Table");
		for (String symbol : reader.symbolTable.keySet()){
			if (!newlyAdded.contains(symbol)){
				System.out.print(symbol+"="+reader.symbolTable.get(symbol)+"  ");
				if (tlError.keySet().contains(symbol)){
					System.out.print(tlError.get(symbol));
				}
				if (reader.slError.keySet().contains(symbol)){
					System.out.print(reader.slError.get(symbol));
				}
				System.out.println();
			}		
		}
		System.out.println();
		
		// Print the memory map
		System.out.println("Memory Map");
		for (Module m : reader.modules){
			for (int i=1;i<m.programText.size();i++){
				System.out.print(count+": "+m.programText.get(i).substring(0, 4)+"  "); // memory address
				if (tlError.keySet().contains(i-1+"_"+String.valueOf(mod))){
					System.out.print(tlError.get(i-1+"_"+String.valueOf(mod))); // possible error message
				}
				System.out.println();
				count += 1;
			}
			mod += 1;
		}
		System.out.println();
		
		// Print the after map errors and warnings
		if (tlError.get("afterMap")!=null){
			System.out.println(tlError.get("afterMap"));
		}
		
	}	
}
