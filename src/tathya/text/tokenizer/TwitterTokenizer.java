package tathya.text.tokenizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TwitterTokenizer implements ITokenizer {
	private List<String> sentences = new ArrayList<String>();

	public List<String> tokenize(String text) {
		// clear previous sentences
		sentences = new ArrayList<String>();

		// preprocess
		text = preprocess(text);

		int startIndex = 0;
		int endIndex = 0;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);

			if (ch == '?' || ch == '!') {
				endIndex = i;
				addSentence(text, startIndex, endIndex);
				startIndex = endIndex;
			} else if (ch == '.' || ch == '-' || ch == ':' || ch == ',') {
				if (isDelimiter(text, i)) {
					endIndex = i;
					addSentence(text, startIndex, endIndex+1);
					startIndex = endIndex;
				}
			}
		}

		// if(text.charAt(text.length()-1) == '?' ||
		// text.charAt(text.length()-1) == '!' || text.charAt(text.length()-1)
		// == '.') {
		// addSentence(text, startIndex, text.length());
		// }
		if (endIndex + 1 < text.length() - 1) {
			addSentence(text, startIndex, text.length());
		}
		
		sentences = postprocess(sentences);
		
		return sentences;
	}
	
	@SuppressWarnings("deprecation")
	private List<String> postprocess(List<String> sentences) {
		List<String> result = new ArrayList<String>();
		for(String s : sentences) {
			StringBuffer temp = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				if(!Character.isLetter(ch) && !Character.isDigit(ch) && !Character.isSpace(ch)) {
					if(i == 0 || (s.charAt(i-1) == ' ')) {
						temp.append(ch);
						temp.append(' ');
					} else if(i == s.length()-1 || (s.charAt(i+1) == ' ')) {
						temp.append(' ');
						temp.append(ch);
					} else {
						temp.append(ch);
					}
				} else {
					temp.append(ch);
				}
			}
			result.add(temp.toString());
		}
		
		return result;
	}

	/*
	 * Tweet preprocessing here
	 */
	private String preprocess(String text) {
		text = text.toLowerCase().replaceFirst("^rt [a-z0-9@: ]+:", "");
		text = text.toLowerCase().replaceAll("http://[a-z.0-9+/%&#~\\-_]+",
				"");
		text = text.toLowerCase().replaceAll("#", "");

		return text;
	}

	private void addSentence(String text, int startIndex, int endIndex) {
		try {
			if (startIndex != endIndex - 1) {
				if (startIndex == 0) {
					sentences.add(text.substring(0, endIndex));
				} else if ((endIndex - 1) != (startIndex + 2)
						&& text.charAt(startIndex + 1) == ' ') {
					sentences.add(text.substring(startIndex + 2, endIndex));
				} else {
					sentences.add(text.substring(startIndex + 1, endIndex));
				}
			}
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Exception : " + e.getMessage());
		}
	}

	private boolean isDelimiter(String text, int index) {
		try {
			// Backward
			int startIndex = index;
			int endIndex = index;
			int prevWordStartIndex = -1;
			int nextWordEndIndex = -1;
			char ch;

			// Backward
			do {
				ch = text.charAt(startIndex);
				startIndex--;
			} while (ch != ' ' && startIndex != -1);

			// Backward for previous word
			prevWordStartIndex = startIndex;
			if (prevWordStartIndex >= 0) {
				do {
					ch = text.charAt(prevWordStartIndex);
					prevWordStartIndex--;
				} while (ch != ' ' && prevWordStartIndex != -1);
			}

			// Forward
			do {
				ch = text.charAt(endIndex);
				endIndex++;
			} while (ch != ' ' && endIndex < text.length());

			// Forward for next word
			if (endIndex < text.length()) {
				nextWordEndIndex = endIndex + 1;
				do {
					ch = text.charAt(nextWordEndIndex);
					nextWordEndIndex++;
				} while (ch != ' ' && nextWordEndIndex < text.length());
			}

			String textChunk = text.substring(startIndex + 2, endIndex);
			String prevWord = null;
			if (prevWordStartIndex != startIndex) {
				prevWord = text.substring(prevWordStartIndex + 2,
						startIndex + 1);
			}
			String nextWord = null;
			if (nextWordEndIndex != -1) {
				nextWord = text.substring(endIndex, nextWordEndIndex - 1);
			}

			// Rule 0: If previous word has a period or is the start of
			// sentence, this is not a delimiter
			if (prevWord == null
					|| prevWord.charAt(prevWord.length() - 1) == '.') {
				return false;
			}

			if (text.charAt(index) == '.') {
				// Rule 1 : Numbers
				if (textChunk.matches("[ ]*[0-9]+\\.[0-9]+[ ,]*")) {
					// System.out.println("False : " + textChunk);
					return false;
				}

				// Rule 2 : abbreviations
				if (textChunk.matches("[ ]*[A-Za-z]\\.([A-Za-z0-9]\\.)*[ ,]*")
						|| textChunk.matches("[ ]*[A-Z][^aeiou]+\\.+[ ,]*")) {
					// System.out.println("False : " + textChunk);
					return false;
				}

				// Rule 3 : if there is a space after delimiter then return true
				if ((index != text.length() - 1)
						&& text.charAt(index + 1) == ' ') {
					// System.out.println("true : " + textChunk);
					return true;
				}

				// Rule 4: urls and emails
				if (textChunk.matches("^[a-z]+://.+")
						|| textChunk.matches("^[a-z]+://.+")
						|| textChunk.matches("^[a-zA-Z0-9_.-]+@.+")) {
					return false;
				}

				// Rule 5: consecutive string with periods and no spaces (e.g.
				// www.yahoo.com)
				if (textChunk
						.matches(".*[a-zA-Z0-9-\\.]+\\.[a-zA-Z0-9-\\.]+.*")) {
					return false;
				}

				return true;
			} else {
				if(text.charAt(index-1) == ' ' || text.charAt(index+1) == ' ') {
					return true;
				}
			}
		} catch (Exception e) {
			
		}
		return false;
	}

	public static void main(String args[]) {
		TwitterTokenizer st = new TwitterTokenizer();
		try{ 
			BufferedReader fr = new BufferedReader(new FileReader(args[0]));
			String line;
			while((line = fr.readLine()) != null) {
				List<String> sentences = st
						.tokenize(line);
				for (String s : sentences) {
					System.out.println(s);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
