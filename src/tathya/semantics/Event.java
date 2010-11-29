package tathya.semantics;

import java.util.ArrayList;
import java.util.List;

public class Event {
	
	private List<String> words;
	private List<String> posTags;

	public Event(ArrayList<String> lines) {
		this.words = new ArrayList<String>();
		this.posTags = new ArrayList<String>();
		this.parseLines(lines);
	}
	
	public List<String> getWords() {
		return words;
	}

	public List<String> getPosTags() {
		return posTags;
	}
	
	public List<String> getEntities() {
		List<String> entities = new ArrayList<String>();
		
		int sindex = -1;
		int eindex = -1;
		boolean nnpFlag = false;
		
		for(int i=0; i<this.posTags.size(); i++) {
			String tag = this.posTags.get(i);
			
			if(tag.equals("NNP") || tag.equals("NN") || tag.equals("DT")) {
				if(tag.equals("DT") && !nnpFlag) {
					continue;
				}
				
				if(!nnpFlag) {
					sindex = i;
				}
				nnpFlag = true;
				continue;
			}
			
			if(nnpFlag) {
				eindex = i-1;
				entities.add(this.substring(sindex, eindex));
				sindex = -1;
				eindex = -1;
				nnpFlag = false;
			}
		}
		
		return entities;
	}
	
	private String substring(int sindex, int eindex) {
		StringBuffer buf = new StringBuffer();
		for(int i=sindex; i<=eindex; i++) {
			buf.append(this.words.get(i) + " ");
		}
		return buf.toString().trim();
	}
	
	private void parseLines(ArrayList<String> lines) {
		for(String line : lines) {
			String[] tokens = line.split("[ \t]+");
			if(tokens.length <2)
				continue;
			this.words.add(tokens[0]);
			this.posTags.add(tokens[1]);
		}
	}
	
}
