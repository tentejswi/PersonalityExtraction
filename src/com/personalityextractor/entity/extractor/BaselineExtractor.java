/**
 * 
 */
package com.personalityextractor.entity.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author semanticvoid
 *
 */
public class BaselineExtractor implements IEntityExtractor {

	final static List<String> stopWords = Arrays.asList(
			   "a", "an", "and", "are", "as", "at", "be", "but", "by",
			   "for", "if", "in", "into", "is", "it",
			   "no", "not", "of", "on", "or", "such",
			   "that", "the", "their", "then", "there", "these",
			   "they", "this", "to", "we", "was", "will", "with", "most", "needs"
	);
	
	final static List<String> glueWords = Arrays.asList(
			   "of", "&", "an"
	);
	
	/* (non-Javadoc)
	 * @see com.personalityextractor.entity.extractor.IEntityExtractor#extract(java.lang.String)
	 */
	@Override
	public ArrayList<String> extract(String text) {
		ArrayList<String> entities = new ArrayList<String>();
		
		text = text.replaceAll("@[a-zA-Z0-9_]+", "");
		String[] lines = text.split("[.,:()?!'\";]+");
		
		
		int capsCount = 0;
		Pattern p = Pattern.compile("^[A-Z]+.*");
		String[] words = text.split("\\s+|[:]+");
		for (int i=0; i<words.length; i++) {
			String word = words[i];
			
			if (p.matcher(word).matches()) {
				capsCount++;
			}
		}
		double ratio = (double)capsCount/(double)words.length;
		
		int lineNum = 0;
		for(String line : lines) {
			if(line != null) {
				words = line.split("\\s+|[:]+");
				
				// TODO needs data analysis for this threshold
				if(ratio < 0.75) {
					StringBuffer buf = new StringBuffer();
					boolean first = true;
					for (String word : words) {
						if(word.equalsIgnoreCase("")) {
							continue;
						}
						
						if (p.matcher(word).matches() && !first) {
							buf.append(word + " ");
						} else {
							boolean isGlue = false;
							for(String gw : glueWords) {
								if(gw.equalsIgnoreCase(word) && buf.length() > 0) {
									buf.append(word + " ");
									isGlue = true;
									break;
								}
							}
							
							if(isGlue) {
								continue;
							}
							
							if(!buf.toString().trim().equalsIgnoreCase(""))
								entities.add(buf.toString().trim());
							buf = new StringBuffer();
						}
						
						first = false;
					}
					
					if(buf.length() > 0) {
						if(!buf.toString().trim().equalsIgnoreCase(""))
							entities.add(buf.toString().trim());
					}
				}
				
				lineNum++;
			}
		}
		
		return entities;
	}
	
	public static void main(String[] args) {
		IEntityExtractor e = new BaselineExtractor();
		
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
