/**
 * 
 */
package com.personalityextractor;

import java.sql.ResultSet;

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
	
	public static void run() {
		String handle = popUserFromQueue();
		if(handle != null) {
			// process tweets TODO
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
