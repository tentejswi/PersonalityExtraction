package com.personalityextractor.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.personalityextractor.entity.WikipediaEntity;

public class WikiminerDB {

	public MysqlStore db;
	
	public WikiminerDB(String host, String user, String passwd, String database) throws Exception {
		db = new MysqlStore(host, user, passwd, database);
	}

	public List<WikipediaEntity> search(String terms) {
		List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		String query = "SELECT page_id, page_title FROM page_indexed WHERE MATCH(page_title) AGAINST('" + terms + "')";
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
	
}
