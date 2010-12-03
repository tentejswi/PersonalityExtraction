package tathya.semantics.datasource;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

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

	public List<String> getRankedTypes(String entity, double relevance) {
		System.out.println(entity);
		ArrayList<String> rankedTypes = new ArrayList<String>();
		List<JSON> types = getTypes(entity, relevance);
		PriorityQueue<String> queue = new PriorityQueue<String>();
		
		int entityCount = YahooBOSS.makeQuery('"' + entity + '"');
		//System.out.println(entityCount);
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
				int count = YahooBOSS.makeQuery(query.toString());
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
		String entity = "junoon";
		List<JSON> types = fb.getTypes(entity, 20);
		int entityCount = YahooBOSS.makeQuery('"' + entity + '"');
		System.out.println(entityCount);
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
				StringBuffer query1 = new StringBuffer();
				for(String tok : tokens) {
					if(tok.equalsIgnoreCase("")) {
						continue;
					}
					query.append(" \"" + tok + "\"");
					query1.append(" \"" + tok + "\"");
				}
				int count = YahooBOSS.makeQuery(query.toString());
//				int dCount = YahooBOSS.makeQuery(query1.toString());
				System.out.println(t + "\t" + ((double) count/(double) entityCount));
			}
		}
	}
	
}
