package com.personalityextractor.entity.graph;

public class Edge {
	
	private long id;
	private String label = "";
	private double weight = 1.0;
	private String nodeId1;
	private String nodeId2;
	
	public Edge(long id, String label, double weight, String nodeId1, String nodeId2) {
		this.id = id;
		this.label = label;
		this.weight = weight;
		this.nodeId1 = nodeId1;
		this.nodeId2 = nodeId2;
	}
	
	public Edge(long id, double weight, String nodeId1, String nodeId2) {
		this.id = id;
		this.weight = weight;
		this.nodeId1 = nodeId1;
		this.nodeId2 = nodeId2;
	}
	
	public Edge(long id, String nodeId1, String nodeId2) {
		this.id = id;
		this.nodeId1 = nodeId1;
		this.nodeId2 = nodeId2;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String toString() {
		return id + "\t" + weight + "\t" + nodeId1 + "\t" + nodeId2 + "\n";
	}
}
