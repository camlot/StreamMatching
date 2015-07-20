package org.rita.lexical;
import java.util.ArrayList;

public class StreamMatching {
	private ArrayList<State> stateSet;  // ״̬��
	
	public static void main(String[] args)
	{
		String RE = "ab*c";
		REHandler rehandler = new REHandler(RE);
		rehandler.printAllRule();
	}
}
