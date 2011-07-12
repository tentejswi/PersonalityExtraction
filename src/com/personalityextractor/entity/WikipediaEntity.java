package com.personalityextractor.entity;

import java.util.ArrayList;

public class WikipediaEntity extends Entity implements
		Comparable<WikipediaEntity> {

	String wikiminer_id;
	String commonnness;
	ArrayList<String> categories;
	int type;

	private void debug() {
		// System.out.println("init:\t" + type + "\t" + wikiminer_id + "\t" +
		// text);
	}

	public WikipediaEntity(String text, int type) {
		super(text);
		wikiminer_id = null;
		categories = new ArrayList<String>();
		this.type = type;
		debug();
	}

	public WikipediaEntity(String text, String wikiminer_id, int type) {
		super(text);
		this.wikiminer_id = wikiminer_id;
		categories = new ArrayList<String>();
		this.type = type;
		debug();
	}

	public WikipediaEntity(String text, String wikiminer_id, int type,
			String commonness) {
		super(text);
		this.wikiminer_id = wikiminer_id;
		this.commonnness = commonness;
		categories = new ArrayList<String>();
		this.type = type;
		debug();
	}

	public void addCategory(String text) {
		categories.add(text);
	}

	public String getWikiminerID() {
		return this.wikiminer_id;
	}

	public String getCommonness() {
		return String.valueOf(Math.log(Double.parseDouble(this.commonnness)));
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void print() {
		System.out.println("Entity: " + this.text);
		System.out.println("wikiminer_id: " + this.wikiminer_id);
		System.out.println("Categories: " + this.categories);
	}

	public boolean equals(WikipediaEntity e) {
		return this.wikiminer_id.equals(e.wikiminer_id);
	}

	@Override
	public int compareTo(WikipediaEntity o) {
		if(Double.parseDouble(this.commonnness) > Double.parseDouble(o.commonnness)) {
			return -1;
		} else {
			return 1;
		}
	}
}