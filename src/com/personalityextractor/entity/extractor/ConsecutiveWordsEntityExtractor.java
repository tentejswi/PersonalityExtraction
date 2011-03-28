/**
 * 
 */
package com.personalityextractor.entity.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author semanticvoid
 *
 */
public class ConsecutiveWordsEntityExtractor implements IEntityExtractor {

	final static List<String> stopWords = Arrays.asList(
			   "a", "an", "and", "are", "as", "at", "be", "but", "by",
			   "for", "if", "in", "into", "is", "it",
			   "no", "not", "of", "on", "or", "such",
			   "that", "the", "their", "then", "there", "these",
			   "they", "this", "to", "was", "will", "with", "most", "needs"
	);
	
	/* (non-Javadoc)
	 * @see com.personalityextractor.entity.extractor.IEntityExtractor#extract(java.lang.String)
	 */
	@Override
	public ArrayList<String> extract(String line) {
		if(line == null) {
			return null;
		}
		
		ArrayList<String> allEntities = new ArrayList<String>();
		String[] words = line.split("[ :;'\"?/><,\\.!@#$%^&()-+=~`{}|]+");
		ArrayList<String> filteredWords = new ArrayList<String>();
		for(String word : words) {
			if(word.length() <= 0) {
				filteredWords.add(null);
				continue;
			}
			boolean isStop = false;
			for(String sWord : stopWords) {
				if(word.equalsIgnoreCase(sWord)) {
					isStop = true;
					break;
				}
			}
			if(isStop) {
				filteredWords.add(null);
				continue;
			}		
			filteredWords.add(word.toLowerCase());
		}
		
		ArrayList<String> consecutiveWords = new ArrayList<String>();
		for(String fw : filteredWords) {
			if(fw == null) {
				ArrayList<String> entities = formEntities(consecutiveWords);
				allEntities.addAll(entities);
				for(String entity : entities) {
//					System.out.println(entity);
				}
				consecutiveWords = new ArrayList<String>();
				continue;
			}			
			consecutiveWords.add(fw);
		}
		
		if(consecutiveWords.size() > 0) {
			ArrayList<String> entities = formEntities(consecutiveWords);
			allEntities.addAll(entities);
			for(String entity : entities) {
//				System.out.println(entity);
			}
		}
		return allEntities;
	}
	
	private ArrayList<String> formEntities(ArrayList<String> words) {
		ArrayList<String> entities = new ArrayList<String>();
		for(int i=0; i<words.size(); i++) {
			StringBuffer buf = new StringBuffer();
			for(int j=i; j<words.size(); j++) {
				buf.append(words.get(j) + " ");
				String entity = buf.toString().trim();
				entities.add(entity);
			}
		}
		
		return entities;
	}

	public static void main(String[] args) {
		IEntityExtractor e = new ConsecutiveWordsEntityExtractor();
		
		List<String> sentences = Arrays.asList(
				   "Rest in Peace!",
				   "New blog post: 50 days with Google Nexus S: http://www.venu.in/blog/?p=314",
				   "@dpolice Hard to say. If the user is geeky - Nexus S . Otherwise iPhone 4 . :) Both are great phones.",
				   "About to embark on the unthinkable... Driving to New York City. Wish me luck.",
				   "Best part of The Hurt Locker ? The lack of background music! Silence speaks quite loudly in this movie.",
				   "I'm playing the Age of Empires.",
				   "iTunes / ipod ecosystem needs to learn a thing or two from Doggcatcher. Seriously. This is the best solution for podcast listeners out there.",
				   "loved India New Land of Opportunity on Boxee http://bit.ly/ghYcfj",
				   "@vjvegi Why this comment about Pakistan all of a sudden? :)",
				   "Swapped the Elantra with a Santa Fe to deal with all that snow on the roads."
				   
		);
		
		for(String sentence : sentences) {
			List<String> entities = e.extract(sentence);
			
			for(String entity : entities) {
				System.out.println("'" + entity + "'");
			}
		}
	}
}
