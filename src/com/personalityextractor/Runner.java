/**
 * 
 */
package com.personalityextractor;

import java.sql.ResultSet;
import java.util.List;

import com.personalityextractor.data.source.Twitter;
import com.personalityextractor.store.MysqlStore;

/**
 * Main Class
 * @author semanticvoid
 *
 */
public class Runner {
	
	static MysqlStore store;

	public static String popUserFromQueue() {
		ResultSet rs = store.execute("SELECT handle FROM user_queue WHERE done = 0 LIMIT 1");
		
		try {
			if(rs.first()) {
				String handle = rs.getString("handle");
				return handle;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean updateUser(String handle) {
		return store.executeUpdate("UPDATE user_queue SET done = 1 WHERE handle like \"" + handle + "\"");
	}
	
	public static void run() {
		String handle = popUserFromQueue();
		Twitter t = new Twitter();
		if(handle != null) {
			List<String> tweets = t.fetchTweets(handle);
			// TODO pass tweets via extractor and resolver
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
		
		while(true) {
			run();
		}
	}

}
