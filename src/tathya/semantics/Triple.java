package tathya.semantics;

public class Triple {

	private Word subject = null;
	private Word object = null;
	private Word predicate = null;
	
	public Triple() {
		
	}
	
	public Word getSubject() {
		return subject;
	}

	public void setSubject(Word subject) {
		this.subject = subject;
	}

	public Word getObject() {
		return object;
	}

	public void setObject(Word object) {
		this.object = object;
	}

	public Word getPredicate() {
		return predicate;
	}

	public void setPredicate(Word predicate) {
		this.predicate = predicate;
	}
	
	public boolean hasSubject() {
		if(subject != null) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasPredicate() {
		if(predicate != null) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasObject() {
		if(object != null) {
			return true;
		}
		
		return false;
	}
}
