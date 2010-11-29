package tathya.semantics.datasource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;

import com.freebase.api.Freebase;
import com.freebase.json.JSON;

public class FreebaseWrapper {
	private static FreebaseWrapper instance = null;
	private Freebase fb = null;
	public HashSet<String> domains = new HashSet<String>();
	
	private FreebaseWrapper() {
		fb = Freebase.getFreebase();
		fb.sign_in("tathya", "tathya");
	}
	
	public void populateDomains(HashSet<String> domains){
		this.domains = domains;
	}
	
	public void populateDomains(){
		try{
			BufferedReader br = new BufferedReader(new FileReader("data/domains.txt"));
			String line = "";
			while((line=br.readLine())!=null){
				String domain = line.split("/")[1].split("\\s+")[0];
				domains.add(domain.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static FreebaseWrapper getInstance() {
		if(instance == null) {
			instance = new FreebaseWrapper();
		}
		return instance;
	}
	
	public List<JSON> getTypes(String query, double relevance) {
		JSON json = this.fb.search(query);
		if(json == null || json.get("result").array().size()==0 || json.get("result").get(0)==null || (json.get("result").get(0)).get("type")==null)
			return null;
		//System.out.println(json);
		List<JSON> typeEntities = ((json.get("result").get(0)).get("type")).array();
		double rScore = (Double) json.get("result").get(0).get("relevance:score").value();
		if(rScore > relevance) {
			return typeEntities;
		}
		
		return null;
	}

	public static void main(String[] args) {
		FreebaseWrapper fb = FreebaseWrapper.getInstance();
		fb.populateDomains();
		List<JSON> types = fb.getTypes("bjp", 0);
		if(types != null) {
			for(JSON type : types) {
				System.out.println(type.get("id").string());
			}
		}
	}
	
}
