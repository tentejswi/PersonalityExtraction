package com.personalityextractor.entity.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import wikipedia.Wikiminer;

import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;

import cs224n.util.Counter;
import cs224n.util.CounterMap;
import cs224n.util.MapFactory;

/*
 * resolve entities by finding the maximum spanning tree
 */
public class MSTResolver extends BaseEntityResolver {

	public MSTResolver(IEntityExtractor e){
		super(e);
	}
	
	public static void main(String[] args) {
		//read through data
		String tweet = "";
		
		//extract entities
		MSTResolver msr = new MSTResolver(EntityExtractFactory.produceExtractor(Extracter.CONSECUTIVE_WORDS));
		ArrayList<String> entities = msr.extract(tweet);
		
		//find potential wiki entities for each entity
		HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities= new HashMap<String, ArrayList<WikipediaEntity>>();
		int totalWikiEntities = 0;
		int totalTweetEntities = 0;
		for(String entity : entities){
			
				String xml = Wikiminer.getXML(entity, false);
				if(xml==null)
					continue;
				totalTweetEntities++;
			    ArrayList<String[]> weentities = Wikiminer.getWikipediaSenses(xml, true);
			    ArrayList<WikipediaEntity> ids = new ArrayList<WikipediaEntity>();
			    
			    for(String[] arr : weentities){
			    	WikipediaEntity we = new WikipediaEntity(arr[0], arr[1]);
			    	totalWikiEntities++;
			    	ids.add(we);
			    }
				tweetEntityTowikiEntities.put(entity, ids);
		}
		
		//pre-calculate all compare scores between wikipedia entities. 
		CounterMap<String, String> probabilites = new CounterMap<String, String>();
		Object[] objArray = tweetEntityTowikiEntities.keySet().toArray();
		List<String> twEntities= Arrays.asList(Arrays.copyOf(objArray, objArray.length, String[].class));
		for(int i=0;i< twEntities.size(); i++){
			String twEntity = twEntities.get(i);
			ArrayList<WikipediaEntity> wikiEntities = tweetEntityTowikiEntities.get(twEntity);
			
			for(int j=0; j<wikiEntities.size(); j++){
				//initialize by setting scores between wikiEntities from the same tweetEntity to 0.0;
				for(int k=j; k < wikiEntities.size(); k++){
					probabilites.setCount(wikiEntities.get(j).getWikiminerID(), wikiEntities.get(k).getWikiminerID(), -1.0);
					probabilites.setCount(wikiEntities.get(k).getWikiminerID(), wikiEntities.get(j).getWikiminerID(), -1.0);
				}
				
				//iterate over ALL wikiEntities and get compare score for wikiEntities[i]
				for(int k=i+1; k < twEntities.size(); k++){
					ArrayList<WikipediaEntity> wEntities = tweetEntityTowikiEntities.get(twEntities.get(k));
					for(WikipediaEntity wEntity : wEntities){
						probabilites.setCount(wikiEntities.get(j).getWikiminerID(), wikiEntities.get(k).getWikiminerID(), 
								Wikiminer.compare(wikiEntities.get(j).getWikiminerID(), wikiEntities.get(k).getWikiminerID()));
						probabilites.setCount(wikiEntities.get(k).getWikiminerID(), wikiEntities.get(j).getWikiminerID(), 
								Wikiminer.compare(wikiEntities.get(j).getWikiminerID(), wikiEntities.get(k).getWikiminerID()));

					}
				}
				
			}
		}
		
	}
	
	public HashMap<String, String> buildwikiIDToTweetEntityMap(HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities){
		//assuming that wikipedia ids are unique
		HashMap<String, String> wikiIDToTweetEntity = new HashMap<String, String>();
		Object[] objArray = tweetEntityTowikiEntities.keySet().toArray();
		List<String> twEntities= Arrays.asList(Arrays.copyOf(objArray, objArray.length, String[].class));
		for(int i=0; i <twEntities.size();i++){
			ArrayList<WikipediaEntity> wikiEntities = tweetEntityTowikiEntities.get(twEntities.get(i));
			for(WikipediaEntity we: wikiEntities){
				wikiIDToTweetEntity.put(we.getWikiminerID(), twEntities.get(i));
			}
		}
		return wikiIDToTweetEntity;
	}
	
	static <T, U> CounterMap<T, U> cloneCounterMap(CounterMap<T, U> source){
		CounterMap<T, U> clone = new CounterMap<T, U>();
		for(T key : source.keySet()){
			Counter<U> valueCounter = source.getCounter(key);
			for(U value : valueCounter.keySet()){
				clone.setCount(key, value, valueCounter.getCount(value));
			}
		}
		
		return clone;
	}
	
	public void computeGreatestPath(List<String> twEntities, HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities, CounterMap<String, String> probabilites){
		ArrayList<String> greatestPath = null;
		double greatestPathLength = 0.0;
		
		HashMap<String, String> wikiIDToTweetEntity = buildwikiIDToTweetEntityMap(tweetEntityTowikiEntities);
		
		for(int i=0; i < twEntities.size(); i++){
			ArrayList<WikipediaEntity> curWord_WikiEntities = tweetEntityTowikiEntities.get(twEntities.get(i));	
			for(int j=0; j < curWord_WikiEntities.size();j++){
				CounterMap<String, String> cur_probabilities = cloneCounterMap(probabilites);
				HashSet<String> visitedNodes = new HashSet<String>();				
				ArrayList<String> curPath = new ArrayList<String>();
				double curDistance = 0.0;
				for(WikipediaEntity we : curWord_WikiEntities){
					visitedNodes.add(we.getWikiminerID());
				}
				String curNode = curWord_WikiEntities.get(j).getWikiminerID(); //starting node
				curPath.add(curNode);
				while (visitedNodes.size()!= wikiIDToTweetEntity.size()) {
					String maxProbNextNode = cur_probabilities.getCounter(curNode).argMax();
					while (visitedNodes.contains(maxProbNextNode)) {
						cur_probabilities.setCount(maxProbNextNode, curNode, -1.0);
						maxProbNextNode = cur_probabilities.getCounter(curNode).argMax();
					}

					if (probabilites.getCount(maxProbNextNode,curNode) == -1.0) {
						break;
					}
					
					curPath.add(maxProbNextNode);
					curDistance+=cur_probabilities.getCount(maxProbNextNode,curNode);
					
					//make maxProbNextnode as curNode and put the required nodes to visited
					
				}								
			}			
		}
	}
	@Override
	public ArrayList<String> extract(String line) {
		return this.extractor.extract(line);
	}

}
