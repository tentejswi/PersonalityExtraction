package tathya.text.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the tokenizer that breaks the input into
 * Word tokens.
 * @author anand
 *
 */
public class WordTokenizer implements ITokenizer {

	/* (non-Javadoc)
	 * @see dygest.text.tokenizer.Tokenizer#tokenize(java.lang.String)
	 */
	public List<String> tokenize(String text) {
		return Arrays.asList(text.split("[ ,\\t\\n\\r\\f\\.;:\"\'-]+"));
	}

}
