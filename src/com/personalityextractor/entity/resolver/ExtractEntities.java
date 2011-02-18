package com.personalityextractor.entity.resolver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import tathya.db.YahooBOSS;
import tathya.text.tokenizer.TwitterTokenizer;
import wikipedia.Wikiminer;

import com.personalityextractor.entity.Entity;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.entity.extractor.NounPhraseExtractor;

import cs224n.util.Counter;
import cs224n.util.PriorityQueue;

public class ExtractEntities implements IEntityExtractor {

	static final HashSet<String> stopWords = new HashSet<String>();
	static NounPhraseExtractor npe = new NounPhraseExtractor();
	
	static {
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(
							"/Users/tejaswi/Documents/workspace/PersonalityExtraction/data/chimps_list-of-english-stopwords-2010-07-01_00-50-22/english_stopwords.tsv"));
			String line = "";
			while ((line = br.readLine()) != null) {
				stopWords.add(line.trim());
			}
			NounPhraseExtractor.initialize("/home/semanticvoid/PE/PersonalityExtraction/lair/englishPCFG.ser.gz");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> extract(String record){
		ArrayList<String> allEntities = new ArrayList<String>();
		for(String s : getEntitiesinTweet(record)){
			allEntities.add(s.trim());
		}
		return allEntities;
	}

	public static HashSet<String> getCapitalizedWords(String token) {
		HashSet<String> capitalWords = new HashSet<String>();
		try {
			Pattern p = Pattern.compile("^[A-Z]+.*");
			String[] split = token.split("\\s+");
			for (String s : split) {
				if (p.matcher(s).matches()) {
					capitalWords.add(s.toLowerCase());
				}
			}
			return capitalWords;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public HashSet<String> getEntitiesinTweet(String tweet){
		HashSet<String> entities = new HashSet<String>();
		TwitterTokenizer tweetTokenizer = new TwitterTokenizer();
		for (String token : tweetTokenizer.tokenize(tweet)) {
			token = token.trim();
			token = token.replaceAll("( [^a-zA-Z0-9\\.]) | ( [^a-zA-Z0-9\\.] ) | ([^a-zA-Z0-9\\.] )"," ");
			try {
				Pattern p = Pattern.compile("^[A-Z]+.*");
				String[] split = token.split("\\s+");
				for (String s : split) {
					s=s.trim();
					if (p.matcher(s).matches() && !stopWords.contains(s.toLowerCase())) {
						entities.add(s);
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}

			for (String np : npe.extract(token)) {
				if (!stopWords.contains(np.trim().toLowerCase())){
					entities.add(np.trim());
				}
			}
		}
		return entities;
	}
	
	
	public List<String[]> getRankedEntities(String entity, List<String> contextPhrases){
		
		List<String[]> rankedEntities = new ArrayList<String[]>();
		PriorityQueue<String[]> queue = new PriorityQueue<String[]>();

		contextPhrases.remove(entity);
		StringBuffer contextQuery = new StringBuffer();
		for (String c : contextPhrases) {
			contextQuery.append("\"" + c + "\"" + " ");
		}
		int contextCount = YahooBOSS.makeQuery(contextQuery.toString());
		String xml = "";
		if((xml=Wikiminer.getXML(entity, false))!=null){
			ArrayList<String[]> senses = Wikiminer.getWikipediaSenses(xml, true);
			for(String[] senseArr : senses){
				int senseCount = YahooBOSS.makeQuery('"' + senseArr[0] + "\" "+contextQuery.toString());
				queue.add(senseArr, ((double) senseCount / (double) contextCount));
			}
		}
		while(queue.hasNext()){
			rankedEntities.add(queue.next());
		}
		
		return rankedEntities;
	}
	
	
	public static void main(String[] args) {
		ExtractEntities ee = new ExtractEntities();
		DocumentBuilder db = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		BufferedWriter bw = null;
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader("data/"+args[0]+".txt"));
			bw = new BufferedWriter(new FileWriter("data/"+args[0]+"_wiki_entities.txt"));
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			db = dbf.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				bw.write("=====================================================\n");
				bw.write("Tweet: "+line+"\n");
				bw.write("=====================================================\n");

				HashSet<String> entities = ee.getEntitiesinTweet(line);
				List<String> contextPhrases = new ArrayList<String>(entities);
				for(String entity: entities){
					bw.write("query: "+entity+"\n");
					List<String[]> rankedEntities = ee.getRankedEntities(entity, contextPhrases);
					//bw.write("RankedEntities: "+rankedEntities+"\n");
					for(String[] entityArr :  rankedEntities){
						bw.write("Entity: "+entityArr[0]+"\n");
						String xml = Wikiminer.getXML(entityArr[1], true);
						bw.write("RankedTypes: "+Wikiminer.getRankedTypes(entityArr[0], xml, contextPhrases, 5)+"\n");
					}
					bw.write("--------------------------------------------------\n");
				}
				bw.write("\n");
			}
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public HashMap<String, Entity> getAllEntities(String handle){
		HashMap<String, Entity> allEntities = new HashMap<String, Entity>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("data/"+handle+".txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("data/"+handle+"_entities.txt"));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter("data/"+handle+"_statistics.txt"));
			
			String line = "";
			Counter<String> nPhraseCounter = new Counter<String>();
			Counter<String> capitalsCounter = new Counter<String>();
			while ((line = br.readLine()) != null) {
				line = line.replaceAll("RT", "");
				TwitterTokenizer tweetTokenizer = new TwitterTokenizer();
				for (String token : tweetTokenizer.tokenize(line)) {
					token = token.trim();
					token = token.replaceAll("( [^a-zA-Z0-9\\.]) | ( [^a-zA-Z0-9\\.] ) | ([^a-zA-Z0-9\\.] )"," ");
					ArrayList<String> nPhrases = new ArrayList<String>();
					HashSet<String> capitalWords = new HashSet<String>();
					try {
						Pattern p = Pattern.compile("^[A-Z]+.*");
						String[] split = token.split("\\s+");
						for (String s : split) {
							if (p.matcher(s).matches() && !stopWords.contains(s.toLowerCase())) {
								capitalWords.add(s.toLowerCase());
								capitalsCounter.incrementCount(s.toLowerCase(), 1.0);
								if(allEntities.containsKey(s.trim())){
									Entity e = allEntities.get(s.trim());
									if(!e.tweets.contains(line)){
										e.tweets.add(line);
										allEntities.put(s.trim(), e);
									}
								} else{
									Entity e = new Entity(s.trim());
									e.tweets.add(line);
									allEntities.put(s.trim(), e);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					bw.write("===============================================\n");
					bw.write(token + "\n");
					System.out.println("token: " + token);
					for (String np : npe.extract(token)) {
						if (!stopWords.contains(np.trim().toLowerCase())){
							nPhrases.add(np.trim());
							nPhraseCounter.incrementCount(np.trim(), 1.0);
							if(allEntities.containsKey(np.trim())){
								Entity e = allEntities.get(np.trim());
								if(!e.tweets.contains(line)){
									e.tweets.add(line);
									allEntities.put(np.trim(), e);
								}
							} else{
								Entity e = new Entity(np.trim());
								e.tweets.add(line);
								allEntities.put(np.trim(), e);
							}
						}
					}
					bw.write("===============================================\n");
					bw.write("Noun-Phrases: " + nPhrases.toString() + "\n");
					// HashSet<String> capitalWords =
					// getCapitalizedWords(token);
					if (capitalWords == null) {
						bw.write("No capitals\n\n");
					} else {
						bw.write("Capitals: " + capitalWords.toString()	+ "\n\n");
						
					}
				}

				bw.flush();
				if (true)
					continue;
			}
			
			PriorityQueue<String> nPhraseQueue = nPhraseCounter.asPriorityQueue();
			PriorityQueue<String> capitalQueue = capitalsCounter.asPriorityQueue();
			while(nPhraseQueue.hasNext()){
				String np = nPhraseQueue.next();
				bw1.write(np+" "+nPhraseCounter.getCount(np)+"\n");					
			}
			bw1.write("=========================================================\n");
			while(capitalQueue.hasNext()){
				String cap = capitalQueue.next();
				bw1.write(cap+" "+capitalsCounter.getCount(cap)+"\n");
			}
			bw1.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allEntities;
	}

	
//	public static void main(String[] args){
//		ExtractEntities ee = new ExtractEntities();
//		HashMap<String, Entity> allEntities = ee.getAllEntities(args[0]);
//		BufferedWriter bw = null;
//		FreebaseWrapper fb = FreebaseWrapper.getInstance(); 
//		try{
//			bw = new BufferedWriter(new FileWriter("data/"+args[0]+"_freebase_entities.txt"));
//			for (String entity : allEntities.keySet()) {
//				if(entity.trim().length()<2)
//					continue;
//				bw.write("=====================================================\n");
//				bw.write("Entity: "+entity+"\n");
//				System.out.println("Entity is "+entity);
//				bw.write("=====================================================\n");
//				List<JSON> rankedEntities = fb.getRankedEntities(entity, 10, allEntities.get(entity).tweets);
//				for(int i=0; i<rankedEntities.size(); i++){
//					JSON rentity = rankedEntities.get(i);
//					bw.write("[Entity: "
//							+ (String) rentity.get("name").value() + "  "
//							+ (String) rentity.get("id").value()+"]");
//				}
//				Entity e = allEntities.get(entity);
//				bw.write("\n-----------------------------------------------------\n");
//				for(String tweet : e.tweets) {
//					bw.write(tweet+"\n");
//				}
//				bw.write("\n");
//			}
//			bw.flush();
//			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//	}
//	
	
//	public static void main(String[] args) {
//		ExtractEntities ee = new ExtractEntities();
//		HashMap<String, Entity> allEntities = ee.getAllEntities(args[0]);
//		DocumentBuilder db = null;
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		BufferedWriter bw = null;
//		try{
//			bw = new BufferedWriter(new FileWriter("data/"+args[0]+"_wiki_entities.txt"));
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		try {
//			db = dbf.newDocumentBuilder();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		for (String entity : allEntities.keySet()) {
//			String xml = "";
//			if ((xml = Wikiminer.getXML(entity, false)) != null) {
//				try {
//					ArrayList<String[]> senses = Wikiminer.getWikipediaSenses(xml, true);
//					bw.write("=====================================================\n");
//					bw.write("Entity: "+entity+"\n");
//					bw.write("=====================================================\n");
//					
//					ArrayList<String> contextPhrases = new ArrayList<String>();
//					for(String tweet : allEntities.get(entity).tweets)	
//						contextPhrases.addAll(ee.getEntitiesinTweet(tweet));
//					
//					PriorityQueue<String> sensesQueue = new PriorityQueue<String>();
//					for(String[] sense : senses){
//						if(sense[0].equalsIgnoreCase("wikipedia entry")) {
//							bw.write(sense[0]+"\n");
//							continue;
//						}
//						String xmlSense = Wikiminer.getXML(sense[1], true);							
//						sensesQueue.add(sense[0], Wikiminer.getPMI(xmlSense, contextPhrases));
//					}
//					
//					bw.write(sensesQueue.toString()+"\n");
//					Entity e = allEntities.get(entity);
//					bw.write("-----------------------------------------------------\n");
//					for(String tweet : e.tweets) {
//						bw.write(tweet+"\n");
//					}
//					bw.write("\n");
//					bw.flush();
//				} catch (Exception e) {
//					e.printStackTrace();
//					System.out.println(xml);
//				}
//			}
//			
//		}
//	}
	
	

	
}
