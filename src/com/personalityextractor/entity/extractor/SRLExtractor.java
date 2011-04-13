package com.personalityextractor.entity.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import senna.Senna;
import senna.Verb;

public class SRLExtractor{
	
	/*
	 * returns a ArrayList of lists containing Noun phrases in the arguments of each verb in the line
	 */
	public static List<List<String>> extract(String text) {
		List<List<String>> entities = new ArrayList<List<String>>();
		Senna senna = new Senna();
		String[] lines = text.split("[:;'\"?/><,\\.!@#$%^&()-+=~`{}|]+");
		for (String line : lines) {
			if((line=line.trim()).length()==0)
				continue;
			String allLines = senna.getSennaOutput(line);
			HashMap<String, Verb> verbArgs = senna.parseSennaLines(allLines, line);

			for (String s : verbArgs.keySet()) {
				Verb verb = verbArgs.get(s);
				List<String> argEntities = new ArrayList<String>();
				for (String arg : verb.argumentToNPs.keySet()) {
					for(String np : verb.argumentToNPs.get(arg)){
						argEntities.add(np);
					}
				 }
				 entities.add(argEntities);
			}
		}
		return entities;
	}	
	
	public static void main(String args[]){
		String line = "Spending 9$ on an album purchase is considered atrocious while paying 15$ for a Chilies dinner once a week - doesn't even warrant a mention!";
		List<List<String>> entities = extract(line);
		for(List<String> arr : entities){
			System.out.println("Args for verb");
			for(String s : arr){
				System.out.println(s);
			}
		}
	}

}
