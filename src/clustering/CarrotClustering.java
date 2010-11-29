package clustering;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import tathya.text.tokenizer.TwitterTokenizer;
import twitter.ConsoleFormatter;
import twitter.HTTPRequest;

public class CarrotClustering {

	public static String extractText(JSONObject jsonObject) {
		JSONObject jObj = JSONObject.fromObject(jsonObject);
		if (jObj.containsKey("text")) {
			return (String) jObj.get("text");
		} else {
			return null;
		}
	}

	public static void main(String args[]) {
		HTTPRequest htr = new HTTPRequest();
		String query = "http://twitter.com/statuses/user_timeline/rsumbaly.json?count=300";
		JSONArray jsonArray = null;
		String response = htr.getHTTPResponse(query);
		jsonArray = (JSONArray) JSONSerializer.toJSON(response);
		Iterator<JSONObject> itr = jsonArray.iterator();
		final ArrayList<Document> documents = new ArrayList<Document>();
		while (itr.hasNext()) {
			while (itr.hasNext()) {
				String tweetText = extractText(itr.next());
				TwitterTokenizer tweetTokenizer = new TwitterTokenizer();
				for (String cleanTweet : tweetTokenizer.tokenize(tweetText)) {
					documents.add(new Document("", cleanTweet));
				}
			}
		}
        final Controller controller = ControllerFactory.createSimple();
        final ProcessingResult byTopicClusters = controller.process(documents, null, LingoClusteringAlgorithm.class);
        final List<Cluster> clustersByTopic = byTopicClusters.getClusters();            
        ConsoleFormatter.displayClusters(clustersByTopic);
	}
}
