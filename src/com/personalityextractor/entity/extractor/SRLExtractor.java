package com.personalityextractor.entity.extractor;

import java.util.ArrayList;
import java.util.HashMap;

import senna.Senna;
import senna.Verb;

public class SRLExtractor{
	
	/*
	 * returns a ArrayList of lists containing Noun phrases in the arguments of each verb in the line
	 */
	public ArrayList<ArrayList<String>> extract(String line) {
		ArrayList<ArrayList<String>> entities = new ArrayList<ArrayList<String>>();
		Senna senna = new Senna();
		String allLines = senna.getSennaOutput(line);
		HashMap<String, Verb> verbArgs = senna.parseSennaLines(allLines, line);
		
		for(String s : verbArgs.keySet()){
			Verb verb = verbArgs.get(s);
			ArrayList<String> argEntities = new ArrayList<String>();
			for(String arg : verb.argumentToText.keySet()){
				argEntities.add(verb.argumentToText.get(arg));
			}
			entities.add(argEntities);
		}
		return entities;
	}	
	
	public static void main(String args[]){
		SRLExtractor srl = new SRLExtractor();
		String line = "Swapped the Elantra with a Santa Fe to deal with all that snow on the roads";
		ArrayList<ArrayList<String>> entities = srl.extract(line);
		for(ArrayList<String> arr : entities){
			System.out.println("Args for verb");
			for(String s : arr){
				System.out.println(s);
			}
		}
	}

}
