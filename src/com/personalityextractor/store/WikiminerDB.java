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
	private static String host;
	private static String user;
	private static String passwd;
	private static String database;

	private WikiminerDB(String host, String user, String passwd, String database)
			throws Exception {
		db = new MysqlStore(host, user, passwd, database);
	}

	public static WikiminerDB getInstance(String host, String user,
			String passwd, String database) throws Exception {
		if (instance == null) {
			WikiminerDB.database = database;
			WikiminerDB.user = user;
			WikiminerDB.passwd = passwd;
			WikiminerDB.host = host;
			instance = new WikiminerDB(host, user, passwd, database);
		}

		return instance;
	}
	
	public static WikiminerDB getInstance() {
		return instance;
	}

	public List<WikipediaEntity> search(String terms) {
		List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		String query = "SELECT page_id, page_title, inlinks FROM page_indexed WHERE MATCH(page_title) AGAINST('"
				+ terms.toLowerCase()
				+ "' IN BOOLEAN MODE) ORDER BY inlinks desc limit 20";
		ResultSet rs = db.execute(query);
		try {
			while (rs.next()) {
				String id = rs.getString("page_id");
				String title = rs.getString("page_title");
				String inlinks = rs.getString("inlinks");
				entities.add(new WikipediaEntity(title, id, inlinks));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return entities;
	}

	public double compare(String id1, String id2) {
		// System.out.println("comparing " + id1 + ":" + id2);
		// System.gc();
		double sim = 0;
		if (id1 == null || id2 == null || id1.equals("") || id2.equals("")) {
			return sim;
		}
		Set<String> categories1 = new HashSet<String>();
		Set<String> categories2 = new HashSet<String>();

		String query1 = "SELECT cl_parent FROM categorylink WHERE cl_child = "
				+ id1 + " LIMIT 200";
		String query2 = "SELECT cl_parent FROM categorylink WHERE cl_child = "
				+ id2 + " LIMIT 200";

		// System.out.println(query1);
		// System.out.println(query2);

		ResultSet rs = db.execute(query1);
		try {
			while (rs.next()) {
				String id = rs.getString("cl_parent");
				categories1.add(id);
			}
			rs.close();
			db.closeStmt();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		rs = db.execute(query2);
		try {
			while (rs.next()) {
				String id = rs.getString("cl_parent");
				categories2.add(id);
			}
			rs.close();
			db.closeStmt();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		double intersection = 0;
		for (String id11 : categories1) {
			if (categories2.contains(id11)) {
				intersection++;
			}
		}

		if (intersection > 0 && (categories1.size() + categories2.size()) > 0) {
			sim = intersection * 2 / (categories1.size() + categories2.size());
		}
		return sim;
	}

	public void populateInLinks(int index) {
		ResultSet rs = null;
		int refresh = 0;
		do {
			String query = "SELECT * from pagelink_in LIMIT " + index
					+ ", 1000";

			try {
				rs = db.execute(query);
				while (rs.next()) {
					String id = rs.getString("li_id");
					String data = rs.getString("li_data");
					String[] ids = data.split(":");
					db.executeUpdate("UPDATE page_indexed set inlinks = "
							+ (ids.length - 1) + " WHERE page_id = " + id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (rs != null) {
					try {
						rs.close();
						db.closeStmt();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			System.out.println(index);
			// if(index%1000000 == 0 && refresh != 0) {
			// break;
			// }
			index += 1000;
			refresh++;
			if (refresh == 5) {
				// this.reconnect();
			}
		} while (index < 6700000);
	}

	public List<WikipediaEntity> getCategories(String id) {
		List<WikipediaEntity> categories = new ArrayList<WikipediaEntity>();
		String query = "SELECT page_id, page_title FROM categorylink, page_indexed WHERE cl_child = "
				+ id + " AND page_id = cl_parent LIMIT 50";
		ResultSet rs = db.execute(query);

		try {
			while (rs.next()) {
				String cid = rs.getString("page_id");
				String ctitle = rs.getString("page_title");
				categories.add(new WikipediaEntity(ctitle, cid));
			}
			rs.close();
			db.closeStmt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return categories;
	}

	private boolean reconnect() {
		this.db = null;
		try {
			db = new MysqlStore(host, user, passwd, database);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		System.gc();
		return true;
	}
}
