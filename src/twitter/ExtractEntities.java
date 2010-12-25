package twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import cs224n.util.Counter;
import cs224n.util.PriorityQueue;

import net.sf.json.JSONObject;

import stanford.NounPhraseExtractor;
import tathya.text.tokenizer.TwitterTokenizer;

public class ExtractEntities {

	static final HashSet<String> stopWords = new HashSet<String>();
	static {
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(
							"/Users/tejaswi/Documents/workspace/PersonalityExtraction/data/chimps_list-of-english-stopwords-2010-07-01_00-50-22/english_stopwords.tsv"));
			String line = "";
			while ((line = br.readLine()) != null) {
				stopWords.add(line.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static HashSet<String> getCapitalizedWords(String token) {
		HashSet<String> capitalWords = new HashSet<String>();
		try {
			Pattern p = Pattern.compile("^[A-Z]+.*");
			String[] split = token.split("\\s+");
			for (String s : split) {
				if (p.matcher(s).matches()) {
					capitalWords.add(s.toLowerCase());
				}
			}
			return capitalWords;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<String> getAllEntities(){
		HashMap<String, Entity> allEntities = new HashMap<String, Entity>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("data/thathoo.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("data/thathoo_entities.txt"));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter("data/thathoo_statistics.txt"));

			String line = "";
			Counter<String> nPhraseCounter = new Counter<String>();
			Counter<String> capitalsCounter = new Counter<String>();
			while ((line = br.readLine()) != null) {
				line = line.replaceAll("RT", "");
				TwitterTokenizer tweetTokenizer = new TwitterTokenizer();
				for (String token : tweetTokenizer.tokenize(line)) {
					token = token.trim();
					token = token.replaceAll("( [^a-zA-Z0-9\\.]) | ( [^a-zA-Z0-9\\.] ) | ([^a-zA-Z0-9\\.] )"," ");
					ArrayList<String> nPhrases = new ArrayList<String>();
					HashSet<String> capitalWords = new HashSet<String>();
					try {
						Pattern p = Pattern.compile("^[A-Z]+.*");
						String[] split = token.split("\\s+");
						for (String s : split) {
							if (p.matcher(s).matches() && !stopWords.contains(s.toLowerCase())) {
								capitalWords.add(s.toLowerCase());
								capitalsCounter.incrementCount(s.toLowerCase(), 1.0);
								if(allEntities.containsKey(s.trim())){
									Entity e = allEntities.get(s.trim());
									e.tweets.add(token);
									allEntities.put(s.trim(), e);
								} else{
									Entity e = new Entity(s.trim());
									e.tweets.add(token);
									allEntities.put(s.trim(), e);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					bw.write("===============================================\n");
					bw.write(token + "\n");
					System.out.println("token: " + token);
					for (String np : NounPhraseExtractor.extract(token)) {
						if (!stopWords.contains(np.trim().toLowerCase())){
							nPhrases.add(np.trim());
							nPhraseCounter.incrementCount(np.trim(), 1.0);
							if(allEntities.containsKey(np.trim())){
								Entity e = allEntities.get(np.trim());
								e.tweets.add(token);
								allEntities.put(np.trim(), e);
							} else{
								Entity e = new Entity(np.trim());
								e.tweets.add(token);
								allEntities.put(np.trim(), e);
							}
						}
					}
					bw.write("===============================================\n");
					bw.write("Noun-Phrases: " + nPhrases.toString() + "\n");
					// HashSet<String> capitalWords =
					// getCapitalizedWords(token);
					if (capitalWords == null) {
						bw.write("No capitals\n\n");
					} else {
						bw.write("Capitals: " + capitalWords.toString()	+ "\n\n");
					}
				}

				bw.flush();
				if (true)
					continue;
			}
			
			PriorityQueue<String> nPhraseQueue = nPhraseCounter.asPriorityQueue();
			PriorityQueue<String> capitalQueue = capitalsCounter.asPriorityQueue();
			while(nPhraseQueue.hasNext()){
				String np = nPhraseQueue.next();
				bw1.write(np+" "+nPhraseCounter.getCount(np)+"\n");					
			}
			bw1.write("=========================================================\n");
			while(capitalQueue.hasNext()){
				String cap = capitalQueue.next();
				bw1.write(cap+" "+capitalsCounter.getCount(cap)+"\n");
				allEntities.add(cap);
			}
			bw1.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allEntities;
	}

	public static void main(String[] args) {
		ExtractEntities ee = new ExtractEntities();
		ee.getAllEntities();
	}

}
