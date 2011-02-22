package com.personalityextractor.entity;

import java.util.ArrayList;


public class WikipediaEntity extends Entity{

	String wikiminer_id;
	ArrayList<String> categories;
	
	public WikipediaEntity(String text) {
		super(text);
		wikiminer_id=null;
		categories = new ArrayList<String>();
	}
	
	public WikipediaEntity(String text, String wikiminer_id) {
		super(text);
		this.wikiminer_id=wikiminer_id;
		categories = new ArrayList<String>();
	}
	 
	public void addCategory(String text){
		categories.add(text);
	}
	
	public String getWikiminerID(){
		return wikiminer_id;
	}
	
	public void print(){
		System.out.println("Entity: "+this.text);
		System.out.println("wikiminer_id: "+this.wikiminer_id);
		System.out.println("Categories: "+this.categories);
	}
	
	
}
