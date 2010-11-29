package twitter;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.synthetic.ByUrlClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;

import com.freebase.json.JSON;

import cs224n.util.Counter;
import cs224n.util.PriorityQueue;

import senna.NounPhraseExtractor;
import senna.RunSenna;
import tathya.semantics.Event;
import tathya.semantics.datasource.FreebaseWrapper;
import tathya.text.tokenizer.TwitterTokenizer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ProcessTweets {

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
		PreprocessTwitterData ptd = new PreprocessTwitterData();
		FreebaseWrapper fb = FreebaseWrapper.getInstance();
		Counter<String> fbClasses = new Counter<String>();
		Counter<String> entitiesCounter = new Counter<String>();
		RunSenna rs = new RunSenna();
		HashMap<String, HashSet<String>> classToEntities = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> entityToClasses = new HashMap<String, HashSet<String>>();
		NounPhraseExtractor npExtractor = new NounPhraseExtractor();
		ArrayList<String> tweets = new ArrayList<String>();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("classes.txt"));
			String query = "http://twitter.com/statuses/user_timeline/rsumbaly.json?count=300";
			BufferedWriter bw1 = new BufferedWriter(new FileWriter("tweets.txt"));
			JSONArray jsonArray = null;
			String response = htr.getHTTPResponse(query);
			//System.out.println("line: " + response);
			jsonArray = (JSONArray) JSONSerializer.toJSON(response);
			Iterator<JSONObject> itr = jsonArray.iterator();
//			fb.populateDomains();
            final ArrayList<Document> documents = new ArrayList<Document>();
			while (itr.hasNext()) {
				//System.out.println("Tweet text: " + extractText(itr.next()));
				String tweetText = extractText(itr.next());
				//System.out.println(tweetText);
				//bw1.write(tweetText+"\n");
				TwitterTokenizer tweetTokenizer = new TwitterTokenizer();
				for(String cleanTweet : tweetTokenizer.tokenize(tweetText)){
					//System.out.println(cleanTweet+"\n\n");
					documents.add(new Document("",cleanTweet));
				}
//				String cleanTweet = ptd.cleanText(tweetText);
//				String[] tweetLine = cleanTweet.split("\n");
//				for(String line : tweetLine){
//					String sennaOut = rs.getSennaOutput(line.trim());
//					ArrayList<String> sennaLines = new ArrayList<String>();
//					for(String sennaLine: sennaOut.split("\n")){
//						sennaLines.add(sennaLine.trim());
//					}					
//					//get all entities in the line
//					Event e = new Event(sennaLines);
//					//ArrayList<String> entities = (ArrayList<String>) e.getEntities();
//					ArrayList<String> entities = npExtractor.getNounPhrases(sennaOut);
//					//ArrayList<String> features = new ArrayList<String>();
//					for(String entity : entities){
//						entitiesCounter.incrementCount(entity, 1.0);
//						List<JSON> types = fb.getTypes(entity,70);
//						if(types != null) {
//							for(JSON type : types) {
//								if(type.get("id")==null)
//									continue;
//								String ID = type.get("id").string().trim();
//								for (String fbType : ID.split("/")) {
//									if(!fb.domains.contains(fbType))
//										continue;
//									fbClasses.incrementCount(fbType, 1.0);
//									if (classToEntities.containsKey(fbType)) {
//										classToEntities.get(fbType).add(entity);
//									} else {
//										HashSet<String> entitiesInText = new HashSet<String>();
//										entitiesInText.add(entity);
//										classToEntities.put(fbType,
//												entitiesInText);
//									}
//									if (entityToClasses.containsKey(entity)) {
//										entityToClasses.get(entity).add(fbType);
//									} else {
//										HashSet<String> classesInText = new HashSet<String>();
//										classesInText.add(fbType);
//										entityToClasses.put(entity,
//												classesInText);
//									}
//								}
//							}
//						}
//					}
					
//				}
				
			}
			bw1.flush();
			
            /* A controller to manage the processing pipeline. */
            final Controller controller = ControllerFactory.createSimple();

            /*
             * Perform clustering by topic using the Lingo algorithm. Lingo can 
             * take advantage of the original query, so we provide it along with the documents.
             */
            final ProcessingResult byTopicClusters = controller.process(documents, null, 
                LingoClusteringAlgorithm.class);
            final List<Cluster> clustersByTopic = byTopicClusters.getClusters();
            
            
            /* Perform clustering by domain. In this case query is not useful, hence it is null. */
//            final ProcessingResult byDomainClusters = controller.process(documents, null,
//                ByUrlClusteringAlgorithm.class);
//            final List<Cluster> clustersByDomain = byDomainClusters.getClusters();
//            // [[[end:clustering-document-list]]]
            
            ConsoleFormatter.displayClusters(clustersByTopic);
//            ConsoleFormatter.displayClusters(clustersByDomain);

			
			BufferedWriter bwfbClasses = new BufferedWriter(new FileWriter("sortedClasses.txt"));
			PriorityQueue<String> sorted = fbClasses.asPriorityQueue();
			while(sorted.hasNext()){
				String classLabel = sorted.next();
				bwfbClasses.write("FreebaseClass: "+classLabel+" Count: "+fbClasses.getCount(classLabel)+" Entities: ");
				for(String s1 : classToEntities.get(classLabel)){
					bwfbClasses.write("\""+s1+"\""+" ");
				}
				bwfbClasses.write("\n\n");	
			}
			bwfbClasses.flush();
			
			BufferedWriter bwEntities = new BufferedWriter(new FileWriter("sortedEntities.txt"));
			PriorityQueue<String> sortedEntities = entitiesCounter.asPriorityQueue();
			while(sortedEntities.hasNext()){
				String classLabel = sortedEntities.next();
				if(entityToClasses.get(classLabel)==null){
					continue;
				}
				bwEntities.write("Entity: "+classLabel+" Count: "+entitiesCounter.getCount(classLabel)+" Classes: ");
				for(String s1 : entityToClasses.get(classLabel)){
					bwEntities.write("\""+s1+"\""+" ");
				}
				bwEntities.write("\n");
				
			}
			bwEntities.flush();
			
			for(String s : fbClasses.keySet()){
				bw.write("FreebaseClass: "+s+" Count: "+fbClasses.getCount(s)+" Entitites: ");
				for(String s1 : classToEntities.get(s)){
					bw.write("\""+s1+"\" ");
				}
				bw.write("\n");
			}
			bw.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
