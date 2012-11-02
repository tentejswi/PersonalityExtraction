package com.personalityextractor.url.HTMLParser.Readability;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Readability {

	/**
	 * @param args
	 */

	String pathToPythonScript = "/Users/tejaswi/Documents/workspace/PersonalityExtraction/src/com/personalityextractor/url/HTMLParser/Readability/ReadabilityHTMLParser.py";

	public String removeTags(String html) {
		StringBuffer plain = new StringBuffer();
		char[] chars = html.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '<') {
				while (chars[i] != '>') {
					i++;
				}
				plain.append(" ");
				//i++;
			} else{
				plain.append(chars[i]);
			}
		}
		return plain.toString();
	}

	public String removeHTML(String url) {
		StringBuffer plainText = new StringBuffer();
		try {
			String[] callAndArgs = { "python", pathToPythonScript, url };
			Process p = Runtime.getRuntime().exec(callAndArgs);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			// read the output
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				plainText.append(s);
				plainText.append("\n");
			}

			// read any errors
			while ((s = stdError.readLine()) != null) {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return removeTags(plainText.toString());
	}

	public static void main(String[] args) {
	//	HTMLParser hp = new HTMLParser();
		Readability r = new Readability();
		System.out.println(r.removeTags((r.removeHTML(args[0]))));
	}

}
