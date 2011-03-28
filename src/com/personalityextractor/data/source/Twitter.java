/**
 * 
 */
package com.personalityextractor.data.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * @author semanticvoid
 * 
 */
public class Twitter {

	public static String clean(String text) throws IOException {
		text = text.toLowerCase().replaceFirst("^rt [a-z0-9@: ]+:", "");
		text = text.replaceAll("http://[a-z.0-9+/%&#~\\\\-_]+", "");
		text = text.replaceAll("\\\\ \"", "");
		text = text.replaceAll("-[ ]+$", " .");
		text = text.replaceAll("[^a-zA-Z0-9\\s]", ".\n");
		if (!text.endsWith(".")) {
			text += ".";
		}
		text += "\n";
		return text;
	}

	private static String extract(JSONObject jsonObject) {
		JSONObject jObj = JSONObject.fromObject(jsonObject);
		if (jObj.containsKey("text")) {
			return (String) jObj.get("text");
		} else {
			return null;
		}
	}

	public List<String> fetchTweets(String user) {
		ArrayList<String> tweets = new ArrayList<String>();
		String urlStr = "http://twitter.com/statuses/user_timeline/" + user + ".json?count=10";
		
		try {
			URL url = new URL(urlStr);
	        URLConnection yc = url.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
	        String inputLine;
	
	        StringBuffer buf = new StringBuffer();
	        while ((inputLine = in.readLine()) != null) { 
	        	buf.append(inputLine);
	        }
	        
	        JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(buf.toString());
	        Iterator<JSONObject> itr = jsonArray.iterator();
	        while (itr.hasNext()) {
	        	String tweetText = extract(itr.next());
	        	tweets.add(tweetText);
	        }
	        
	        in.close();
	        return tweets;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
