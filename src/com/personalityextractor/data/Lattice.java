/**
 * 
 */
package com.personalityextractor.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wikipedia.Wikiminer;

import com.personalityextractor.entity.WikipediaEntity;

/**
 * Lattice 
 * @author akishore
 *
 */
public class Lattice {

	HashMap<Long, LatticeNode> nodes;
	HashMap<String, LatticeEdge> edges;
	
	public Lattice() {
		nodes = new HashMap<Long, LatticeNode>();
		edges = new HashMap<String, LatticeEdge>();
	}
	
	/**
	 * method to build a lattice from all entity senses
	 * @param sensesList list of list of entity senses 
	 */
	public void buildLattice(List<List<WikipediaEntity>> sensesList) {
		LatticeNode startNode = new LatticeNode(-1, "start");
		nodes.put(startNode.getId(), startNode);
		
		ArrayList<Long> prevColumn = new ArrayList<Long>();
		prevColumn.add(startNode.getId());
		
		for(List<WikipediaEntity> senseList : sensesList) {
			ArrayList<Long> currColumn = new ArrayList<Long>();
			for(WikipediaEntity e : senseList) {
				long id = Long.parseLong(e.getWikiminerID());
				LatticeNode node = new LatticeNode(id, e.getText());
				nodes.put(id, node);
				currColumn.add(id);
				
				for(Long pId : prevColumn) {
					addEdge(pId, id);
				}
			}
			
			prevColumn.addAll(currColumn);
		}
		
		// connect with end node
		LatticeNode endNode = new LatticeNode(-2, "end");
		nodes.put(endNode.getId(), endNode);
		for(Long pId : prevColumn) {
			addEdge(pId, endNode.getId());
		}
	}
	
	private void addEdge(long id1, long id2) {
		LatticeNode node1 = nodes.get(id1);
		LatticeNode node2 = nodes.get(id2);
		
		LatticeEdge edge = null;
		// if start node
		if(node1.getId() == -1) {
			edge = new LatticeEdge(node1, node2);
		} else {
			double weight = Wikiminer.compareArticlesWithJaccard(String.valueOf(node1.getId()), String.valueOf(node2.getId()));
			edge = new LatticeEdge(node1, node2, weight);
		}
		
		edges.put(edge.getId(), edge);
	}
}
