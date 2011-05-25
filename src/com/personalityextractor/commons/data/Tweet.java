package com.personalityextractor.commons.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Tweet {
	
	private String text;
	private List<String> sentences =null;
	private List<String> links =null;
	private List<String> hashTags =null;
	
	public Tweet(String text){
		this.text = text;
		tokenize();
	}
	
	public boolean isNewsArticle(String text){
		String[] split = text.split("\\s+");
		double numCaps =0.0;
		Pattern p = Pattern.compile("^[A-Z]+.*");
		for(String token : split){
			if(p.matcher(token).matches()){
				numCaps++;
			}
		}
		if((numCaps/((double) split.length)) > 0.5)
				return true;
		
		return false;
	}
	
	private void tokenize(){
		String[] tokens = this.text.split("\\s+");
		StringBuffer plainText = new StringBuffer();
		//Pattern p = Pattern.compile("^(http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
		//Pattern p = Pattern.compile("http://[a-z.0-9+/%&#~\\\\-_]");
		
		for(String token : tokens){
			if(token.equalsIgnoreCase("rt") || token.startsWith("@")){
				continue;
			}
			
			if(token.startsWith("http://") || token.endsWith(".com")){
				if(this.links==null)
					this.links= new ArrayList<String>();
				links.add(token);
				continue;
			}
			plainText.append(token+" ");
		}
		
		String[] sentences = plainText.toString().trim().split("[:;\"?/><,\\.!@#%^()\\-+=~`{}|]+");
		this.sentences = new ArrayList<String>();
		for(String sentence : sentences){
			if((sentence=sentence.trim()).length()!=0){
				if(isNewsArticle(sentence))
					sentence= sentence.toLowerCase();
				this.sentences.add(sentence);
			}
		}
	}
	
	public List<String> getLinks(){
		return this.links;
	}
	
	public List<String> getSentences(){
		return this.sentences;
	}
	
	public static void main(String[] args){
		String text = "Are you a do-good geek? @EFF is hiring a \"Technology Generalist\": http://bit.ly/jeb8x7";
		Tweet t = new Tweet(text);
		System.out.println(t.sentences);
	}

}
