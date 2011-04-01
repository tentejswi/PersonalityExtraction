/**
 * 
 */
package com.personalityextractor.entity.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.personalityextractor.data.source.Wikiminer;
import com.personalityextractor.entity.WikipediaEntity;

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
	}
}
