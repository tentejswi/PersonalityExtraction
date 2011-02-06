package com.personalityextractor.entity.extraction;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import wikipedia.Wikiminer;

/**
 * @author akishore
 *
 */
public class PoorMansEntityExtractor implements IEntityExtractor {
	
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
			if(word.length() <= 3) {
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
				if(entity.equalsIgnoreCase("data visualization")){
//					System.out.println("");
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
		PoorMansEntityExtractor pme = new PoorMansEntityExtractor();
//		pme.extract("Preparing my end-of-internship talk at Microsoft. Pictures, no formula :)");
//		pme.extract("Will soon be en route amman to Frankfurt.");
//		pme.extract("New model of the universe fits data better than Big Bang");
		pme.extract("RT @LanceWeiler: Hubs & Connectors: Understanding Networks Through Data Visualization http://bit.ly/eegRqT HT @JeffClark");
	}

}
