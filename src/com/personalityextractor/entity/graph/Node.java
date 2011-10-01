/**
 * 
 */
package com.personalityextractor.entity.graph;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.personalityextractor.entity.Entity;
import com.personalityextractor.entity.WikipediaEntity;

/**
 * @author semanticvoid
 *
 */
public class Node {

	private String id;
	private List<Edge> edges;
	private WikipediaEntity entity;
	private double weight = 0.0;
	private boolean isLeaf = false;
	
	public Node(WikipediaEntity entity) {
		this.id = entity.getWikiminerID();
		this.entity = entity;
		this.edges = new ArrayList<Edge>();
		this.weight = entity.getCount();
	}
	
	public void addEdge(Edge e) {
		this.edges.add(e);
	}
	
	public String getId() {
		return id;
	}
	
	public void addWeight(double w) {
		this.weight += w;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double w) {
		weight = w;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public WikipediaEntity getEntity() {
		return entity;
	}
	
	public String toJSONString() {
		return toJSONObject().toString();
	}
	
	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("text", entity.getText());
		json.put("weight", weight);
		return json;
	}

	/**
	 * @return the isLeaf
	 */
	public boolean isLeaf() {
		return isLeaf;
	}

	/**
	 * @param isLeaf the isLeaf to set
	 */
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
}
