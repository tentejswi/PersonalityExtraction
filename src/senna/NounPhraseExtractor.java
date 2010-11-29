package senna;

import java.util.ArrayList;

import tathya.semantics.Event;
import twitter.PreprocessTwitterData;

public class NounPhraseExtractor {

	public ArrayList<String> getNounPhrases(String sennaOutput) {
		SennaLines sl = new SennaLines();
		ArrayList<String> nounPhrases = new ArrayList<String>();
		String[] lineArr = sennaOutput.split("\n");

		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> chunkerTokens = new ArrayList<String>();
		for (String line : lineArr) {
			String[] tokens = line.trim().split("[ \t]+");
			if (tokens.length < 3)
				continue;
			words.add(tokens[0].trim());
			chunkerTokens.add(tokens[2].trim());
		}
		for (int i = 0; i < chunkerTokens.size(); i++) {
			if (chunkerTokens.get(i).equalsIgnoreCase("s-np")) {
				nounPhrases.add(words.get(i));
			} else if (chunkerTokens.get(i).equalsIgnoreCase("b-np")) {
				String np = "";
				while (!chunkerTokens.get(i).equalsIgnoreCase("e-np")) {
					np += (words.get(i) + " ");
					i++;
				}
				np += (words.get(i) + " ");
				nounPhrases.add(np.trim());
			}
		}
		return nounPhrases;
	}

	public static void main(String[] args) {
		NounPhraseExtractor npe = new NounPhraseExtractor();
		try {
			PreprocessTwitterData ptd = new PreprocessTwitterData();
			RunSenna rs = new RunSenna();
			String tweet = "#lazyweb There are a lot of StackOverflow like sites for stats now. What is the best site for asking statistical questions? Quora?";
			String cleaned = ptd.cleanText(tweet);
			System.out.println("cleaned: "+cleaned);
			for(String s : cleaned.split("\n")){
			String sennaOutput = rs.getSennaOutput(s.trim());
			ArrayList<String> nPhrases = npe.getNounPhrases(sennaOutput);
			System.out.println("NPs");
			for(String nPhrase : nPhrases){
				System.out.println(nPhrase);
			}
		}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
