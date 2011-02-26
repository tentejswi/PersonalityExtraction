/**
 * 
 */
package com.personalityextractor.data;

/**
 * @author akishore
 *
 */
public class LatticeEdge {

	private LatticeNode node1;
	private LatticeNode node2;
	private double weight;
	
	public LatticeEdge(LatticeNode node1, LatticeNode node2) {
		this.node1 = node1;
		this.node2 = node2;
		weight = 1.0;
	}
	
	public LatticeEdge(LatticeNode node1, LatticeNode node2, double weight) {
		this.node1 = node1;
		this.node2 = node2;
		this.weight = weight;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public LatticeNode getNode1() {
		return node1;
	}
	
	public LatticeNode getNode2() {
		return node2;
	}
	
	public String getId() {
		return this.node1.getId() + "-" + this.node2.getId();
	}
}
