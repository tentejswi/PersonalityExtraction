/**
 * 
 */
package com.personalityextractor.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * @author semanticvoid
 *
 */
public class MysqlStore {
	
	private Connection conn = null;
	private Statement stmt = null;

	public MysqlStore(String host, String user, String passwd, String database) throws Exception {
		try {
		      Class.forName("com.mysql.jdbc.Driver");
		      String url = "jdbc:mysql://" + host + ":3306/" + database;
		      conn =	DriverManager.getConnection(url, user, passwd);
		    }catch( Exception e ) {
		      throw e;
		    }
	}
	
	public ResultSet execute(String query) {
		ResultSet rs;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return rs;
	}
	
	public boolean executeUpdate(String query) {
		try {
			stmt = conn.createStatement();
			boolean val = stmt.execute(query);
			stmt.close();
			return val;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void closeStmt() {
		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
