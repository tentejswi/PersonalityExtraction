package com.personalityextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.store.WikiminerDB;

public class PGTest {
	
	private static WikiminerDB db;
	static {
		try {
			db = WikiminerDB.getInstance("localhost", "root", "", "wikiminer");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		IEntityExtractor extractor = EntityExtractFactory
				.produceExtractor(Extracter.NOUNPHRASE);
		
		List<String> tweets = new ArrayList<String>();
		tweets.add("Google is a search engine.");
		Set<WikipediaEntity> allEntities = new HashSet<WikipediaEntity>();
		for (String tweet : tweets) {
			List<String> entities = extractor.extract(tweet);
			HashMap<String, ArrayList<WikipediaEntity>> wikiEntities = getWikiSenses(entities);
			for(ArrayList<WikipediaEntity> we : wikiEntities.values()) {
				allEntities.addAll(we);
			}
		}
		
		Set<WikipediaEntity> curEntities = allEntities;
		Set<WikipediaEntity> nxtEntities = new HashSet<WikipediaEntity>();
		int count = 0;
		int limit = 3;
		while(count < limit) {
			for(WikipediaEntity e : curEntities) {
				List<WikipediaEntity> categories = db.getCategories(e.getWikiminerID());
				for(WikipediaEntity c : categories) {
					if(!allEntities.contains(categories)) {
						nxtEntities.add(c);
					}
					System.out.println(e.getWikiminerID() + "\t" + c.getWikiminerID());
				}
			}
			allEntities.addAll(nxtEntities);
			curEntities = nxtEntities;
			nxtEntities = new HashSet<WikipediaEntity>();
			count++;
		}
		
		System.out.println();
	}
	
	public static HashMap<String, ArrayList<WikipediaEntity>> getWikiSenses(
			List<String> entities) {

		HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities = new HashMap<String, ArrayList<WikipediaEntity>>();

		for (String entity : entities) {
			ArrayList<WikipediaEntity> ids = new ArrayList<WikipediaEntity>();
			ids.addAll(db.search(entity));
			tweetEntityTowikiEntities.put(entity, ids);
		}

		return tweetEntityTowikiEntities;
	}

}
