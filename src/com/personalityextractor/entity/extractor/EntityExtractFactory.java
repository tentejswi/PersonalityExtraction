/**
 * 
 */
package com.personalityextractor.entity.extractor;

/**
 * @author semanticvoid
 *
 */
public class EntityExtractFactory {

	public enum Extracter {
		BASELINE, CONSECUTIVE_WORDS, NOUNPHRASE;
	}
	
	public static IEntityExtractor produceExtractor(Extracter e) {
		if(e == Extracter.BASELINE) {
			return new BaselineExtractor();
		} else if(e == Extracter.CONSECUTIVE_WORDS) {
			return new ConsecutiveWordsEntityExtractor();
		} else if(e == Extracter.NOUNPHRASE) {
			return new NounPhraseExtractor();
		} else {
			// default
			return new BaselineExtractor();
		}
	}
}
