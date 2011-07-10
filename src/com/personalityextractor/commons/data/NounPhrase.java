package com.personalityextractor.commons.data;


public class NounPhrase {
	private String text;
	private String type;

	public NounPhrase(String text, String type){
		this.text=text;
		this.type=type; //for Proper Nouns type= PN; common Nouns type=CN 
	}
	
	public String getText(){
		return this.text;
	}
	public String getType(){
		return this.type;
	}

	@Override
	public String toString() {
		return this.text+":"+this.type;
	}
	
	
	
	
}
