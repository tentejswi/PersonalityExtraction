/**
 * 
 */
package com.personalityextractor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.personalityextractor.data.source.Twitter;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.entity.graph.Graph;
import com.personalityextractor.entity.graph.Node;
import com.personalityextractor.entity.graph.ranking.IRanker;
import com.personalityextractor.entity.graph.ranking.WeightGraphRanker;
import com.personalityextractor.entity.resolver.ViterbiResolver;
import com.personalityextractor.store.MysqlStore;

/**
 * Main Class
 * 
 * @author semanticvoid
 * 
 */
public class Runner {

	static MysqlStore store;

	public static String popUserFromQueue() {
		ResultSet rs = store
				.execute("SELECT handle FROM user_queue WHERE done = 0 LIMIT 1");

		try {
			if (rs.first()) {
				String handle = rs.getString("handle");
				return handle;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean updateUser(String handle) {
		return store
				.executeUpdate("UPDATE user_queue SET done = 1 WHERE handle like \""
						+ handle + "\"");
	}
	
	public static boolean setUserInterests(String handle, String json) {
		return store
				.executeUpdate("INSERT INTO user_interests(handle, json) values ('" + handle + "','" + json + "')" +
						"ON DUPLICATE KEY UPDATE json = '" + json + "'");
	}
	
	public static String nodesToJson(List<Node> nodes) {
		JSONObject json = new JSONObject();
		
		for(Node n : nodes) {
			json.put(n.getEntity().getText(), 1);
		}
		
		return json.toString();
	}

	public static void run() {
		String handle = popUserFromQueue();
		
		if(handle == null) {
			return;
		}
		
		Twitter t = new Twitter();
		IEntityExtractor extractor = EntityExtractFactory.produceExtractor(Extracter.CONSECUTIVE_WORDS);
		ViterbiResolver resolver = new ViterbiResolver();
		HashMap<String, WikipediaEntity> allEntities = new HashMap<String, WikipediaEntity>();
		
		if(handle != null) {
			List<String> tweets = t.fetchTweets(handle);
			
//			tweets.clear();
//			tweets.add("The mouse ate the cheese.");
			for(String tweet : tweets) {
				List<String> entities = extractor.extract(tweet);
				List<WikipediaEntity> resolvedEntities = resolver.resolve(entities);
				
				for(WikipediaEntity e : resolvedEntities) {
					if(!allEntities.containsKey(e.getText())) {
						allEntities.put(e.getText(), e);
					} else {
						(allEntities.get(e.getText())).incrCount();
					}
				}
				break;
			}
			
			List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
			entities.addAll(allEntities.values());
			Graph g = new Graph(entities);
			g.build();
			IRanker ranker = new WeightGraphRanker(g);
			List<Node> topNodes = ranker.getTopRankedNodes(2);
			setUserInterests(handle, nodesToJson(topNodes));
			// update status
			updateUser(handle);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			store = new MysqlStore("localhost", "root", "", "pe");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		while (true) {
			run();
		}
	}

}
