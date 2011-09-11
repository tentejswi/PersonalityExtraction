package com.personalityextractor.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.personalityextractor.commons.data.Tweet;
import com.personalityextractor.entity.extractor.SennaNounPhraseExtractor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ReadJSON {

	/**
	 * @param args
	 */
	private static String extract(JSONObject jsonObject) {
		JSONObject jObj = JSONObject.fromObject(jsonObject);
		if (jObj.containsKey("text")) {
			return (String) jObj.get("text");
		} else {
			return null;
		}
	}
	
	public void addSennaOuputtoJson(String json){
		JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(json);
		   Iterator<JSONObject> itr = jsonArray.iterator();	        
	        while (itr.hasNext()) {
	        	String tweetText = extract(itr.next());
	        	Tweet tweet = new Tweet(tweetText);
	        	
	        }
	     	
	}
	
	public List<String> parseJSONArray(String json){
		List<String> tweets = new ArrayList<String>();
		JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(json);
        Iterator<JSONObject> itr = jsonArray.iterator();
        
        while (itr.hasNext()) {
        	String tweetText = extract(itr.next());
        	tweets.add(tweetText);
        }
        
        return tweets;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<String>();
		HashSet<String> testwithurl = new HashSet<String>();
		HashSet<String> testWithouturl = new HashSet<String>();
		HashSet<String> exclusive = new HashSet<String>();
		
		try {
			BufferedReader rdr = new BufferedReader(new FileReader(new File(args[0])));
			String line = null;
			while ((line = rdr.readLine()) != null) {
				//System.out.println(line);
				list.add(line);
			}
			int count =0;
			for(String objstr : list){
				JSONObject obj = JSONObject.fromObject(objstr);
				Iterator<String> itr = obj.keys();
				while(itr.hasNext()){
					String key = itr.next();
					//System.out.println(key);
					if(count==0){
						testwithurl.add(key);
					}
					if(count==1){
						testWithouturl.add(key);
					}
				}
				
				count++;
			}
//			
			for(String url1 : testWithouturl){
				if(!testwithurl.contains(url1)){
					exclusive.add(url1);
				}
			}
			
//			for(String url1 : testwithurl){
//				if(!testWithouturl.contains(url1)){
//					exclusive.add(url1);
//				}
//			}
			for(String url : exclusive)
				System.out.println(url);
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
