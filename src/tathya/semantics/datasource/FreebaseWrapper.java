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
		if (instance == null) {
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
	

	@SuppressWarnings("unchecked")
	public List<JSON> getTypes(JSON entity) {
		// if((entity.get("types")).array().size()==0)
		// return null;

		List<JSON> types = (entity.get("type")).array();
		// double rScore = (Double)
		// entity.get("result").get(0).get("relevance:score").value();
		// if(rScore > relevance) {
		return types;
		// }

		// return null;
	}

	public List<JSON> getEntities(String query, double relevance) {
		// JSON options = JSON.o("limit", "50");
		JSON json = this.fb.search(query);
		System.out.println(json);

		if ((json.get("result")).array().size() == 0)
			return null;
		List<JSON> entities = new ArrayList<JSON>();
		for (int i = 0; i < json.get("result").array().size(); i++) {
			String t = json.get("result").get(i).get("id").toString();
			t = t.replace("\"", "");
			t = t.replace("\\", "");
			t = t.replace("_", " ");
			if (t.matches("^.user.*") || t.matches("^.base.*")
					|| t.matches("^.common.*") || t.matches("^.m/.*") || t.matches("^.soft/.*")) {
				continue;
			}
			entities.add((JSON) json.get("result").get(i));
		}
		double rScore = (Double) json.get("result").get(0)
				.get("relevance:score").value();
		if (rScore > relevance) {
			return entities;
		}

		return null;
	}

	public List<JSON> getRankedEntities(String entityStr, double relevance,
			String context) {
		List<String> contextPhrases = stanford.NounPhraseExtractor
				.extract(context);
		ArrayList<JSON> rankedEntities = new ArrayList<JSON>();
		List<JSON> entities = getEntities(entityStr, relevance);
		PriorityQueue<JSON> queue = new PriorityQueue<JSON>();

		StringBuffer contextQuery = new StringBuffer();
		for (String c : contextPhrases) {
			contextQuery.append("\"" + c + "\"" + " ");
		}

		int entityCount = YahooBOSS.makeQuery('"' + entityStr + '"');
		System.out.println(contextQuery.toString());
		if (entities != null) {
			for (JSON entity : entities) {
				String t = (String) entity.get("id").value();
				t = t.replace("\"", "");
				t = t.replace("\\", "");
				t = t.replace("_", " ");
				String[] tokens = t.split("/");
				int count = YahooBOSS.makeQuery("\""
						+ tokens[tokens.length - 1] + "\"" + " \""
						+ (String) entity.get("name").value() + " "
						+ contextQuery.toString());
				queue.add(entity, ((double) count / (double) entityCount));
				// System.out.println(t + "\t" + ((double) count/(double)
				// entityCount));
			}
		}

		while (queue.hasNext()) {
			rankedEntities.add(queue.next());
		}

		return rankedEntities;
	}

	public List<String> getRankedTypes(JSON entity) {
		return getRankedTypes(entity, null);
	}

	/*
	 * Sort the freebase types in descending order of relevance 
	 */
	public List<String> getRankedTypes(String entity, double relevance) {
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
				String origQuery = query.toString();
				for(String tok : tokens) {
					if(tok.equalsIgnoreCase("")) {
						continue;
					}
					query.append(" \"" + tok + "\"");
				}

				int count = YahooBOSS.makeQuery(query.toString());
				queue.add(query.toString().substring(origQuery.length(), query.length()).trim(), ((double) count/(double) entityCount));
				//System.out.println(t + "\t" + ((double) count/(double) entityCount));
			}
		}
		while(queue.hasNext()){
			rankedTypes.add(queue.next());
		}
		return rankedTypes;

	}


	public List<String> getRankedTypes(JSON entity, String context) {
		List<String> contextPhrases = stanford.NounPhraseExtractor.extract(context);
		ArrayList<String> rankedTypes = new ArrayList<String>();
		List<JSON> types = getTypes(entity);
		PriorityQueue<String> queue = new PriorityQueue<String>();

		StringBuffer contextQuery = new StringBuffer();
		for (String c : contextPhrases) {
		contextQuery.append("\"" + c + "\"" + " ");
		}

		int entityCount = YahooBOSS.makeQuery('"' + (String) entity.get("name")
		.value() + '"');
		// System.out.println(contextQuery.toString());
		if (types != null) {
		for (JSON type : types) {
		String t = type.get("id").toString();
		t = t.replace("\"", "");
		t = t.replace("\\", "");
		t = t.replace("_", " ");
		if (t.matches("^.user.*") || t.matches("^.base.*")
		|| t.matches("^.common.*") || t.matches("^.m/.*")) {
		continue;
		}
		String[] tokens = t.split("/");
		StringBuffer query = new StringBuffer((String) entity.get(
		"name").value());
		for (String tok : tokens) {
		if (tok.equalsIgnoreCase("")) {
		continue;
		}
		query.append(" \"" + tok + "\"");
		}
		int count = YahooBOSS.makeQuery(query.toString() + " "
		+ contextQuery.toString());
		queue.add(query.toString(),
		((double) count / (double) entityCount));
		// System.out.println(t + "\t" + ((double) count/(double)
		// entityCount));
		}
		}
		while (queue.hasNext()) {
		rankedTypes.add(queue.next());
		}
		return rankedTypes;
	}

	public static void main(String[] args) {
		FreebaseWrapper fb = FreebaseWrapper.getInstance();
		String query = "giants";
		String context1 = "giants are a great baseball team";
		String context2 = "giants are a great football team";

		List<JSON> entities = fb.getRankedEntities(query, 0, context1);
		for (int i = 0; i < entities.size(); i++) {
			JSON entity = entities.get(i);
			System.out.println("Entity:\t"
					+ (String) entity.get("name").value() + "\t"
					+ (String) entity.get("id").value());
			List<String> types = fb.getRankedTypes(entity, context1);
			for(String type : types) {
				System.out.println(type);
			}
			
			if(i == 3) {
				break;
			}

			System.out.println("\n--------------------------------\n");
		}

		System.out.println(" \n\n################################\n\n ");

		entities = fb.getRankedEntities(query, 0, context2);
		for (int i = 0; i < entities.size(); i++) {
			JSON entity = entities.get(i);
			System.out.println("Entity:\t"
					+ (String) entity.get("name").value() + "\t"
					+ (String) entity.get("id").value());
			List<String> types = fb.getRankedTypes(entity, context2);
			for(String type : types) {
				System.out.println(type);
			}
			
			if(i == 3) {
				break;
			}

			System.out.println("\n--------------------------------\n");
		}
	}

}
