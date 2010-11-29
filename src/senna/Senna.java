package senna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Senna {
	
	String[] lineArr;
	

	/*
	 * Flatten Senna features to suit Maxent output
	 * Input: ArrayList<HashMap<String, Verb>> -  Arraylist containing for each sentence in the tweet a Hashmap of verbs inside it to the
	 * arguments of the verb stored in Verb class
	 * Ouput: List of strings
	 */
	public void flattenSennaFeatures(ArrayList<HashMap<String, Verb>> verbArgsinTweet){
		ArrayList<String> features = new ArrayList<String>();
		for(HashMap<String, Verb> verbArgsinSentence : verbArgsinTweet){
			for(String verbText : verbArgsinSentence.keySet()){
				
			}
		}
		
	}
	public HashMap<String, Verb>  parseSennaLines(String allText, String sentence){	
		lineArr = allText.split("\n");
		HashMap<String, Verb> verbsToArgs = new HashMap<String, Verb>();
		ArrayList<Verb> verbs = new ArrayList<Verb>();
		int verbCount = 0;
		for(int i=0; i < lineArr.length; i++) {
			//String[] line = lineArr[i].trim().split("\t");
			String line = lineArr[i].trim();
			lineArr[i] = line;
			Pattern p = Pattern.compile("VB[A-Z]?\t");
			Matcher  m = p.matcher(line);
			//System.out.println(line);
			if(( !line.split("\\s+")[4].trim().equalsIgnoreCase("-"))  ){
				Verb v = getVerbArguments(++verbCount, line.split("\\s+")[0].trim(), sentence);
				verbs.add(v);
				verbsToArgs.put(v.text, v);
			}
		}
		return verbsToArgs; 
	}
	
	//get the arguments of a verb
	public Verb getVerbArguments(int index, String verb, String sentence){
		
		List<String> words = Arrays.asList(sentence.trim().split("\\s+"));
		//System.out.println(sentence);
		Verb v = new Verb();
		v.text = verb;
		HashMap<String, String> argumentToText = new HashMap<String, String>();
		index = index + 4;
		//System.out.println("index "+index);
		for(int i=0; i < lineArr.length; i++){
			//String[] arr = lineArr[i].split("\\s+");
			String value =  lineArr[i].trim().split("\\s+")[index].trim();
			if(value.equals("O")){
				continue;
			} else if(value.startsWith("S-") && !value.contains("S-V")){
				String arg = value.split("S-")[1];
				//String text = lineArr[i].trim().split("\\s+")[0].trim();
				String text = words.get(i).trim();
				argumentToText.put(arg, text);
			}
			else if(value.startsWith("B-")){
				String arg = value.split("B-")[1];
				//String text = lineArr[i].trim().split("\\s+")[0].trim();
				String text = words.get(i).trim();
				while(!value.startsWith("E-")){
					 i++;
					 value =  lineArr[i].trim().split("\\s+")[index].trim();
					 //text += (" "+lineArr[i].trim().split("\\s+")[0].trim());
					 text += (" "+words.get(i).trim());
				}
				if(argumentToText.containsKey(arg))
					arg = arg + "-1";
				argumentToText.put(arg, text);
			}
		}
		v.argumentToText = argumentToText;
		return v;
	}

}
