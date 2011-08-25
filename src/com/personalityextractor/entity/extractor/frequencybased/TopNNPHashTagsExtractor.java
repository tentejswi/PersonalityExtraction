package com.personalityextractor.entity.extractor.frequencybased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sandbox.FileRW;
import sandbox.ReadJSON;

import com.personalityextractor.commons.data.Tweet;
import com.personalityextractor.data.source.Wikiminer;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.entity.extractor.SennaNounPhraseExtractor;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.url.data.URLEntityExtractor;

import cs224n.util.Counter;
import cs224n.util.PriorityQueue;

public class TopNNPHashTagsExtractor implements IFrequencyBasedExtractor {

	double threshold = 1.0;
	HashMap<String, Tweet> tweetIDs = new HashMap<String, Tweet>();
	HashMap<String, ArrayList<String>> entityToTweetIDs = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> tweetToEntities = new HashMap<String, ArrayList<String>>();

	public void buildTweetIDMap(List<String> tweets) {
		int count = 0;
		for (String tw : tweets) {
			count++;
			tweetIDs.put(String.valueOf(count), new Tweet(tw));
		}
	}

	@Override
	public Counter<String> extract(List<String> allTweets) {
		System.out.println(allTweets.size());
		buildTweetIDMap(allTweets);
		Counter<String> entityCounter = new Counter<String>();
		IEntityExtractor extractor = EntityExtractFactory
				.produceExtractor(Extracter.SENNANOUNPHRASE);
		for (String id : tweetIDs.keySet()) {
			
			Tweet tweet = tweetIDs.get(id);
			System.out.println(tweet.getSentences());
			if (!tweetToEntities.containsKey(id)) {
				ArrayList<String> ents = new ArrayList<String>();
				tweetToEntities.put(id, ents);
			}
			for (String s : tweet.getSentences()) {
				List<String> entities = extractor.extract(s);
				for (String entity : entities) {
					tweetToEntities.get(id).add(entity);
					entityCounter.incrementCount(entity, 1.0);
					if (entityToTweetIDs.containsKey(entity)) {
						if (entityToTweetIDs.get(entity).contains(id))
							continue;
						entityToTweetIDs.get(entity).add(id);
					} else {
						ArrayList<String> tweets = new ArrayList<String>();
						tweets.add(id);
						entityToTweetIDs.put(entity, tweets);
					}
				}
			}

			if (tweet.getLinks() != null) {
				for (String link : tweet.getLinks()) {
					// System.out.println(link);
					List<String> entities = URLEntityExtractor
							.extractEntitiesinTitle(link);
					if (entities != null) {
						for (String entity : entities) {
							tweetToEntities.get(id).add(entity);
							entityCounter.incrementCount(entity, 1.0);
							if (entityToTweetIDs.containsKey(entity)) {
								if (entityToTweetIDs.get(entity).contains(id))
									continue;
								entityToTweetIDs.get(entity).add(id);
							} else {
								ArrayList<String> tweets = new ArrayList<String>();
								tweets.add(id);
								entityToTweetIDs.put(entity, tweets);
							}
						}
					}
				}
			}

			for (String hashTag : tweet.getHashTags()) {
				tweetToEntities.get(id).add(hashTag);
				entityCounter.incrementCount(hashTag, 1.0);
				if (entityToTweetIDs.containsKey(hashTag)) {
					if (entityToTweetIDs.get(hashTag).contains(id))
						continue;
					entityToTweetIDs.get(hashTag).add(id);
				} else {
					ArrayList<String> tweets = new ArrayList<String>();
					tweets.add(id);
					entityToTweetIDs.put(hashTag, tweets);
				}

			}

		}

		// get common nouns from tweets containing # tags >1 occurrence
		SennaNounPhraseExtractor sp = new SennaNounPhraseExtractor();
		PriorityQueue<String> keys = entityCounter.asPriorityQueue();
		while(keys.hasNext()) {
			String entity = keys.next();
			if (entity.startsWith("#") && entityCounter.getCount(entity) > 1) {
				for (String id : entityToTweetIDs.get(entity)) {
					Tweet t = tweetIDs.get(id);
					for (String sent : t.getSentences()) {
						
						String sennaoutput = SennaNounPhraseExtractor.getSennaOutput(sent);
						for (String np : sp.getProperNounPhrases(sennaoutput)) {
							entityCounter.incrementCount(np, 1.0);
							tweetToEntities.get(id).add(np);							
							if (entityToTweetIDs.containsKey(np)) {
								if (entityToTweetIDs.get(np).contains(id))
									continue;
								entityToTweetIDs.get(np).add(id);
							} else {
								ArrayList<String> tweets = new ArrayList<String>();
								tweets.add(id);
								entityToTweetIDs.put(np, tweets);
							}
						}
						
						for (String np : sp.getCommonNounPhrases(sennaoutput)) {
							entityCounter.incrementCount(np, 2.0);
							tweetToEntities.get(id).add(np);							
							if (entityToTweetIDs.containsKey(np)) {
								if (entityToTweetIDs.get(np).contains(id))
									continue;
								entityToTweetIDs.get(np).add(id);
							} else {
								ArrayList<String> tweets = new ArrayList<String>();
								tweets.add(id);
								entityToTweetIDs.put(np, tweets);
							}
						}
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

	
	public HashMap<String, WikipediaEntity> resolve(Counter<String> entityCounter) {
		HashMap<String, WikipediaEntity> resolvedEntities = new HashMap<String, WikipediaEntity>();
		for (String entity : entityCounter.keySet()) {
			Counter<WikipediaEntity> senseCounter = new Counter<WikipediaEntity>();
			String entityXML = Wikiminer.getXML(entity, false);
			if (entityXML == null)
				continue;
			System.out.println("Resolving : "+entity);
			for(String id : entityToTweetIDs.get(entity)){
				System.out.println("id "+id);
				List<String> oentities = tweetToEntities.get(id);
				System.out.println(oentities);
				HashSet<String> dentities = new HashSet<String>();
				dentities.addAll(oentities);
				for (String oentity : dentities) {
					if(Wikiminer.getXML(oentity, false) == null)
						continue;
					if (entity.equalsIgnoreCase(oentity))
						continue;
					System.out.println("comparing with "+oentity);
					HashMap<String, WikipediaEntity> relativeBestSenses = Wikiminer.getRelativeBestSenses(entity, oentity);
					if(relativeBestSenses!=null && relativeBestSenses.containsKey(entity)){
						senseCounter.incrementCount(relativeBestSenses.get(entity), 1.0);
					} 
				}
			}
			if(senseCounter.argMax()!=null){
				System.out.println("Resolved");
				resolvedEntities.put(entity, senseCounter.argMax());
			} else{
				List<WikipediaEntity> wikiSenses =  Wikiminer.getWikipediaEntities(entityXML, false);
				resolvedEntities.put(entity, wikiSenses.get(0));
			}
		}
		return resolvedEntities;
	}
	

	public static void main(String[] args) {
		TopNNPHashTagsExtractor tt = new TopNNPHashTagsExtractor();
		ReadJSON rjs = new ReadJSON();
		List<String> json = FileRW.getLinesinFile(args[0]);
		List<String> tweets = new ArrayList<String>();
		for (String son : json) {
			tweets.addAll(rjs.parseJSONArray(son));
		}
		
		Counter<String> entities = tt.extract(tweets);
		System.out.println("Done getting entities..");
		
		PriorityQueue<String> pq = entities.asPriorityQueue();
		while(pq.hasNext()){
			String entity = pq.next();
			System.out.println(entity+" "+entities.getCount(entity));
		}
//		
//		HashMap<String, WikipediaEntity> resolved = tt.resolve(entities);
//		for(String entity : resolved.keySet()){
//			WikipediaEntity we = resolved.get(entity);
//			System.out.println("Entity: "+entity+" Count: "+" Resolution: "+we.getText()+" "+we.getWikiminerID());
//		}
//		
	}

}
