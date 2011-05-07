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
import com.personalityextractor.store.WikiminerDB;

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

	public void build(int depth) {
		int currentDepth = 0;
		Set<String> prevCategories = new HashSet<String>();
		
		// add leaf node categories
		prevCategories.addAll(nodes.keySet());
		
		Set<String> currCategories;
		
		do {
			currCategories = new HashSet<String>();
			
			for(String cid : prevCategories) {
				Node cNode = nodes.get(cid);
				
				List<WikipediaEntity> categories = WikiminerDB.getInstance().getCategories(cid);
				
				for(WikipediaEntity category : categories) {
					Node n2 = null;
					
					if(cid.equalsIgnoreCase(category.getWikiminerID())) {
						continue;
					}
					
					if(!prevCategories.contains(category.getWikiminerID()) && !currCategories.contains(category.getWikiminerID())) {
						currCategories.add(category.getWikiminerID());
						n2 = new Node(category);
						nodes.put(n2.getId(), n2);
					} else if(prevCategories.contains(category.getWikiminerID()) || currCategories.contains(category.getWikiminerID())) {
						n2 = nodes.get(category.getWikiminerID());
					}
					
					formEdge(cNode, n2);
				}
			}
			
			prevCategories = currCategories;
			currentDepth++;
		} while(prevCategories.size() > 0 && currentDepth < depth);
		
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
		g.build(1);
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
