package com.personalityextractor.entity.resolver;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import tathya.db.YahooBOSS;
import wikipedia.Wikiminer;

import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.entity.extractor.IEntityExtractor;

import cs224n.util.PriorityQueue;

/**
 * @author akishore
 *
 */
public class WikiMinerEntityResolver extends BaseEntityResolver {
	
	public WikiMinerEntityResolver(IEntityExtractor extractor) {
		super(extractor);
	}

	public ArrayList<String> extract(String line) {
		return extractor.extract(line);
	}
	
	public boolean breakTies(String bigger_entity_id, String smaller_entity){
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
	 * a basic method to resolve ambiguity between entities one of which is a *bigger entity* of all the other. 
	 * some example cases: 'New' or 'New York'?, 'short' or 'short story', '' 
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
					 * the higher entity has passed the test- so remove all smaller entities which are not present in the links for this bigger entity
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
		for (int i = 0; i < words.size(); i++) {
			String entity = words.get(i).trim();
			String xml = null;
			if ((xml = Wikiminer.getXML(entity, false)) != null) {
				try {
					InputSource is = new InputSource();
					is.setCharacterStream(new StringReader(xml));
					Document dom = db.parse(is);
					NodeList senseNodes = dom.getElementsByTagName("Sense");
					Node topSense = senseNodes.item(0);
					if (topSense != null) {
						NamedNodeMap attrs = topSense.getAttributes();
						Node commonness = attrs.getNamedItem("commonness");
						try {
							double relevance = Double.parseDouble(commonness.getTextContent());
							if (relevance >= 0.70) {
								entities.add(entity);
							}
						} catch (Exception e) {
						}
					} else {
						NodeList articleNodes = dom.getElementsByTagName("Article");
						if (articleNodes != null && articleNodes.item(0) != null) {
							entities.add(entity);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return entities;

		
	}
	

	public static List<WikipediaEntity> getRankedEntities(String entity, List<String> contextPhrases){
		List<WikipediaEntity> rankedEntities = new ArrayList<WikipediaEntity>();
		PriorityQueue<WikipediaEntity> queue = new PriorityQueue<WikipediaEntity>();
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
				WikipediaEntity we = new WikipediaEntity(senseArr[0],senseArr[1]);
				queue.add(we, ((double) senseCount / (double) contextCount));
			}
		}
		while(queue.hasNext()){
			rankedEntities.add(queue.next());
		}
		
		return rankedEntities;
	}

	public static List<String> getRankedTypes(String entity, String xml, List<String> contextPhrases, int numTypes){
		int entityCount = YahooBOSS.makeQuery('"' + entity + '"');
		StringBuffer contextQuery = new StringBuffer();
		for (String c : contextPhrases) {
			contextQuery.append("\"" + c + "\"" + " ");
		}
		
		List<String> rankedCategories = new ArrayList<String>();
		PriorityQueue<String> queue = new PriorityQueue<String>();
		
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
			NodeList senseNodes = dom.getElementsByTagName("Category");
			if (senseNodes != null && senseNodes.getLength() != 0) {
				for (int i = 0; i < senseNodes.getLength(); i++) {
					Node topSense = senseNodes.item(i);
					if(topSense!=null){
						NamedNodeMap attrs = topSense.getAttributes();
						String type = attrs.getNamedItem("title").getTextContent();
						int count = YahooBOSS.makeQuery("\""+type+"\" \""+entity+"\" "+contextPhrases.toString());
						queue.add(type, ((double)count/(double)entityCount));
					}
				}
			}
			while(queue.hasNext() && numTypes > 0 ){
				numTypes--;
				rankedCategories.add(queue.next());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedCategories;
	}

	
	private static List<WikipediaEntity> resolveEntities(ArrayList<String> entities) {
		ArrayList<WikipediaEntity> resolvedEntities = new ArrayList<WikipediaEntity>();
		List<String> contextPhrases = new ArrayList<String>(entities);
		for(String entity : entities){
			List<WikipediaEntity> rankedEntities = getRankedEntities(entity, contextPhrases);
			for(WikipediaEntity we : rankedEntities){				
				String xml = getXML(we.getWikiminerID(), true);
				for(String s: getRankedTypes(we.getText(), xml, contextPhrases, 5)){

					we.addCategory(s);
				}
				resolvedEntities.add(we);
			}
		}
		return resolvedEntities;
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
		WikiMinerEntityResolver pme = new WikiMinerEntityResolver(EntityExtractFactory.produceExtractor(Extracter.CONSECUTIVE_WORDS));
//		ArrayList<String> entities = new ArrayList<String>();
//		entities.add("York City");
//		entities.add("City");
//		entities.add("New York City");
//		entities.add("New York");
//		System.out.println(pme.removeExtraneousEntities(entities));
//		
//		entities = new ArrayList<String>();
//		entities.add("short story");
//		entities.add("story");
//		entities.add("short");
//		System.out.println(pme.removeExtraneousEntities(entities));
		
		
		ArrayList<String> entities = pme.extract("Will soon be en route New York to Frankfurt.");
		for(String e : entities) {
			System.out.println(e);
		}
		for(WikipediaEntity we: resolveEntities(entities)){
			we.print();
		}
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
