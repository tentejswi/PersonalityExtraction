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
import com.personalityextractor.entity.graph.ranking.IRanker;
import com.personalityextractor.entity.graph.ranking.WeightGraphRanker;
import com.personalityextractor.store.LuceneStore;
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
		this.root = new Node(new WikipediaEntity("__ROOT__", "-1", -1));
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

		int iter = 0;
		do {
			currCategories = new HashSet<String>();

			
			for (String cid : prevCategories) {
				Node cNode = nodes.get(cid);
				List<WikipediaEntity> categories = null;
				if(iter == 0) {
					categories = Wikiminer.getCategories(cid);
				} else {
					categories = Wikiminer.getParentCategories(cid);
				}
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

					formEdge(cNode, n2, cNode.getWeight());
				}
			}
			iter++;

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
		for(String id : nodes.keySet()) {
			Node n = nodes.get(id);
			if(n.getEntity().getType() == 1) {
				System.out.println(n.getId() + "\t" + n.getEntity().getText() + "\t"
						+ n.getEntity().getType() + "\t" + n.getWeight());
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
		ArrayList<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		entities.add(new WikipediaEntity("Rajiv Gandhi", "26129", 1));
		WikipediaEntity e = new WikipediaEntity("Sonia Gandhi", "169798", 1);
		e.incrCount();
		e.incrCount();e.incrCount();
		entities.add(e);
		Graph g = new Graph(entities);
		g.build(3);
		g.printWeights();
		IRanker ranker = new WeightGraphRanker(g);
		List<Node> topNodes = ranker.getTopRankedNodes(100);
		System.out.println();
	}
	
}
