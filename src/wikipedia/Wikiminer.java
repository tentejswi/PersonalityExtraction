package wikipedia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Wikiminer {
	
	//caution ahead: mix up the order of the args at your own peril 
	public static double calculateJaccard(List<String> links, List<String> contextPhrases){
		StringBuffer arr1String = new StringBuffer();
		HashSet<String> union = new HashSet<String>();
		
		double overlap = 0.0;
		for(String s : links){
			arr1String.append(s+" ");
			union.add(s);
		}
		System.out.println(arr1String);
		String arr1Concat = arr1String.toString().trim();
		for(String s : contextPhrases){
			if(arr1Concat.contains(s)){
				overlap++;
			}
			union.add(s);
		}
		
		return (overlap/union.size());
	}
	
	public static double getPMI(String xml, List<String> contextPhrases){
		
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
			
			return calculateJaccard(links, contextPhrases);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}
	
	public static ArrayList<String[]> getWikipediaSenses(String xml, boolean getId){
		ArrayList<String[]> senses = new ArrayList<String[]>();
		DocumentBuilder db = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}

		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		// System.out.println(xml);
		try{
		Document dom = db.parse(is);
		NodeList senseNodes = dom.getElementsByTagName("Sense");
		if (senseNodes != null && senseNodes.getLength() != 0) {
			for (int i = 0; i < senseNodes.getLength(); i++) {
				Node topSense = senseNodes.item(i);
				if (topSense != null) {
					NamedNodeMap attrs = topSense.getAttributes();
					Node commonness = attrs.getNamedItem("commonness");
					double relevance = Double.parseDouble(commonness.getTextContent());
					if (relevance >= 0.1) {
						String[] senseArray = {attrs.getNamedItem("title").getTextContent(), attrs.getNamedItem("id").getTextContent()};
						senses.add(senseArray);
					}
				}
			}
		} else {
			NodeList articleNodes = dom.getElementsByTagName("Article");
			if (articleNodes != null && articleNodes.item(0) != null) {
				senses.add(new String[]{"wikipedia entry", "0"});
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return senses;

	}
	
	public static String getXML(String query, boolean isId) {
		
		if(query.equalsIgnoreCase("wikipedia entry"))
			return null;
		StringBuffer correctEncoding = new StringBuffer();
		String[] sensesplit = query.split("\\s+");
		if(sensesplit.length>1)
		for(String s : sensesplit){
			correctEncoding.append(s+"%20");
		}
		if(correctEncoding.length()!=0){
			query = correctEncoding.toString().substring(0, correctEncoding.toString().length()-3);
		}

		try {
			String urlStr = "http://wdm.cs.waikato.ac.nz:8080/service?task=search&xml";
			if(isId) {
				urlStr += "&id=" + query;
			} else {
				urlStr += "&term=" + query;
			}
			URL url = new URL(urlStr);
	        URLConnection yc = url.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
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
	
	public static void main(String args[]){
		Wikiminer wm = new Wikiminer();
		System.out.println(getXML("Model%20(person)", false));
	}


}
