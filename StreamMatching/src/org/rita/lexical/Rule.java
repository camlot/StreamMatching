package org.rita.lexical;

import java.util.ArrayList;

/*
 * left -> right1 | right2
 * left liner grammar
 * right1 terminator or NULL
 * right2 state or NULL
 */
public class Rule implements Cloneable{
	private String left;
	private ArrayList<String> right1;
	private ArrayList<String> right2;
	boolean closure;  // record whether the rule is translated by a closure or not
	int currentRule;  // the index of current rule
	
	public Rule(String left)
	{
		this.left = left;
		this.closure = false;
		this.currentRule = 0;
	}
	
	public Rule(String left, String right1, String right2)
	{
		this.left = left;
		this.right1.add(right1);
		this.right2.add(right2);
		this.closure = false;
		this.currentRule = 0;
	}
	
	 public Rule clone() {    
	        try {    
	            return (Rule)super.clone();    
	        } catch (CloneNotSupportedException e) {    
	            return null;    
	        }    
	    }    

	
	public void resetIndex()
	{
		this.currentRule = 0;
	}
	
	boolean isClousure()
	{
		return this.closure;
	}
	
	void setIsClousure(boolean isClosure)
	{
		this.closure = isClosure;
	}
	
	public void setLeft(String left)
	{
		this.left = left;
	}
	/* 
	 * get current state name
	 */
	public String getLeft()
	{
		return this.left;
	}
	
	// return current rule
	public boolean getNextRule(String[] s)
	{
		if(this.currentRule < right1.size()){
			s[0] = this.right1.get(this.currentRule);
			s[1] = this.right2.get(this.currentRule);
			return true;
		}
		this.resetIndex(); // currentRule = 0
		return false;
	}
	
	// add a rule for current state
	public void addRule(String right1, String right2)
	{
		this.right1.add(right1);
		this.right2.add(right2);
	}
	
	// change a rule
	public void fixRule(int pos, String right1, String right2)
	{
		this.right1.set(pos, right1);
		this.right2.set(pos, right2);
	}

	public void fixRule(String right2)
	{
		this.right2.set(currentRule, right2);
	}
	// check if current state has a epsilon symbol
	public int existEpsilon()
	{
		int pos = 0;
		for(String tmp : right1)
		{
			if(tmp.equals("")){
				if(right2.get(pos).equals(""))
					return pos;
			}
			++pos;
		}
		return -1;
	}
}
