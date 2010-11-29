package tathya.text.tokenizer;

import java.util.List;

/**
 * This class represents the base class for all
 * tokenizers
 * @author anand
 */
public interface ITokenizer {

	/**
	 * Tokenize the given input string
	 * @param text	the text to be tokenized
	 * @return the list of tokens generated
	 */
	public List<String> tokenize(String text);
	
}
