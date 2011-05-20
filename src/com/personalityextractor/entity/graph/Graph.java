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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
	Node root = null;
	int edgeCount = 0;

	public Graph(List<WikipediaEntity> leafEntities) {
		this.root = new Node(new WikipediaEntity("__ROOT__", "-1"));
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

			for (String cid : prevCategories) {
				Node cNode = nodes.get(cid);

				List<WikipediaEntity> categories = WikiminerDB.getInstance()
						.getCategories(cid);

				for (WikipediaEntity category : categories) {
					Node n2 = null;

					if (cid.equalsIgnoreCase(category.getWikiminerID())) {
						continue;
					}

					if (!nodes.containsKey(category.getWikiminerID())) {
						currCategories.add(category.getWikiminerID());
						n2 = new Node(category);
						nodes.put(n2.getId(), n2);
					} else {
						n2 = nodes.get(category.getWikiminerID());
					}

					formEdge(cNode, n2, cNode.getWeight()/categories.size());
				}
			}

			prevCategories = currCategories;
			currentDepth++;
		} while (prevCategories.size() > 0 && currentDepth < depth);

		// link to roo
		for (String cid : prevCategories) {
			Node cNode = nodes.get(cid);
			formEdge(cNode, root, 0);
		}
	}

	private void formEdge(Node n1, Node n2, double weight) {
		Edge e1 = new Edge(edgeCount++, n1.getId(), n2.getId());
		Edge e2 = new Edge(edgeCount++, n2.getId(), n1.getId());
		n1.addEdge(e1);
		n2.addEdge(e2);
		n2.addWeight(weight);
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	public String toJSON() {
		return generateJSON(root, null, new HashSet<String>()).toString();
	}
	
	public void printWeights() {
		List<Node> queue = new ArrayList<Node>();
		Set<String> seen = new HashSet<String>();
		
		queue.add(root);
		seen.add(root.getId());
		
		while(queue.size() > 0) {
			Node node = queue.remove(0);
			if(node != null) {
				System.out.println(node.getId() + "\t" + node.getEntity().getText() + "\t" + node.getWeight());
			} else {
				continue;
			}
			
			if(node.getId().equals("776875")) {
				System.out.print("");
			}
			
			for(Edge e: node.getEdges()) {
				Node node1 = nodes.get(e.getNode2());
				if(node1 != null && !seen.contains(node1.getId())) {
					queue.add(node1);
					seen.add(node1.getId());
				}
			}
		}
	}

	private JSONObject generateJSON(Node root, Node parent, Set<String> seen) {
		JSONObject json = new JSONObject();
		List<Edge> edges = root.getEdges();
		
//		if(root != null && root.getId() != null && root.getId().equals("38809")) {
//			System.out.print("");
//		}

		if (edges.size() > 0) {
			for (Edge e : edges) {
				if(e.getNode2() != null) {
						JSONObject cJson = null;
						if(!seen.contains(e.getNode2())) {
							seen.add(e.getNode2());
							cJson = generateJSON(nodes.get(e.getNode2()),
								root, seen);
						}
						if (cJson != null) {
							json.put(nodes.get(e.getNode2()).getEntity().getText(), cJson);
						} else {
							json.put(nodes.get(e.getNode2()).getEntity().getText(), nodes.get(e.getNode2())
									.getWeight());
						}
					}
				}
		} else {
			return null;
		}

		return json;
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
		for (Node n : g.getNodes()) {
			System.out.println("Node: " + n.getId());
			System.out.println("Entity: " + n.getEntity().getText());
			System.out.println("Weight: " + n.getWeight());
			for (Edge e : n.getEdges()) {
				System.out.println("edge: " + e.toString());
			}
			System.out.println("\n\n");
		}

		WeightGraphRanker ranker = new WeightGraphRanker(g);
		List<Node> topNodes = ranker.getTopRankedNodes(1);
		System.out.println("---------- TOP NODES -------------\n");
		for (Node n : topNodes) {
			System.out.println("Node: " + n.getId());
			System.out.println("Entity: " + n.getEntity().getText());
			System.out.println("Weight: " + n.getWeight());
			for (Edge e : n.getEdges()) {
				System.out.println("edge: " + e.toString());
			}
			System.out.println("\n\n");
		}
		System.out.println("");
	}
}
