package com.personalityextractor.url.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.personalityextractor.commons.data.Tweet;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.EntityExtractFactory;
import com.personalityextractor.entity.extractor.IEntityExtractor;
import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
import com.personalityextractor.entity.resolver.ViterbiResolver;

public class URLEntityExtractor {

	ViterbiResolver vr = new ViterbiResolver();
	
	public List<String> extractEntitiesinTitle(String title) {
		IEntityExtractor extractor = EntityExtractFactory.produceExtractor(Extracter.SENNANOUNPHRASE);
		return extractor.extract(title);
	}
	
	public List<WikipediaEntity> resolveEntitiesinTitle(List<String> entities){
		return vr.resolve(entities);
	}
	
	public List<String> readLinesinFile(String file){
		List<String> lines = new ArrayList<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line="";
			while((line= br.readLine())!=null){
				lines.add(line.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return lines;
	}
	
	public static void main(String[] args) {
		URLEntityExtractor uee = new URLEntityExtractor();
		URLContent uc = new URLContent();
		List<String> testUrls = uee.readLinesinFile(args[0]);
		for (String urlStr : testUrls) {
			String urlContent = URLContent.fetchURLContent(urlStr);
			String title = URLContent.fetchTitleString(urlContent);
			System.out.println("title: "+ title);
			Tweet t = new Tweet(title);
			for (String sentence : t.getSentences()) {
				List<String> entities = uee.extractEntitiesinTitle(sentence);
				System.out.println("entities: "+entities);
				List<WikipediaEntity> wikiEntities = uee.resolveEntitiesinTitle(entities);
				for (WikipediaEntity we : wikiEntities) {
					System.out.println(we.getText() + " - "+ we.getWikiminerID());
				}
			}
			System.out.println("\n");
		}
	}
		
}
