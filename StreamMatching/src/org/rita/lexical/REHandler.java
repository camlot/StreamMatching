package org.rita.lexical;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
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
		case '.': return 2;
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
		case '.': return 3;
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
		sb.append(RegularExpression.charAt(RegularExpression.length()-1));
		sb.append('#');
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
			//String startState = "", tmpState = "";
			try{
				if(op == '*')
				{
					return createClosure(data1);
				}
				else if(op == '.') {
					return createAnd(data1, data2);
				}
				else if(op == '|'){ 
					return createOr(data1, data2);
				}
				else
				{
					throw new Exception("Oprator is not legal!");
				}
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
		return "";
	}
	
	private String createClosure(String data1)
	{
		String startState = "";
		
		if(data1.length() == 1)  // a*
		{
			/*
			 * Q->aQ|a|epsilon
			 */
			startState = createState();
			Rule r = new Rule(startState, data1, startState); // aQ
			r.setIsClousure(true);  // mark current rule is a closure
			//r.addRule(data1, "");  // a
			r.addRule("", "");  // epsilon
			ruleList.add(r);
		}
		else  // Q*
		{
			/*
			 * Q->aB|aQ|a|epsilon
			 * 
			 */
			for(Rule r : ruleList)
			{
				if(r.getLeft().equals(data1)){
					if(r.isClousure())
						return data1; // Q** = Q*
					// TODO
					else
					{
						/* 
						 * traverse all rule of Q
						 * a => aQ
						 * aQ' => expand Q' then add Q behind all the terminator
						 * add epsilon to Q
						 */
						//r = epsilonFree(r);
						r.setIsClousure(true);
						try{
							Rule root = getRuleByName(data1); // Q
							String[] s = new String[2];
							Queue<Rule> ruleQueue = new LinkedList<Rule>();
							ruleQueue.add(root);
							while(!ruleQueue.isEmpty())
							{
								Rule curRule = ruleQueue.poll();
								//if(curRule.isClousure()) // b*
								//{ continue; }
								// traverse current rule
								while(curRule.getNextRule(s))
								{
									if(s[0].equals(""))
									{
										curRule.deleteRule(); // delete epsilon rule
									}
									else if(s[1].equals(""))  // terminator
									{
										curRule.fixRule(data1);  // a => aQ
									}
									else
									{
										ruleQueue.add(getRuleByName(s[1]));
									}
								}
								ruleQueue.poll();
							}
							root.addRule("", "");
							startState = data1;
						}catch(Exception e)
						{
							System.out.println(e);
						}
						
					}
				}
			}
		}
		return startState;
	}
	
	private String createOr(String data1, String data2) throws Exception
	{
		String startState = "";
		if(data1.length() == 1 && data2.length() == 1)  // a|b
		{
			/*
			 * Q1->a|b
			 */
			startState = createState();
			Rule r = new Rule(startState, data1, "");
			r.addRule(data2, "");
			ruleList.add(r);
		}
		else if(data1.length() == 1 || data2.length() == 1) // a|Q1 or Q1|a
		{
			/*
			 * Q1|a
			 */
			if(data1.length() != 1) // exchange data1 and data2
			{
				String tmp = data1;
				data1 = data2;
				data2 = tmp;
			}

			/*
			 * Q1->...|a
			 */
			for(Rule r : ruleList)
			{
				if(r.getLeft().equals(data2))  // get Q1 from ruleList
				{
//					if(!r.isClousure()){
						r.addRule(data1, "");  // insert rule to Q1
						r.setIsClousure(false);
						startState = r.getLeft();
						break;
//					}
//					else{
//						/*
//						 * Q1->cQ1|xxx
//						 * ===>
//						 * Q2->Q1|a => Q2->cQ1|xxx|a
//						 */
//						Rule newr = r.clone();  // clone the old Rule
//						startState = createState();  // Q2
//						newr.setLeft(startState);
//						newr.addRule(data1, "");
//					}
				}
			}
		}
		else // Q1|Q2  // TODO
		{
			Rule r1 = null, r2 = null;
			// get r from ruleList
			for(Rule r : ruleList)
			{
				if(r.getLeft().equals(data1))
				{
					r1 = r;
				}
				else if(r.getLeft().equals(data2))
				{
					r2 = r;
				}
			}
			if(r1 == null || r2 == null)
			{
				throw new Exception("Can not find rule by name");
			}
			if(r1.existEpsilon())
			{
				
			}
//			if(r1.isClousure() && r2.isClousure()) // ()*|()*
//			{
//				
//			}
//			else if(r1.isClousure() || r2.isClousure())
//			{
//				
//			}
//			else{
//				
//			}
		}
		return startState;
	}
	
	private String createAnd(String data1, String data2)
	{
		String newState = "", startState = "";
		if(data1.length() == 1 && data2.length() == 1)  // a+b
		{
			/*
			 * Q2->aQ1
			 * Q1->b
			 */
			newState = createState();  // Q1
			startState = createState(); // Q2
			ruleList.add(new Rule(newState, data2, ""));
			ruleList.add(new Rule(startState, data1, newState));
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
			 * scan Q2 and add 'a' to the back of terminator
			 * if epsilon exist, change epsilon to 'a'
			 */
			try{
				Rule r = getRuleByName(data1);
				if (r.isClousure()) {
					// r.deleteRule(data1);  
					int pos = r.existEpsilon();
					r.fixRule(pos, data2, "");  // epsilon => terminator
					r.setIsClousure(false);
				}
				else{
					
				}
			}catch(Exception e)
			{
				System.out.println(e);
			}
			
		}
		else // Q1+Q2 //TODO
		{
			/*
			 * traverse Q1 then add Q2 behind all the terminator
			 */
			try{
				Rule root = getRuleByName(data1); // Q1
				String[] s = new String[2];
				Queue<Rule> ruleQueue = new LinkedList<Rule>();
				ruleQueue.add(root);
				while(!ruleQueue.isEmpty())
				{
					root = ruleQueue.poll();
					// traverse current rule
					while(root.getNextRule(s)){
						if(s[1].equals(""))  // terminator
						{
							root.fixRule(data2);
						}
						else
						{
							ruleQueue.add(getRuleByName(s[1]));
						}
					}
					ruleQueue.poll();
				}
				startState = data1;
			}catch(Exception e)
			{
				System.out.println(e);
			}
		}
		return startState;
	}
	
	
	// change current grammar into epsilon free
//	private Rule epsilonFree(Rule ruleName)
//	{
//			
//	}
	
	private Rule getRuleByName(String name) throws Exception
	{
		for(Rule r : ruleList)
		{
			if(r.getLeft().equals(name))
			{
				return r;
			}
		}
		throw new Exception("Cannot find rule by name");
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
		while(currentPos != re.length())
		{
			currentChar = re.charAt(currentPos); // get next char
			String tmp = "";
			tmp += currentChar;
			if(!isSymbol(currentChar)) // current char is terminator
			{
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
						OPNR.push(tmp); // push the symbol into OPNR stack
						++currentPos;
					}
					else if(value == 1) // op1 = op2
					{
						OPNR.pop();  // pop'('
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
	 * print RG was being translated by RE
	 */
	public void printAllRule()
	{
		createRules();
		for(int i=0; i < ruleList.size(); ++i)
		{
			Rule r = ruleList.get(i);
			System.out.print(r.getLeft()+"->");
			for(int j = 0; j < r.size(); ++j)
			{
				String [] s = new String[2];
				r.get(s, j);
				System.out.print(s[0]+s[1]+"|");
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
