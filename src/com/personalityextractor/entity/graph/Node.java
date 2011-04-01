/**
 * 
 */
package com.personalityextractor.entity.graph;

import java.util.ArrayList;
import java.util.List;

import com.personalityextractor.entity.WikipediaEntity;

/**
 * @author semanticvoid
 *
 */
public class Node {

	private String id;
	private List<Edge> edges;
	private WikipediaEntity entity;
	
	public Node(WikipediaEntity entity) {
		this.id = entity.getWikiminerID();
		this.entity = entity;
		this.edges = new ArrayList<Edge>();
	}
	
	public void addEdge(Edge e) {
		this.edges.add(e);
	}
	
	public String getId() {
		return id;
	}
}
