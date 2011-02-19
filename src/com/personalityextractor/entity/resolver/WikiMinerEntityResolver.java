package com.personalityextractor.entity.resolver;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.personalityextractor.entity.extractor.IEntityExtractor;


import wikipedia.Wikiminer;

/**
 * @author akishore
 *
 */
public class WikiMinerEntityResolver implements IEntityExtractor {
	
	final static List<String> stopWords = Arrays.asList(
			   "a", "an", "and", "are", "as", "at", "be", "but", "by",
			   "for", "if", "in", "into", "is", "it",
			   "no", "not", "of", "on", "or", "such",
			   "that", "the", "their", "then", "there", "these",
			   "they", "this", "to", "was", "will", "with", "most", "needs"
	);

	public ArrayList<String> extract(String line) {
		if(line == null) {
			return null;
		}
		
		ArrayList<String> allEntities = new ArrayList<String>();
		String[] words = line.split("[ :;'\"?/><,\\.!@#$%^&()-+=~`{}|]+");
		ArrayList<String> filteredWords = new ArrayList<String>();
		for(String word : words) {
			if(word.length() <= 0) {
				filteredWords.add(null);
				continue;
			}
			boolean isStop = false;
			for(String sWord : stopWords) {
				if(word.equalsIgnoreCase(sWord)) {
					isStop = true;
					break;
				}
			}
			if(isStop) {
				filteredWords.add(null);
				continue;
			}		
			filteredWords.add(word.toLowerCase());
		}
		
		ArrayList<String> consecutiveWords = new ArrayList<String>();
		for(String fw : filteredWords) {
			if(fw == null) {
				ArrayList<String> entities = extractEntities(consecutiveWords);
				allEntities.addAll(entities);
				for(String entity : entities) {
//					System.out.println(entity);
				}
				consecutiveWords = new ArrayList<String>();
				continue;
			}			
			consecutiveWords.add(fw);
		}
		
		if(consecutiveWords.size() > 0) {
			ArrayList<String> entities = extractEntities(consecutiveWords);
			allEntities.addAll(entities);
			for(String entity : entities) {
//				System.out.println(entity);
			}
		}
		return allEntities;
	}
	
	/*
	 * a basic method to resolve ambiguity between 2 entities one of which is a *substring* of the other. 
	 * some example cases: 'New' or 'New York'?, 'short' or 'short story', '' 
	 */
	public boolean breakTies(String bigger_entity_id, String smaller_entity){
		if(bigger_entity_id.equalsIgnoreCase("645042") && smaller_entity.equalsIgnoreCase("city")){
			System.out.println("");
		}
		String xml = null;
		if((xml = Wikiminer.getXML(bigger_entity_id, true)) != null) {
			ArrayList<String> links = getLinks(xml);
			for(String s : links){
				if(smaller_entity.equalsIgnoreCase(s)){
					return true;
				}
			}
			
		}
		return false;
	}
	
	/*
	 * Takes as input only entities   
	 */
	private ArrayList<String> removeExtraneousEntities(ArrayList<String> entities){
		ArrayList<String> extraneous = new ArrayList<String>();
		HashMap<String, ArrayList<String>> sortedEntities = new HashMap<String, ArrayList<String>>();
		for(String entity : entities){
			String[] split = entity.split("\\s+");
			String split_length = Integer.toString(split.length);
			if(sortedEntities.containsKey(split_length)){
				sortedEntities.get(split_length).add(entity);
			} else{
				ArrayList<String> arr = new ArrayList<String>();
				arr.add(entity);
				sortedEntities.put(split_length, arr);
			}
		}
		
		Object[] keys = sortedEntities.keySet().toArray();
		Arrays.sort(keys);
		for(int i = keys.length-1; i >=0; i--){
			ArrayList<String> arr = sortedEntities.get(keys[i]);
			for(String entity : arr){
				if(extraneous.contains(entity))
					continue;
				String xml = "";
				if((xml = Wikiminer.getXML(entity, false)) != null) {
					String id = getHighestSenseID(xml);
					if(id==null){
						continue;
					}
					/*
					 * the higher entity has passed the test- so take only lower entities which contain links
					 */
					for(int j=i-1; j >=0; j--){
						ArrayList<String> arr_smaller = sortedEntities.get(keys[j]);
						for(String entity_small: arr_smaller){
							if(!breakTies(id, entity_small)){
								extraneous.add(entity_small);
							}
						}
					}
				}
			}
		}
		for(String s: extraneous){
			entities.remove(s);
		}
		return entities;
	}
	
	private String getHighestSenseID(String xml){
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
//	        System.out.println(xml);
			Document dom = db.parse(is);
			NodeList senseNodes = dom.getElementsByTagName("Sense");
			Node topSense = senseNodes.item(0);
			if(topSense != null) {
				NamedNodeMap attrs = topSense.getAttributes();
				Node commonness = attrs.getNamedItem("commonness");
				try {
					double relevance = Double.parseDouble(commonness.getTextContent());
					if(relevance >= 0.70) {
						return attrs.getNamedItem("id").getTextContent();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				NodeList articleNodes = dom.getElementsByTagName("Article");
				if(articleNodes != null && articleNodes.item(0) != null) {
					NamedNodeMap attrs = articleNodes.item(0).getAttributes();
					return attrs.getNamedItem("id").getTextContent();	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return null;

	}
	
	private boolean checkThreshold(String xml){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (Exception e) {
			return false;
		}
		
		try {
			InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
			Document dom = db.parse(is);
			NodeList senseNodes = dom.getElementsByTagName("Sense");
			Node topSense = senseNodes.item(0);
			if(topSense != null) {
				NamedNodeMap attrs = topSense.getAttributes();
				Node commonness = attrs.getNamedItem("commonness");
				try {
					double relevance = Double.parseDouble(commonness.getTextContent());
					if(relevance >= 0.70) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				NodeList articleNodes = dom.getElementsByTagName("Article");
				if(articleNodes != null && articleNodes.item(0) != null) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return false;
	}
	
	private ArrayList<String> getLinks(String xml){
		
		ArrayList<String> links = new ArrayList<String>();		
		DocumentBuilder db = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}

		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		try {
			Document dom = db.parse(is);
			NodeList outNodes = dom.getElementsByTagName("LinkOut");
			if (outNodes != null && outNodes.getLength() != 0) {
				for (int i = 0; i < outNodes.getLength(); i++) {
					Node link = outNodes.item(i);
					if (link != null) {
						NamedNodeMap attrs = link.getAttributes();
						Node commonness = attrs.getNamedItem("relatedness");
						double relevance = Double.parseDouble(commonness
								.getTextContent());
						if (relevance >= 0.1) {
							links.add(attrs.getNamedItem("title")
									.getTextContent());
						}
					}
				}
			}

			NodeList inNodes = dom.getElementsByTagName("LinkIn");
			if (inNodes != null && inNodes.getLength() != 0) {
				for (int i = 0; i < inNodes.getLength(); i++) {
					Node link = inNodes.item(i);
					if (link != null) {
						NamedNodeMap attrs = link.getAttributes();
						Node commonness = attrs.getNamedItem("relatedness");
						double relevance = Double.parseDouble(commonness
								.getTextContent());
						if (relevance >= 0.1) {
							links.add(attrs.getNamedItem("title")
									.getTextContent());
						}
					}
				}
			}			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return links;
	}
	private static ArrayList<String> extractEntities(ArrayList<String> words) {
		ArrayList<String> entities = new ArrayList<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (Exception e) {
			return entities;
		}
		for(int i=0; i<words.size(); i++) {
			StringBuffer buf = new StringBuffer();
			for(int j=i; j<words.size(); j++) {
				buf.append(words.get(j) + " ");
				String entity = buf.toString().trim();
				if(entity.equalsIgnoreCase("driving")){
					System.out.println("");
				}
				String xml = null;
				if((xml = Wikiminer.getXML(entity, false)) != null) {
					try {
						InputSource is = new InputSource();
				        is.setCharacterStream(new StringReader(xml));
//				        System.out.println(xml);
						Document dom = db.parse(is);
						NodeList senseNodes = dom.getElementsByTagName("Sense");
						Node topSense = senseNodes.item(0);
						if(topSense != null) {
							NamedNodeMap attrs = topSense.getAttributes();
							Node commonness = attrs.getNamedItem("commonness");
							try {
								double relevance = Double.parseDouble(commonness.getTextContent());
								if(relevance >= 0.70) {
									entities.add(entity);
								}
							} catch (Exception e) {
							}
						} else {
							NodeList articleNodes = dom.getElementsByTagName("Article");
							if(articleNodes != null && articleNodes.item(0) != null) {
								entities.add(entity);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return entities;
	}
	
	private static String getXML(String query, boolean isId) {
		try {
			String urlStr = "http://wdm.cs.waikato.ac.nz:8080/service?task=search&xml";
			if(isId) {
				urlStr += "&id=" + query;
			} else {
				urlStr += "&term=" + query;
			}
			URL url = new URL(urlStr);
	        URLConnection yc = url.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        String inputLine;
	
	        StringBuffer buf = new StringBuffer();
	        while ((inputLine = in.readLine()) != null) 
	            buf.append(inputLine);
	        in.close();
	        
	        if(buf.toString().contains("unknownTerm")) {
	        	return null;
	        }
	        
	        return buf.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WikiMinerEntityResolver pme = new WikiMinerEntityResolver();
		ArrayList<String> entities = new ArrayList<String>();
		entities.add("York City");
		entities.add("City");
		entities.add("New York City");
		entities.add("New York");
		System.out.println(pme.removeExtraneousEntities(entities));
		
		entities = new ArrayList<String>();
		entities.add("short story");
		entities.add("story");
		entities.add("short");
		System.out.println(pme.removeExtraneousEntities(entities));
		
		
//		pme.extract("Preparing my end-of-internship talk at Microsoft. Pictures, no formula :)");
//		pme.extract("Will soon be en route amman to Frankfurt.");
//		pme.extract("New model of the universe fits data better than Big Bang");
		//pme.extract("RT @LanceWeiler: Hubs & Connectors: Understanding Networks Through Data Visualization http://bit.ly/eegRqT HT @JeffClark");
		//System.out.println(pme.extract("About to embark on the unthinkable... Driving to New York City. Wish me luck."));
		
		
		
//		try{
//			BufferedReader br = new BufferedReader(new FileReader(args[0]));
//			String line = "";
//			while((line=br.readLine())!=null){
//				line=line.replace("<E>", "");
//				line=line.replace("</E>", "");
//				System.out.println(line+"\n"+pme.extract(line));
//			}
//			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
	}

}
