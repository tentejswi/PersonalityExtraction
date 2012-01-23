/**
 * 
 */
package com.personalityextractor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONObject;

import com.personalityextractor.data.source.Facebook;
import com.personalityextractor.data.source.Twitter;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.frequencybased.TopNNPHashTagsExtractor;
import com.personalityextractor.entity.extractor.frequencybased.TopNPExtractor;
import com.personalityextractor.entity.graph.Edge;
import com.personalityextractor.entity.graph.Graph;
import com.personalityextractor.entity.graph.Node;
import com.personalityextractor.entity.graph.ranking.IRanker;
import com.personalityextractor.entity.graph.ranking.WeightGraphRanker;
import com.personalityextractor.evaluation.PerfMetrics;
import com.personalityextractor.evaluation.PerfMetrics.Metric;
import com.personalityextractor.store.MysqlStore;

import cs224n.util.Counter;

/**
 * Main Class
 * 
 * 
 */
public class Runner {

	static MysqlStore store;

	public static String[] popUserFromQueue() {
		String[] handle = new String[2];
		ResultSet rs = store
				.execute("SELECT handle, type FROM user_queue WHERE done = 0 LIMIT 1");

		try {
			if (rs.first()) {
				handle[0] = rs.getString("handle");
				handle[1] = rs.getString("type");
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

	public static String nodesToJson(String handle, Graph g, List<Node> nodes) {
		JSONObject j = g.toJSON(handle, nodes);
		// System.out.println(j.toString());
		// json.put(n.getEntity().getText(), j);
		// }
		//
		// JSONObject jroot = new JSONObject();
		// jroot.put(handle, json);
		return j.toString();
	}

	public static void run() {
		String[] handle = popUserFromQueue();

		if (handle == null) {
			return;
		}

		HashMap<String, WikipediaEntity> allEntities = new HashMap<String, WikipediaEntity>();

		Date start = new Date();
		System.out.print("processing " + handle[0] + "...\t");

		if (handle[1].equalsIgnoreCase("t")) {
			Twitter t = new Twitter();
			List<String> tweets = t.fetchTweets(handle[0], 200);
			TopNPExtractor tne = new TopNPExtractor();
			Counter<String> extracted_entities = tne.extract(tweets);
			System.out.println(extracted_entities.toString(10));
			allEntities = tne.resolve(extracted_entities);
		} else if (handle[1].equalsIgnoreCase("f")) {
			Facebook fb = new Facebook(handle[0]);
			List<String> updates = fb.getUserStatusUpdates();
			TopNPExtractor tne = new TopNPExtractor();
			Counter<String> extracted_entities = tne.extract(updates);
			allEntities = tne.resolve(extracted_entities);
		}

		System.out.println("=========Wikipedia Entities==============");
		for (String str : allEntities.keySet()) {
			System.out.println(str + " " + allEntities.get(str).getText() + " "
					+ allEntities.get(str).getWikiminerID());
		}
		System.out.println("=========================================");

		List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		entities.addAll(allEntities.values());
		Graph g = new Graph(entities);
		g.build(7);
		g.printWeights();
		Date end = new Date();
		PerfMetrics.getInstance().addToMetrics(Metric.TOTAL,
				(end.getTime() - start.getTime()));

		printMetrics();

		IRanker ranker = new WeightGraphRanker(g);
		List<Node> topNodes = ranker.getTopRankedNodes(25);
		System.out.println(g.toJSON(handle[0], null));
		setUserInterests(handle[0], g.toJSON(handle[0], null).toString());
		// update status
		updateUser(handle[0]);
		System.out.println("ALL --- DONE");
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
			// Thread.sleep(10000);
		}
	}

}
