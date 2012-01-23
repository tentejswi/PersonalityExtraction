/**
 * 
 */
package com.personalityextractor.entity.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import com.personalityextractor.Runner;
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

	private static Set<String> superCategories = new HashSet<String>();
	static {
		superCategories.add("Agriculture");
		superCategories.add("Arts");
		superCategories.add("Applied sciences");
		superCategories.add("Belief");
		superCategories.add("Business");
		superCategories.add("Computers");
		superCategories.add("Culture");
		superCategories.add("Education");
		superCategories.add("Environment");
		superCategories.add("Geography");
		superCategories.add("Health");
		superCategories.add("History");
		superCategories.add("Humanities");
		superCategories.add("Language");
		superCategories.add("Law");
		superCategories.add("Mathematics");
		superCategories.add("Nature");
		superCategories.add("People");
		superCategories.add("Politics");
		superCategories.add("Science");
//		superCategories.add("Society");
		superCategories.add("Technology");
		superCategories.add("Sports");
		superCategories.add("Travel");
	}

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

	private List<WikipediaEntity> getSuperCategory(String id, int depth) {
		WikipediaEntity entity = null;
		List<WikipediaEntity> supeCategories = new ArrayList<WikipediaEntity>();
		int shortestPath = Integer.MAX_VALUE;
		int currentDepth = 0;
		int maxCount = -1;
		int minCount = 999;
		HashMap<String, WikipediaEntity> entities = new HashMap<String, WikipediaEntity>();
		HashMap<String, Integer> entityCount = new HashMap<String, Integer>();
		Set<String> prevCategories = new HashSet<String>();
		Set<String> currCategories = null;
		prevCategories.add(id);

		int iter = 0;
		do {
			currCategories = new HashSet<String>();

			for (String cid : prevCategories) {
				Node cNode = nodes.get(cid);
				List<WikipediaEntity> categories = null;
				if (iter == 0) {
					categories = Wikiminer.getCategories(cid);
				} else {
					categories = Wikiminer.getParentCategories(cid);
				}

				for (WikipediaEntity category : categories) {
					Node n2 = null;

					if (cid.equalsIgnoreCase(category.getWikiminerID())) {
						continue;
					}

					// if (!nodes.containsKey(category.getWikiminerID())) {
					if (!superCategories.contains(category.getText())) {
						currCategories.add(category.getWikiminerID());
					} else {
						System.out.println("hit " + category.getText()
								+ " at depth " + currentDepth);
						// if (currentDepth < shortestPath) {
						// entity = category;
						// }

						if (!entities.containsKey(category.getText())) {
							entities.put(category.getText(), category);
						}

						if (!entityCount.containsKey(category.getText())) {
							entityCount.put(category.getText(), (depth-currentDepth));
						}
						else {
							entityCount.put(category.getText(),
									entityCount.get(category.getText()) + (depth-currentDepth));
						}
					}
					// n2 = new Node(category);
					// nodes.put(n2.getId(), n2);
					// } else {
					// // n2 = nodes.get(category.getWikiminerID());
					// }

					// formEdge(cNode, n2, cNode.getWeight());
				}
			}
			iter++;

			prevCategories = currCategories;
			currentDepth++;
//			System.out.println("depth:\t" + currentDepth);
		} while (prevCategories.size() > 0 && currentDepth < depth);

		for (String k : entityCount.keySet()) {
			int count = entityCount.get(k);
//			System.out.println(k + "\t" + count);
//			supeCategories.add(entities.get(k));
			if (count > maxCount) {
				maxCount = count;
//				entity = entities.get(k);
			}
			
			if (count < minCount) {
				minCount = count;
			}
		}
		
		System.out.println("maxcount: " + maxCount + "\tmincount: " + minCount);
		List<Integer> list = new ArrayList<Integer>();
		for (String k : entityCount.keySet()) {
			list.add(entityCount.get(k));
		}
		Collections.sort(list);
//		System.out.println("95 percentile: " + list.get((int) Math.floor(((list.size()-1)*0.95))));
		int index = (int) Math.floor(((list.size()-1)*0.95));
		if(index >= 0) {
			int midcount = list.get(index);
		
			for (String k : entityCount.keySet()) {
				int count = entityCount.get(k);
				System.out.println(k + "\t" + count);
	//			supeCategories.add(entities.get(k));
				if (count >= midcount) {
					supeCategories.add(entities.get(k));
				}
			}
		}
		

		return supeCategories;
	}

	public void build(int depth) {
		Set<Node> cNodes = new HashSet<Node>();
		Object[] ids = nodes.keySet().toArray();
		for (Object id : ids) {
			String cid = (String) id;
			List<WikipediaEntity> e = getSuperCategory(cid, depth);
			if (e != null) {
				for(WikipediaEntity e1 : e) {
					System.out.println(nodes.get(id).getEntity().getText()
							+ " has super category " + e1.getText());
					Node n2 = null;
					if(nodes.containsKey(e1.getWikiminerID())) {
						n2 = nodes.get(e1.getWikiminerID());
					} else {
						n2 = new Node(e1);
					}
					if (!nodes.containsKey(n2.getId())) {
						cNodes.add(n2);
						nodes.put(n2.getId(), n2);
						formEdge(n2, root, 1);
					}
					Node n1 = nodes.get(cid);
					formEdge(n1, n2, 1); // fixed weight for now
				}
			}
		}

		// link to root
		// for (Node n : cNodes) {
		// nodes.put(n.getId(), n);
		// formEdge(n, root, 1);
		// }

		System.out.println("done");
	}

	// public void build(int depth) {
	// int currentDepth = 0;
	// Set<String> prevCategories = new HashSet<String>();
	//
	// // add leaf node categories
	// prevCategories.addAll(nodes.keySet());
	//
	// Set<String> currCategories;
	//
	// int iter = 0;
	// if(depth > 0) {
	// do {
	// currCategories = new HashSet<String>();
	//
	//
	// for (String cid : prevCategories) {
	// Node cNode = nodes.get(cid);
	// List<WikipediaEntity> categories = null;
	// if(iter == 0) {
	// categories = Wikiminer.getCategories(cid);
	// } else {
	// categories = Wikiminer.getParentCategories(cid);
	// }
	//
	// // List<WikipediaEntity> tmpCategories = new
	// ArrayList<WikipediaEntity>();
	// // for (WikipediaEntity c : categories) {
	// // String txt = c.getText();
	// // WikipediaEntity e = Wikiminer.getHighestSenseEntity(txt);
	// // if(e != null) {
	// // tmpCategories.add(e);
	// // }
	// // }
	// // categories = tmpCategories;
	//
	// for (WikipediaEntity category : categories) {
	// Node n2 = null;
	//
	// if (cid.equalsIgnoreCase(category.getWikiminerID())) {
	// continue;
	// }
	//
	// if (!nodes.containsKey(category.getWikiminerID())) {
	// if(!superCategories.contains(category.getText())) {
	// currCategories.add(category.getWikiminerID());
	// } else {
	// System.out.println("hit " + category.getText());
	// }
	// n2 = new Node(category);
	// nodes.put(n2.getId(), n2);
	// } else {
	// n2 = nodes.get(category.getWikiminerID());
	// }
	//
	// formEdge(cNode, n2, cNode.getWeight());
	// }
	// }
	// iter++;
	//
	// prevCategories = currCategories;
	// currentDepth++;
	// System.out.println("depth:\t" + currentDepth);
	// } while (prevCategories.size() > 0 && currentDepth < depth);
	// }
	//
	// // link to roo
	// for (String cid : prevCategories) {
	// Node cNode = nodes.get(cid);
	// formEdge(cNode, root, 0);
	// }
	// }

	private void formEdge(Node n1, Node n2, double weight) {
		Edge e1 = new Edge(edgeCount++, n1.getId(), n2.getId());
		Edge e2 = new Edge(edgeCount++, n2.getId(), n1.getId());
		n1.addEdge(e1);
		n2.addEdge(e2);
		n2.addWeight(weight);
		// add decay
		// n2.addWeight(weight*1.0*DECAY);
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	public JSONObject toJSON(String handle, List<Node> nodes) {
		JSONObject json = new JSONObject();

		Set<String> seen = new HashSet<String>();
		// Node root = new Node(new WikipediaEntity(handle, -1));
		if (nodes != null) {
			for (Node n : nodes) {
				// JSONObject j = generateJSON(n, null, seen);
				// System.out.println(j.toString());
				// json.put(n.getEntity().getText(), j);
				formEdge(n, root, 1);
			}
		}

		json = generateJSON(root, null, new HashSet<String>());
		JSONObject jroot = new JSONObject();
		jroot.put(handle, json);
		return jroot;
	}

	public void printWeights() {
		for (String id : nodes.keySet()) {
			Node n = nodes.get(id);
			// if(n.getEntity().getType() == 1) {
			System.out.println(n.getId() + "\t" + n.getEntity().getText()
					+ "\t" + n.getEntity().getType() + "\t" + n.getWeight());
			// }
		}
	}

	private JSONObject generateJSON(Node root, Node parent, Set<String> seen) {
		if (root == null) {
			return null;
		} else {
			seen.add(root.getId());
		}

		JSONObject json = new JSONObject();
		List<Edge> edges = root.getEdges();

		// if(root != null && root.getId() != null &&
		// root.getId().equals("38809")) {
		// System.out.print("");
		// }

		if (edges.size() > 0) {
			for (Edge e : edges) {
				if (e.getNode2() != null) {
					JSONObject cJson = null;
					if (!seen.contains(e.getNode2())) { // &&
														// !seen.contains(e.getNode1()))
														// {
						// seen.add(e.getNode2());
						seen.add(e.getNode1());
						if (!nodes.get(e.getNode2()).isLeaf()) {
							cJson = generateJSON(nodes.get(e.getNode2()), root,
									seen);
						} else {
							json.put(nodes.get(e.getNode2()).getEntity()
									.getText(), nodes.get(e.getNode2())
									.getWeight());
						}
					}
					if (cJson != null) {
						json.put(nodes.get(e.getNode2()).getEntity().getText(),
								cJson);
					} else {
						if (nodes.get(e.getNode2()) != null
								&& nodes.get(e.getNode2()) != null
								&& !seen.contains(e.getNode2())) {
							double w = nodes.get(e.getNode2()).getWeight();
							if (nodes.get(e.getNode2()).getWeight() < 1) {
								w = 1;
							}
							json.put(nodes.get(e.getNode2()).getEntity()
									.getText(), w);
						}
					}
				}
			}

			// if (json.size() == 0) {
			// json.put(root.getEntity().getText(), root.getWeight());
			// }
		} else {
			return null;
		}

		return json;
	}

	public static void main(String[] args) {
		ArrayList<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		 entities.add(new WikipediaEntity("Rajiv Gandhi", "26129", 1));
		 entities.add(new WikipediaEntity("Apple", "856", 1));
		 entities.add(new WikipediaEntity("Sonia Gandhi", "169798", 1));
		 entities.add(new WikipediaEntity("Bill Gates", "3747", 1));
//		 WikipediaEntity e = new WikipediaEntity("Sonia Gandhi", "169798", 1);
//		 e.incrCount();
//		 e.incrCount();
//		 e.incrCount();
//		 entities.add(e);

		List<String> tweets = new ArrayList<String>();
//		tweets.add("Sonia Gandhi is a person.");
		tweets.add("Rajiv Gandhi is a Congress leader.");
		tweets.add("Sachin Tendulkar is awesome.");
		tweets.add("Amazon is an awesome company.");
		TopNNPHashTagsExtractor tne = new TopNNPHashTagsExtractor();
		Counter<String> extracted_entities = tne.extract(tweets);
		Counter<String> finalEntityCounter = new Counter<String>();
		finalEntityCounter.setCount("Sonia Gandhi" , 10);
//		HashMap<String, WikipediaEntity> allEntities = tne
//				.resolve(finalEntityCounter);
//		entities = new ArrayList<WikipediaEntity>();
//		entities.addAll(allEntities.values());

		Graph g = new Graph(entities);
		g.build(7);
		g.printWeights();
		IRanker ranker = new WeightGraphRanker(g);
		// List<Node> topNodes = ranker.getTopRankedNodes(100);
		System.out.println(g.toJSON("testuser", null));
		// System.out.println(Runner.nodesToJson("testuser", g, topNodes));
	}

}
