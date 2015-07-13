package org.rita.lexical;

import java.util.ArrayList;
import java.util.Stack;

//import com.sun.java.util.*;


public class REHandler {
	private String RegularExpression;
	private ArrayList<Rule> ruleList;
	private static int stateNum;
	// constructed
	public REHandler(String RE)
	{
		this.RegularExpression = RE;
		this.stateNum = 0;
	}
	
	/*
	 * check the Regular Expression is legal or not
	 */
	public boolean checkRE()
	{
		return true;
	}
	
	/*
	 * op1<op2 return 0;
	 * op1=op2 return 1;
	 * op1>op2 return 2;
	 */
	private int compare(char op1, char op2) throws Exception
	{
		// some syntactic error of RE
		if(op1 == '(' && op2 == '#' || op1 == ')' && op2 == '(')
		{
			throw new Exception("The left brackets is not match!");
		}
		else if(op1 == '#' && op2 == ')')
		{
			throw new Exception("The right brackets is not match!");
		}
		else if(op1 == '*' && op2 == '*')
		{
			throw new Exception("Syntactic error cause by series Star!");
		}
		else
		{
			try{
				// compare the priority of the char in the stack and current reading
				if(getISP(op1) < getCSP(op2)) return 0;
				else if(getISP(op1) > getCSP(op2)) return 2;
				if(getISP(op1) == getCSP(op2)) return 1;
			}catch(Exception e){
				throw e;
			}
		}
		return -1; // error
	}
	
	/*
	 * the priority of the symbol on the top of OPNR
	 */
	private int getISP(char op) throws Exception
	{
		switch(op)
		{
		case '#': return 0;
		case '(': return 1;
		case '*': return 7;
		case '|': return 4;
		case '+': return 2;
		case ')': return 8;
		}
		throw new Exception("It's not a verified char!");
	}
	
	/*
	 * the priority of the symbol current read
	 * 
	 */
	private int getCSP(char op) throws Exception
	{
		switch(op)
		{
		case '#': return 0;
		case '(': return 8;
		case '*': return 7;
		case '|': return 5;
		case '+': return 3;
		case ')': return 1;
		}
		throw new Exception("It's not a verified char!");
	}
	
	// a*bd|c=>a*.b.d|c
	private String addJoinOP()
	{
		char left,right;
		StringBuffer sb = new StringBuffer();  //  store new string add join symbol
		for(int i=0; i<RegularExpression.length()-1; ++i)
		{
			left = RegularExpression.charAt(i);
			right = RegularExpression.charAt(i+1);
			sb.append(left);
			// NOT (right  )left  |left&right .left&right
			if(left != '(' && left != '|' && right != '|' && right != ')' && right != '*' && left != '.' && right != '.')
			{
				sb.append('.');
			}			
		}
		sb.append(RegularExpression.charAt(RegularExpression.length()-1)+'#');
		String result = sb.toString();
		return result;
	}
	
	// check a char whether is a symbol or not
	private boolean isSymbol(char op)
	{
		if(op == '.' || op == '*' || op == '|' || op == '(' || op == ')' || op =='#')
			return true;
		else
		{
			return false;
		}
	}
	
	/*
	 * create new state Qn
	 */
	private static String createState()
	{
		String state = "Q"+stateNum++;
		return state;
	}
	
	
	/* 
	 * create a rule data1 op data2
	 * if op='*' use rule data1*
	 */
	private String createRule(char op, String data1, String data2) throws Exception 
	{
		if(!isSymbol(op))
		{
			throw new Exception("Oprator is not legal!");
		}
		else
		{
			String startState = "", tmpState = "";
			if(op == '*')
			{
				if(data1.length() == 1)  // a*
				{
					/*
					 * Q1->aQ1|ε
					 */
					startState = createState();
					Rule r = new Rule(startState, data1, startState); // aQ1
					r.addRule("", "");  // ε
					ruleList.add(r);
				}
				else  // Q1* //TODO
				{
					
				}
			}
			else if(op == '.') {
				if(data1.length() == 1 && data2.length() == 1)  // a+b
				{
					/*
					 * Q2->aQ1
					 * Q1->b
					 */
					tmpState = createState();  // Q1
					startState = createState(); // Q2
					ruleList.add(new Rule(tmpState, data2, ""));
					ruleList.add(new Rule(startState, data1, tmpState));
					
				}
				else if(data1.length() == 1)  // a+Q1
				{
					/*
					 * Q2->aQ1
					 */
					startState = createState();
					ruleList.add(new Rule(startState, data1, data2));
				}
				else if(data2.length() == 1) // Q1+a //TODO
				{
					/*
					 * Q2->Q1a
					 */
					
				}
				else // Q1+Q2 //TODO
				{
					/*
					 * 
					 */
				}
			}
			else if(op == '|'){ 
				if(data1.length() == 1 && data2.length() == 1)  // a|b //TODO
				{
					
				}
				else if(data1.length() == 1)  // a|Q1
				{
					/*
					 * Q1->...|a
					 */
					for(Rule r : ruleList)
					{
						if(r.getLeft().equals(data2))
						{
							r.addRule(data1, "");  // insert rule to Q1
							startState = r.getLeft();
							break;
						}
					}
				}
				else if(data2.length() == 1)  // Q1|a
				{
					/*
					 * Q1->...|a
					 */
					for(Rule r : ruleList)
					{
						if(r.getLeft().equals(data1))
						{
							r.addRule(data2, "");  // insert rule to Q1
							startState = r.getLeft();
							break;
						}
					}
				}
				else // Q1|Q2 //TODO
				{
					
				}
			}
			return startState;
		}

	}
	
	// Trans the RE into RG
	private void createRules()
	{
		Stack<String> OPNR = new Stack<String>(); // Symbol Stack
		Stack<String> OPND = new Stack<String>(); // Data Stack
		OPNR.push("#");  // put a # on the top of the stack
		String re = addJoinOP();
		char currentChar = '\0';
		int currentPos = 0;
		while(currentPos != re.length());
		{
			currentChar = re.charAt(currentPos); // get next char
			if(!isSymbol(currentChar)) // current char is terminator
			{
				String tmp = "";
				tmp += currentChar;
				OPND.push(tmp); // push the char into OPND stack
				++currentPos;
			}
			else  // current char is symbol
			{
				String s = OPNR.peek();
				char op1 = s.charAt(0);
				try{
					int value = compare(op1, currentChar);
					if(value == 0) // op1 < op2
					{
						OPND.push(s); // push the char into OPND stack
						++currentPos;
					}
					else if(value == 1) // op1 = op2
					{
						OPND.pop();  // pop'('
						++currentPos;
					}
					else if(value == 2) // op1 > op2
					{
						if(op1 == '*') // pop one data
						{
							OPND.push(createRule(op1, OPND.pop(),""));
						}
						else // pop two data
						{
							String right = OPND.pop();
							OPND.push(createRule(op1, OPND.pop(), right));
						}
					}
					else{
						System.out.println("undefined compare error!");
					}
				}catch(Exception e){
					System.out.println(e);
				}
			}
		}
		
	}
	
	/*
	 * use rules to create a NFA
	 * doing after rules have been created
	 */
	private void createNFA() throws Exception  //TODO
	{
		if(ruleList.isEmpty())
		{
			throw new Exception("Pls create rule first!");
		}
		
	}
}