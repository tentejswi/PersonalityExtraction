/**
 * 
 */
package com.personalityextractor.data;

/**
 * @author akishore
 *
 */
public class LatticeNode {

	private String text;
	private long id;
	
	public LatticeNode(long id, String text) {
		this.id = id;
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public long getId() {
		return id;
	}
	
}
