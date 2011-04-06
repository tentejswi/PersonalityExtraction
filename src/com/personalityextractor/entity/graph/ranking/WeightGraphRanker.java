/**
 * 
 */
package com.personalityextractor.entity.graph.ranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.personalityextractor.entity.graph.Graph;
import com.personalityextractor.entity.graph.Node;

/**
 * @author semanticvoid
 *
 */
public class WeightGraphRanker extends AbstractRanker {

	public WeightGraphRanker(Graph g) {
		super(g);
	}

	/* (non-Javadoc)
	 * @see com.personalityextractor.entity.graph.ranking.IRanker#rank(com.personalityextractor.entity.graph.Graph)
	 */
	@Override
	public void rank() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see com.personalityextractor.entity.graph.ranking.IRanker#getTopRankedNodes(int)
	 */
	@Override
	public List<Node> getTopRankedNodes(int n) {
		List<Node> nodes = new ArrayList<Node>();
		nodes.addAll(graph.getNodes());
		Collections.sort(nodes, new Comparator<Node>() {
			public int compare(Node n1, Node n2) {
				if(n1.getWeight() > n2.getWeight()) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		
		List<Node> topNodes = new ArrayList<Node>();
		Iterator<Node> itr = nodes.iterator();
		int i = 0;
		while(itr.hasNext() && i<n) {
			topNodes.add(itr.next());
			i++;
		}
		
		return topNodes;
	}

}
