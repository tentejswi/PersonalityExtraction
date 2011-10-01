package com.personalityextractor.entity;

import java.util.ArrayList;

/*
 * represents an entity that can potentially indicate a user's interest  
 */

public class Entity {
	String text = null;
	double score = 0.0; 
	int count = 0;
	public ArrayList<String> tweets;
	
	public Entity(String text){
		text = text.replaceAll("'", "");
		this.text= text;
		tweets = new ArrayList<String>();
	}
	
	public String getText(){
		return this.text;
	}
	
	public void incrCount() {
		count++;
	}
	
	public int getCount() {
		return count;
	}
}
