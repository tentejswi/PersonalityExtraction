package tathya.semantics;

import java.util.ArrayList;
import java.util.List;

public class Word {

	public enum Type { SUBJECT, OBJECT, PREDICATE, ATTRIBUTE }

	private String text;
	private Type type;
	private List<Word> attributes = null;
	
	public Word(String text, Type t) {
		this.text = text;
		this.type = t;
		
		if(this.type != Type.ATTRIBUTE) {
			attributes = new ArrayList<Word>();
		}
	}
	
	public String getText() {
		if(text != null) {
			return text;
		}
		
		return "";
	}
	
	public void setType(Type t) {
		this.type = t;
	}
	
	public void addAttribute(Word attr) {
		attributes.add(attr);
	}
	
	public void addAttributeAll(List<Word> attr) {
		for(Word w : attr) {
			attributes.add(w);
		}
	}
	
	public boolean hasAttributes() {
		if(attributes != null && attributes.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public List<Word> getAttributes() {
		return attributes;
	}
	
	public boolean isSubject() {
		if(this.type == Type.SUBJECT) {
			return true;
		}
		
		return false;
	}
	
	public boolean isObject() {
		if(this.type == Type.OBJECT) {
			return true;
		}
		
		return false;
	}
	
	public boolean isPredicate() {
		if(this.type == Type.PREDICATE) {
			return true;
		}
		
		return false;
	}
	
	public boolean isAttribute() {
		if(this.type == Type.ATTRIBUTE) {
			return true;
		}
		
		return false;
	}
}
