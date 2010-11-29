package tathya.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBI {

	Connection conn = null;

	public void connect() {
		try {
			String userName = "test";
			String password = "test";
			String url = "jdbc:mysql://fromvalue.greatamerica.corp.yahoo.com:3306/timeline";
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, userName, password);
			System.out.println("Database connection established");
		} catch (Exception e) {
			System.err.println("Cannot connect to database server");
		}
	}

	public void execute(String query) {
		try {
			Statement s = conn.createStatement();
			s.executeUpdate(query);
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResultSet select(String query) {
		try {
			Statement s = conn.createStatement();
			s.executeQuery(query);
			ResultSet rs = s.getResultSet();
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String args[]) {
		DBI db = new DBI();
		db.connect();
	}
}
