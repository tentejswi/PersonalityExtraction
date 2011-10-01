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

import net.sf.json.JSONObject;

import com.personalityextractor.Runner;
import com.personalityextractor.commons.data.Tweet;
import com.personalityextractor.data.source.Wikiminer;
import com.personalityextractor.entity.WikipediaEntity;
import com.personalityextractor.entity.extractor.frequencybased.TopNNPHashTagsExtractor;
import com.personalityextractor.entity.graph.ranking.IRanker;
import com.personalityextractor.entity.graph.ranking.WeightGraphRanker;

import cs224n.util.Counter;

/**
 * @author semanticvoid
 * 
 */
public class Graph {

	private static double DECAY = 0.5;
	
	HashMap<String, Node> nodes;
	Node root = null;
	int edgeCount = 0;

	public Graph(List<WikipediaEntity> leafEntities) {
		this.root = new Node(new WikipediaEntity("__ROOT__", "-1", -1));
		this.nodes = new HashMap<String, Node>();
		for (WikipediaEntity we : leafEntities) {
			Node n = new Node(we);
			n.setLeaf(true);
			n.setWeight(we.count);
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
		if(depth > 0) {
			do {
				currCategories = new HashSet<String>();
	
				
				for (String cid : prevCategories) {
					Node cNode = nodes.get(cid);
					List<WikipediaEntity> categories = null;
	//				if(iter == 0) {
						categories = Wikiminer.getCategories(cid);
	//				} else {
	//					categories = Wikiminer.getParentCategories(cid);
	//				}
					
					List<WikipediaEntity> tmpCategories = new ArrayList<WikipediaEntity>();
					for (WikipediaEntity c : categories) {
						String txt = c.getText();
						WikipediaEntity e = Wikiminer.getHighestSenseEntity(txt);
						if(e != null) {
							tmpCategories.add(e);
						}
					}
					categories = tmpCategories;
					
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
		}

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
		// add decay
//		n2.addWeight(weight*1.0*DECAY);
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}
	
	public JSONObject toJSON(String handle, List<Node> nodes) {
		JSONObject json = new JSONObject();
		
		Set<String> seen = new HashSet<String>();
		Node root = new Node(new WikipediaEntity(handle, -1));
		for(Node n : nodes) {
//			JSONObject j = generateJSON(n, null, seen);
//			System.out.println(j.toString());
//			json.put(n.getEntity().getText(), j);
			formEdge(n, root, 1);
		}
		
		json =  generateJSON(root, null, new HashSet<String>());
		JSONObject jroot = new JSONObject();
		jroot.put(handle, json);
		return jroot;
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
		if(root == null) {
			return null;
		} else {
			seen.add(root.getId());
		}
		
		JSONObject json = new JSONObject();
		List<Edge> edges = root.getEdges();
		
//		if(root != null && root.getId() != null && root.getId().equals("38809")) {
//			System.out.print("");
//		}

		if (edges.size() > 0) {
			for (Edge e : edges) {
				if(e.getNode2() != null) {
						JSONObject cJson = null;
						if(!seen.contains(e.getNode2()) && !seen.contains(e.getNode1())) {
							seen.add(e.getNode2());
							seen.add(e.getNode1());
							cJson = generateJSON(nodes.get(e.getNode2()),
								root, seen);
						}
						if (cJson != null) {
							json.put(nodes.get(e.getNode2()).getEntity().getText(), cJson);
						} else {
							if(nodes.get(e.getNode2()) != null && nodes.get(e.getNode2()) != null && !seen.contains(e.getNode2())) {
								double w = nodes.get(e.getNode2()).getWeight();
								if(nodes.get(e.getNode2()).getWeight() < 1) {
									w = 1;
								}
								json.put(nodes.get(e.getNode2()).getEntity().getText(), w);
							}
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
//		entities.add(new WikipediaEntity("Rajiv Gandhi", "26129", 1));
//		WikipediaEntity e = new WikipediaEntity("Sonia Gandhi", "169798", 1);
//		e.incrCount();
//		e.incrCount();
//		e.incrCount();
//		entities.add(e);
		
		List<String> tweets = new ArrayList<String>();
		tweets.add("Sonia Gandhi is a person.");
		tweets.add("Rajiv Gandhi is a person.");
		TopNNPHashTagsExtractor tne = new TopNNPHashTagsExtractor();
		Counter<String> extracted_entities = tne.extract(tweets);
		HashMap<String, WikipediaEntity> allEntities = tne.resolve(extracted_entities);
		entities = new ArrayList<WikipediaEntity>();
		entities.addAll(allEntities.values());
		
		Graph g = new Graph(entities);
		g.build(0);
		g.printWeights();
		IRanker ranker = new WeightGraphRanker(g);
		List<Node> topNodes = ranker.getTopRankedNodes(100);
		System.out.println();
		System.out.println(Runner.nodesToJson("testuser", g, topNodes));
	}
	
}
