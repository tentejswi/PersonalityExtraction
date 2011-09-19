/**
 * 
 */
package com.personalityextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONObject;

import com.personalityextractor.commons.data.Tweet;
import com.personalityextractor.data.source.Twitter;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.entity.graph.Graph;
import com.personalityextractor.entity.graph.Node;
import com.personalityextractor.entity.graph.ranking.IRanker;
import com.personalityextractor.entity.graph.ranking.WeightGraphRanker;
import com.personalityextractor.entity.resolver.ViterbiResolver;
import com.personalityextractor.evaluation.PerfMetrics;
import com.personalityextractor.evaluation.PerfMetrics.Metric;
import com.personalityextractor.store.MysqlStore;
import com.personalityextractor.url.data.URLContent;

/**
 * Main Class
 * 
 * 
 */
public class Runner {

	static MysqlStore store;

	public static String popUserFromQueue() {
		ResultSet rs = store
				.execute("SELECT handle FROM user_queue WHERE done = 0 AND type = 't' LIMIT 1");

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

	public static boolean setUserInterests(String handle, String json) {
		return store
				.executeUpdate("INSERT INTO user_interests(handle, json) values ('"
						+ handle
						+ "','"
						+ json
						+ "')"
						+ "ON DUPLICATE KEY UPDATE json = '" + json + "'");
	}

	public static String nodesToJson(List<Node> nodes) {
		JSONObject json = new JSONObject();

		for (Node n : nodes) {
			json.put(n.getEntity().getText(), 1);
		}

		return json.toString();
	}

	public static void run() {
		String handle = popUserFromQueue();

		if (handle == null) {
			return;
		}

		Twitter t = new Twitter();
		IEntityExtractor extractor = EntityExtractFactory
				.produceExtractor(Extracter.SENNANOUNPHRASE) ;
		ViterbiResolver resolver = new ViterbiResolver();
		HashMap<String, WikipediaEntity> allEntities = new HashMap<String, WikipediaEntity>();

		if (handle != null) {
			Date start = new Date();
			System.out.print("processing " + handle + "...\t");
			// List<String> tweets = t.fetchTweets(handle, 20);

			List<String> tweets = new ArrayList<String>();
			// tweets.clear();
			// tweets.add("iPhone");
			try {
				BufferedReader rdr = new BufferedReader(new FileReader(
						new File("/Users/tejaswi/Documents/workspace/PersonalityExtraction/data/sample1.txt")));
				String line = null;
				while ((line = rdr.readLine()) != null) {
					tweets.add(line);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			int count = 0;
			for (String text : tweets) {
				// System.out.println(++count);
				Tweet tweet = new Tweet(text);

				for (String s : tweet.getSentences()) {
					List<String> entities = extractor.extract(s);

					// for(String e : entities) {
					// System.out.println(e);
					// }
					List<WikipediaEntity> resolvedEntities = resolver
							.resolve(entities);

					for (WikipediaEntity e : resolvedEntities) {
						if (!allEntities.containsKey(e.getText())) {
							allEntities.put(e.getText(), e);
						}

						(allEntities.get(e.getText())).incrCount();
					}
				}

				for (String urlStr : tweet.getLinks()) {
					String urlContent = URLContent.fetchURLContent(urlStr);
					String title = URLContent.fetchTitleString(urlContent);
					Tweet titleFormatted = new Tweet(title);
					for (String s : titleFormatted.getSentences()) {
					
						List<String> entities = extractor.extract(s);
						List<WikipediaEntity> resolvedEntities = resolver
								.resolve(entities);
						for (WikipediaEntity e : resolvedEntities) {
							if (!allEntities.containsKey(e.getText())) {
								allEntities.put(e.getText(), e);
							}

							(allEntities.get(e.getText())).incrCount();
						}

					}
				}
				// break;
			}

			List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
			entities.addAll(allEntities.values());
			Graph g = new Graph(entities);
			g.build(2);
			g.printWeights();
			Date end = new Date();
			PerfMetrics.getInstance().addToMetrics(Metric.TOTAL,
					(end.getTime() - start.getTime()));

			printMetrics();

			IRanker ranker = new WeightGraphRanker(g);
			List<Node> topNodes = ranker.getTopRankedNodes(2);
			setUserInterests(handle, nodesToJson(topNodes));
			// update status
			// updateUser(handle);
		}
	}

	public static void printMetrics() {
		PerfMetrics metrics = PerfMetrics.getInstance();
		System.err.println("**********************************************");
		System.err.println("             Performance Metrics              ");
		System.err.println("**********************************************");
		for (Metric m : metrics.getMetrics()) {
			System.err.println(m.toString() + "\t" + metrics.getMetric(m));
		}
		System.err.println("**********************************************");
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
			System.out.println("running");
			run();
			break;
		}
	}

}
