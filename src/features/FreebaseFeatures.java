package features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import senna.NounPhraseExtractor;
import senna.RunSenna;
import tathya.semantics.datasource.FreebaseWrapper;

import com.freebase.json.JSON;

import cs224n.util.Counter;


public class FreebaseFeatures implements IFeatures {
	NounPhraseExtractor nounPhraseExtractor = new NounPhraseExtractor();
	
	public void getTopFeatures(){
		Counter<String> fbClasses = new Counter<String>();
		Counter<String> entitiesCounter = new Counter<String>();
		RunSenna rs = new RunSenna();
		HashMap<String, HashSet<String>> classToEntities = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> entityToClasses = new HashMap<String, HashSet<String>>();
		
//		ArrayList<String> entities = npExtractor.getNounPhrases(sennaOut);
//		//ArrayList<String> features = new ArrayList<String>();
//		for(String entity : entities){
//			entitiesCounter.incrementCount(entity, 1.0);
//			List<JSON> types = fb.getTypes(entity,70);
//			if(types != null) {
//				for(JSON type : types) {
//					if(type.get("id")==null)
//						continue;
//					String ID = type.get("id").string().trim();
//					for (String fbType : ID.split("/")) {
//						if(!fb.domains.contains(fbType))
//							continue;
//						fbClasses.incrementCount(fbType, 1.0);
//						if (classToEntities.containsKey(fbType)) {
//							classToEntities.get(fbType).add(entity);
//						} else {
//							HashSet<String> entitiesInText = new HashSet<String>();
//							entitiesInText.add(entity);
//							classToEntities.put(fbType,
//									entitiesInText);
//						}
//						if (entityToClasses.containsKey(entity)) {
//							entityToClasses.get(entity).add(fbType);
//						} else {
//							HashSet<String> classesInText = new HashSet<String>();
//							classesInText.add(fbType);
//							entityToClasses.put(entity,
//									classesInText);
//						}
//					}
//				}
//			}
//		}

	}
	
	
	public List<String> getFeatures(String text){
		ArrayList<String> freebaseFeatures = new ArrayList<String>();
		FreebaseWrapper fb = FreebaseWrapper.getInstance();
		
		//call senna
		RunSenna rs = new RunSenna();
		String sennaOut = rs.getSennaOutput(text.trim());
		
		//get Noun Phrases
		ArrayList<String> nounPhrases = nounPhraseExtractor.getNounPhrases(sennaOut);
		for (String entity : nounPhrases) {
			List<JSON> types = fb.getTypes(entity, 70);
			if (types != null) {
				for (JSON type : types) {
					if (type.get("id") == null)
						continue;
					String ID = type.get("id").string().trim();
					for (String fbType : ID.split("/")) {
						if (!fb.domains.contains(fbType))
							continue;
						freebaseFeatures.add(fbType);
					}
				}
			}
		}		
		return freebaseFeatures;
		
	}
}
