package linker;

import java.util.ArrayList;

public class Module {
	public ArrayList<String> definitionList;
	public ArrayList<String> useList;
	public ArrayList<Integer> useAddr; // address been used
	public ArrayList<String> programText;
	public int baseAddr;
	
	public Module() {
		definitionList = new ArrayList<String>();
		useList = new ArrayList<String>();
		programText = new ArrayList<String>();
		useAddr = new ArrayList<Integer>();
		baseAddr = 0;
	}
	
	public void update(int index, String newPt){
		programText.set(index, newPt);
	}
}
