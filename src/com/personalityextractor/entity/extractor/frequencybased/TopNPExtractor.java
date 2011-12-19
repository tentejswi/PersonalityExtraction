package com.personalityextractor.entity.extractor.frequencybased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.personalityextractor.commons.FileRW;
import com.personalityextractor.commons.ReadJSON;

import com.personalityextractor.commons.data.Tweet;
import com.personalityextractor.data.source.Wikiminer;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.url.data.URLEntityExtractor;

import cs224n.util.Counter;
import cs224n.util.CounterMap;
import cs224n.util.PriorityQueue;

public class TopNPExtractor implements IFrequencyBasedExtractor {

	double threshold = 1.0;
	CounterMap<String, String> cooccurence = new CounterMap<String, String>();

	@Override
	public Counter<String> extract(List<String> allTweets) {
		HashSet<String> distTweets = new HashSet<String>(allTweets);
		allTweets = new ArrayList<String>(distTweets);
		List<String> newList = new ArrayList<String>();
	
		for(int i=0; i <allTweets.size(); i++){
			if(!allTweets.get(i).startsWith("@")){
				newList.add(allTweets.get(i));
			}
		}

		allTweets = newList;
		
		Counter<String> entityCounter = new Counter<String>();
		IEntityExtractor extractor = EntityExtractFactory
				.produceExtractor(Extracter.NOUNPHRASE);

		for (String tw : allTweets) {
			System.out.println(tw);
			HashSet<String> entitiesinTweet = new HashSet<String>();
			Tweet tweet = new Tweet(tw);
			for (String sentence : tweet.getSentences()) {
				List<String> entities = extractor.extract(sentence);
					if(entities!=null){
						entitiesinTweet.addAll(entities);
						entityCounter.incrementAll(entities, 1.0);
					}
			}
			
			for (String link : tweet.getLinks()) {
				List<String> entities = URLEntityExtractor
						.extractEntitiesinTitle(link, extractor);
				if (entities != null) {
					entitiesinTweet.addAll(entities);
					entityCounter.incrementAll(entities, 1.0);
				}
			}
			
			entitiesinTweet.addAll(tweet.getHashTags());
			entityCounter.incrementAll(tweet.getHashTags(), 1.0);
			System.out.println(entitiesinTweet);
			
			for (String ent1 : entitiesinTweet) {
				for (String ent2 : entitiesinTweet) {
					if (!ent1.equalsIgnoreCase(ent2)) {
						cooccurence.incrementCount(ent1, ent2, 1.0);
					}
				}
			}	
		}
		
		// apply cutoff
		Counter<String> finalEntityCounter = new Counter<String>();
		for (String entity : entityCounter.keySet()) {
			double count = entityCounter.getCount(entity);
			if (count > this.threshold) {
				finalEntityCounter.setCount(entity, count);
			}
		}

		return finalEntityCounter;
	}

	public HashMap<String, WikipediaEntity> resolve(
			Counter<String> entityCounter) {
		HashMap<String, WikipediaEntity> resolvedEntities = new HashMap<String, WikipediaEntity>();
		for (String entity : entityCounter.keySet()) {
			Counter<WikipediaEntity> senseCounter = new Counter<WikipediaEntity>();
			String entityXML = Wikiminer.getXML(entity, false);
			if (entityXML == null)
				continue;

			List<String> oentities = new ArrayList<String>();
			if (cooccurence.keySet().contains(entity)) {
				PriorityQueue<String> sorted = cooccurence.getCounter(entity)
						.asPriorityQueue();
				int count = 0;
				while (sorted.hasNext() && count < 5) {
					oentities.add(sorted.next());
					count++;
				}
			} 

			for (String oentity : oentities) {
				if (Wikiminer.getXML(oentity, false) == null)
					continue;
				if (entity.equalsIgnoreCase(oentity))
					continue;
				// System.out.println("comparing with " + oentity);
				HashMap<String, WikipediaEntity> relativeBestSenses = Wikiminer
						.getRelativeBestSenses(entity, oentity);
				if (relativeBestSenses != null
						&& relativeBestSenses.containsKey(entity)) {
					senseCounter.incrementCount(relativeBestSenses.get(entity),
							1.0);
				}
			}

			if (senseCounter.argMax() != null) {
				WikipediaEntity we = senseCounter.argMax();
				we.count = entityCounter.getCount(entity);
				resolvedEntities.put(entity, we);
			} else {
				List<WikipediaEntity> wikiSenses = Wikiminer
						.getWikipediaEntities(entityXML, false);
				resolvedEntities.put(entity, wikiSenses.get(0));
			}
		}
		return resolvedEntities;
	}

	public static void main(String[] args) {

		TopNPExtractor tt = new TopNPExtractor();

		ReadJSON rjs = new ReadJSON();
		List<String> json = FileRW.getLinesinFile(args[0]);
		List<String> tweets = new ArrayList<String>();
		for (String son : json) {
			if (son.length() == 0)
				continue;
			tweets.addAll(rjs.parseJSONArray(son));
		}

		Counter<String> entities = tt.extract(tweets);
		System.out.println("Done getting entities..");

		PriorityQueue<String> pq = entities.asPriorityQueue();
		while (pq.hasNext()) {
			String entity = pq.next();
			System.out.println(entity + " TotalCount:"
					+ entities.getCount(entity));
			PriorityQueue<String> pq1 = tt.cooccurence.getCounter(entity)
					.asPriorityQueue();
			int count = 0;
			while (pq1.hasNext() && count < 5) {
				count++;
				String key = pq1.next();
				System.out.println(key + " Cooccurence: "
						+ tt.cooccurence.getCount(entity, key));
			}
			System.out.println("\n");
		}

		 HashMap<String, WikipediaEntity> resolved = tt.resolve(entities);
		 for (String entity : resolved.keySet()) {
		 WikipediaEntity we = resolved.get(entity);
		 System.out.println("Entity: " + entity + " Count: "
		 + entities.getCount(entity) + " Resolution: "
		 + we.getText() + " " + we.getWikiminerID());
		 }

	}

}
