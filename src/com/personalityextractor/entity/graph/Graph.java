/**
 * 
 */
package com.personalityextractor.entity.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.personalityextractor.data.source.Wikiminer;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.graph.ranking.WeightGraphRanker;

/**
 * @author semanticvoid
 * 
 */
public class Graph {

	HashMap<String, Node> nodes;
	int edgeCount = 0;

	public Graph(List<WikipediaEntity> leafEntities) {
		this.nodes = new HashMap<String, Node>();
		for (WikipediaEntity we : leafEntities) {
			Node n = new Node(we);
			nodes.put(n.getId(), n);
		}
	}

	public void build() {
		Set<String> prevCategories = new HashSet<String>();
		
		// add leaf node categories
		prevCategories.addAll(nodes.keySet());
		
		Set<String> currCategories;
		
		do {
			currCategories = new HashSet<String>();
			
			for(String cid : prevCategories) {
				Node cNode = nodes.get(cid);
				
				List<String[]> categories = Wikiminer.getCategories(cid);
				
				for(String[] tuple : categories) {
					Node n2 = null;
					
					if(cid.equalsIgnoreCase(tuple[0])) {
						continue;
					}
					
					if(!prevCategories.contains(tuple[0]) && !currCategories.contains(tuple[0])) {
						currCategories.add(tuple[0]);
						n2 = new Node(new WikipediaEntity(tuple[1], tuple[0]));
						nodes.put(n2.getId(), n2);
					} else if(prevCategories.contains(tuple[0]) || currCategories.contains(tuple[0])) {
						n2 = nodes.get(tuple[0]);
					}
					
					formEdge(cNode, n2);
				}
			}
			
			prevCategories = currCategories;
		} while(prevCategories.size() > 0);
		
	}

	private void formEdge(Node n1, Node n2) {
		Edge e = new Edge(edgeCount++, n1.getId(), n2.getId());
		n1.addEdge(e);
		n2.addEdge(e);
		n2.addWeight(n1.getWeight());
	}
	
	public Collection<Node> getNodes() {
		return nodes.values();
	}
	
	public static void main(String[] args) {
		List<WikipediaEntity> nodes = new ArrayList<WikipediaEntity>();
		WikipediaEntity e1 = new WikipediaEntity("mouse", "18845");
		e1.incrCount();
		nodes.add(e1);
				
		WikipediaEntity e2 = new WikipediaEntity("monitor", "2351503");
		e2.incrCount();
		nodes.add(e2);
		
		Graph g = new Graph(nodes);
		g.build();
		for(Node n : g.getNodes()) {
			System.out.println("Node: " + n.getId());
			System.out.println("Entity: " + n.getEntity().getText());
			System.out.println("Weight: " + n.getWeight());
			for(Edge e : n.getEdges()) {
				System.out.println("edge: " + e.toString());
			}
			System.out.println("\n\n");
		}
		
		
		WeightGraphRanker ranker = new WeightGraphRanker(g);
		List<Node> topNodes = ranker.getTopRankedNodes(1);
		System.out.println("---------- TOP NODES -------------\n");
		for(Node n : topNodes) {
			System.out.println("Node: " + n.getId());
			System.out.println("Entity: " + n.getEntity().getText());
			System.out.println("Weight: " + n.getWeight());
			for(Edge e : n.getEdges()) {
				System.out.println("edge: " + e.toString());
			}
			System.out.println("\n\n");
		}
		System.out.println("");
	}
}
