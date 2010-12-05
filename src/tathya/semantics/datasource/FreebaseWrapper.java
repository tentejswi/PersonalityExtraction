package tathya.semantics.datasource;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import senna.NounPhraseExtractor;
import senna.RunSenna;
import tathya.db.YahooBOSS;

import com.freebase.api.Freebase;
import com.freebase.json.JSON;

import cs224n.util.Counter;
import cs224n.util.PriorityQueue;

public class FreebaseWrapper {
	private static FreebaseWrapper instance = null;
	private Freebase fb = null;
	
	private FreebaseWrapper() {
		fb = Freebase.getFreebase();
		fb.sign_in("tathya", "tathya");
	}
	
	public static FreebaseWrapper getInstance() {
		if(instance == null) {
			instance = new FreebaseWrapper();
		}
		
		return instance;
	}
	
	public List<JSON> getTypes(String query, double relevance) {
		JSON json = this.fb.search(query);
		//System.out.println(json);
		
		if((json.get("result")).array().size()==0)
			return null;
		List<JSON> typeEntities = ((json.get("result").get(0)).get("type")).array();
		double rScore = (Double) json.get("result").get(0).get("relevance:score").value();
		if(rScore > relevance) {
			return typeEntities;
		}
		
		return null;
	}
	
	public List<String> getNames(String query, double relevance) {
		JSON json = this.fb.search(query);
		//System.out.println(json);
		
		if((json.get("result")).array().size()==0)
			return null;
		List<String> names = new ArrayList<String>();
		for(int i=0; i<json.get("result").array().size(); i++) {
			names.add((String) json.get("result").get(i).get("name").value());
		}
		double rScore = (Double) json.get("result").get(0).get("relevance:score").value();
		if(rScore > relevance) {
			return names;
		}
		
		return null;
	}
	
	public List<String> getRankedNames(String entity, double relevance, String context) {
		List<String> contextPhrases = stanford.NounPhraseExtractor.extract(context);
		ArrayList<String> rankedNames = new ArrayList<String>();
		List<String> names = getNames(entity, relevance);
		PriorityQueue<String> queue = new PriorityQueue<String>();
		
		StringBuffer contextQuery = new StringBuffer();
		for(String c : contextPhrases) {
			contextQuery.append("\"" + c + "\"" + " ");
		}
		
		int entityCount = YahooBOSS.makeQuery('"' + entity + '"');
		System.out.println(contextQuery.toString());
		if(names != null) {
			for(String name : names) {
				int count = YahooBOSS.makeQuery("\"" + name + "\"" + " " + contextQuery.toString());
				queue.add(name, ((double) count/(double) entityCount));
				//System.out.println(t + "\t" + ((double) count/(double) entityCount));
			}
		}
		
		while(queue.hasNext()){
			rankedNames.add(queue.next());
		}
		
		return rankedNames;
	}
	
	public List<String> getRankedTypes(String entity, double relevance) {
		return getRankedTypes(entity, relevance, null);
	}

	public List<String> getRankedTypes(String entity, double relevance, String context) {
		List<String> contextPhrases = stanford.NounPhraseExtractor.extract(context);
		ArrayList<String> rankedTypes = new ArrayList<String>();
		List<JSON> types = getTypes(entity, relevance);
		PriorityQueue<String> queue = new PriorityQueue<String>();
		
		StringBuffer contextQuery = new StringBuffer();
		for(String c : contextPhrases) {
			contextQuery.append("\"" + c + "\"" + " ");
		}
		
		int entityCount = YahooBOSS.makeQuery('"' + entity + '"');
//		System.out.println(contextQuery.toString());
		if(types != null) {
			for(JSON type : types) {
				String t = type.get("id").toString();
				t = t.replace("\"", "");
				t = t.replace("\\", "");
				t = t.replace("_", " ");
				if(t.matches("^.user.*") || t.matches("^.base.*") 
						|| t.matches("^.common.*") || t.matches("^.m/.*")) {
					continue;
				}
				String[] tokens = t.split("/");
				StringBuffer query = new StringBuffer(entity);
				for(String tok : tokens) {
					if(tok.equalsIgnoreCase("")) {
						continue;
					}
					query.append(" \"" + tok + "\"");
				}
				int count = YahooBOSS.makeQuery(query.toString() + " " + contextQuery.toString());
				queue.add(query.toString(), ((double) count/(double) entityCount));
				//System.out.println(t + "\t" + ((double) count/(double) entityCount));
			}
		}
		while(queue.hasNext()){
			rankedTypes.add(queue.next());
		}
		return rankedTypes;
	}
	
	public static void main(String[] args) {
		FreebaseWrapper fb = FreebaseWrapper.getInstance();
		List<String> types = fb.getRankedNames("tagore", 0, "Tagore is the author of Jana gana mana");
		for(String type : types) {
			System.out.println(type);
		}
		
		System.out.println(" \n\n--------\n\n ");
		
		types = fb.getRankedNames("tagore", 0, "Tagore was an actress");
		for(String type : types) {
			System.out.println(type);
		}
	}
	
}
