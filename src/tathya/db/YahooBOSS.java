package tathya.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class YahooBOSS {

	// Yahoo API key
	private static final String API_KEY = "qpBYTfjV34HWf6xUMwEjWYveb6ioxgZdv21O0anUms9gcB3NFox9caeEuavV7BtPubKJNg--";

	public static int makeQuery(String query) {
		int count = 0;

		try {
			// Convert spaces to +, etc. to make a valid URL
			query = URLEncoder.encode(query, "UTF-8");

			// Give me back 10 results in JSON format
			URL url = new URL("http://boss.yahooapis.com/ysearch/web/v1/"
					+ query + "?appid=" + API_KEY + "&format=json");
			URLConnection connection = url.openConnection();

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			JSONParser parser = new JSONParser();
			String response = builder.toString();
			JSONObject json = (JSONObject) parser.parse(response);
			JSONObject jsonResponse = (JSONObject) json.get("ysearchresponse");
			count = Integer.parseInt((String) jsonResponse.get("totalhits"));

//			System.out.println(response);
		} catch (Exception e) {
			System.err.println("Something went wrong...");
			e.printStackTrace();
		}
		
		return count;
	}

	public static void main(String args[]) {
		System.out.println(YahooBOSS.makeQuery("india"));
	}
}