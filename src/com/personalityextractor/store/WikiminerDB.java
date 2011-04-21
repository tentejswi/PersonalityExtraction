package com.personalityextractor.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.personalityextractor.entity.WikipediaEntity;

public class WikiminerDB {

	private MysqlStore db = null;
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
		String query = "SELECT page_id, page_title FROM page_indexed WHERE MATCH(page_title) AGAINST('" + terms + "') limit 50";
		ResultSet rs = db.execute(query);
		try {
			while(rs.next()) {
				String id = rs.getString("page_id");
				String title = rs.getString("page_title");
				entities.add(new WikipediaEntity(title, id));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return entities;
	}
	
	public double compare(String id1, String id2) {
		double sim  = 0;
		if(id1 == null || id2 == null || id1.equals("") || id2.equals("")) {
			return sim;
		}
		Set<String> categories1 = new HashSet<String>();
		Set<String> categories2 = new HashSet<String>();
		
		String query1 = "SELECT cl_parent FROM categorylink WHERE cl_child = " + id1 + " LIMIT 300";
		String query2 = "SELECT cl_parent FROM categorylink WHERE cl_child = " + id2 + " LIMIT 300";
		
		ResultSet rs = db.execute(query1);
		try {
			while(rs.next()) {
				String id = rs.getString("cl_parent");
				categories1.add(id);
			}
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
}
