/**
 * 
 */
package com.personalityextractor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.personalityextractor.data.source.Twitter;
import com.personalityextractor.entity.Entity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.entity.extractor.IEntityExtractor;
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

	public static void run() {
		String handle = popUserFromQueue();
		Twitter t = new Twitter();
		IEntityExtractor extractor = EntityExtractFactory.produceExtractor(Extracter.CONSECUTIVE_WORDS);
		ViterbiResolver resolver = new ViterbiResolver();
		
		if(handle != null) {
			List<String> tweets = t.fetchTweets(handle);
			
			for(String tweet : tweets) {
				List<String> entities = extractor.extract(tweet);
				List<Entity> resolvedEntities = resolver.resolve(entities);
			}
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
