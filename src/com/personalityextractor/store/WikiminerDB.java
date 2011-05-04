package com.personalityextractor.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.personalityextractor.entity.WikipediaEntity;

public class WikiminerDB {

	private static MysqlStore db = null;
	private static WikiminerDB instance = null;
	
	private WikiminerDB(String host, String user, String passwd, String database) throws Exception {
		db = new MysqlStore(host, user, passwd, database);
	}
	
	public static WikiminerDB getInstance(String host, String user, String passwd, String database) throws Exception {
		if(instance == null) {
			instance = new WikiminerDB(host, user, passwd, database);
		}
		
		return instance;
	}

	public List<WikipediaEntity> search(String terms) {
		List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		String query = "SELECT page_id, page_title, MATCH(page_title) AGAINST('" + terms + "') as relevance FROM page_indexed WHERE MATCH(page_title) AGAINST('" + terms + "' IN BOOLEAN MODE) limit 20";
		ResultSet rs = db.execute(query);
		try {
			while(rs.next()) {
				String id = rs.getString("page_id");
				String title = rs.getString("page_title");
				String score = rs.getString("relevance");
				entities.add(new WikipediaEntity(title, id, score));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return entities;
	}
	
	public double compare(String id1, String id2) {
		System.out.println("comparing " + id1 + ":" + id2);
		System.gc();
		double sim  = 0;
		if(id1 == null || id2 == null || id1.equals("") || id2.equals("")) {
			return sim;
		}
		Set<String> categories1 = new HashSet<String>();
		Set<String> categories2 = new HashSet<String>();
		
		String query1 = "SELECT cl_parent FROM categorylink WHERE cl_child = " + id1 + " LIMIT 200";
		String query2 = "SELECT cl_parent FROM categorylink WHERE cl_child = " + id2 + " LIMIT 200";
		
//		System.out.println(query1);
//		System.out.println(query2);
		
		ResultSet rs = db.execute(query1);
		try {
			while(rs.next()) {
				String id = rs.getString("cl_parent");
				categories1.add(id);
			}
			rs.close();
		} catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
		
		rs = db.execute(query2);
		try {
			while(rs.next()) {
				String id = rs.getString("cl_parent");
				categories2.add(id);
			}
			rs.close();
		} catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
		
		double intersection = 0;
		for(String id11 : categories1) {
			if(categories2.contains(id11)) {
				intersection++;
			}
		}
		
		if(intersection > 0 && (categories1.size()+categories2.size()) > 0) {
			sim = intersection*2/(categories1.size()+categories2.size());
		}
		return sim;
	}

	public void populateInLinks() {
		int index = 0;
		ResultSet rs = null;
		
		do {
			String query = "SELECT * from pagelink_in LIMIT " + index + ", 100";
			
			try {
				rs = db.execute(query);
				while(rs.next()) {
					String id = rs.getString("li_id");
					String data = rs.getString("li_data");
					String[] ids = data.split(":");
					db.executeUpdate("UPDATE page_indexed set inlinks = " + (ids.length-1) 
							+ " WHERE page_id = " + id);
					System.out.println(id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			index += 100;
		} while(rs != null);
	}
}
