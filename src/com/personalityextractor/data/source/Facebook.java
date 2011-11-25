/**
 * 
 */
package com.personalityextractor.data.source;

import java.io.BufferedReader;
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
public class Facebook {

	private String id = null;
	private String accessToken = null;

	public Facebook(String accessToken) {
		this.accessToken = accessToken;
		this.id = getUserId();
	}

	private String getData(String urlStr) {
		try {
			URL url = new URL(urlStr);
			URLConnection yc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;

			StringBuffer buf = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				buf.append(inputLine);
			}

			in.close();
			return buf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private String getUserId() {
		String url = "https://graph.facebook.com/me?access_token="
				+ accessToken;
		String response = getData(url);

		if (response != null) {
			JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(response);
			if (jsonObj != null && jsonObj.containsKey("id")) {
				return jsonObj.getString("id");
			}
		}

		return null;
	}

	public List<String> getUserStatusUpdates() {
		List<String> posts = new ArrayList<String>();
		String url = "https://graph.facebook.com/me/feed?access_token="
				+ accessToken;

		for(int i=0; i<10; i++) {
			String response = getData(url);
			if (response != null) {
				JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(response);
				
				if(jsonObj.containsKey("paging")) {
					JSONObject paging = jsonObj.getJSONObject("paging");
					if(paging.containsKey("next")) {
						url = paging.getString("next");
					}
				}
				
				if(jsonObj.containsKey("data")) {
					JSONArray feedsArray = jsonObj.getJSONArray("data");
					Iterator<JSONObject> itr = feedsArray.iterator();
					while (itr.hasNext()) {
						JSONObject post = itr.next();
						if (post.containsKey("message")
								&& post.containsKey("from")
								&& post.getJSONObject("from").getString("id")
										.equals(id)) {
							posts.add(post.getString("message"));
						}
					}
				}
			}
			
			if(url == null) {
				break;
			}
		}
		
		return posts;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Facebook fb = new Facebook(
				"AAAAAAITEghMBAC3HeZA6TrXYUb3wMkgZBSZA6ZAKkrL9Y3BZBiapUqz6YC4iLeeclCBKsSxJYBSuq1jByI1YVeLinb01Wn2JXNooZCoHOaPQZDZD");
		List<String> posts = fb.getUserStatusUpdates();
		for(String p : posts) {
			System.out.println(p);
		}
	}

}
