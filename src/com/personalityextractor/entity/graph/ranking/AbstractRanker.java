/**
 * 
 */
package com.personalityextractor.entity.graph.ranking;

import java.util.List;

import com.personalityextractor.entity.graph.Graph;
import com.personalityextractor.entity.graph.Node;

/**
 * @author semanticvoid
 *
 */
public class AbstractRanker implements IRanker {
	
	protected Graph graph;
	
	public AbstractRanker(Graph g) {
		this.graph = g;
	}

	/* (non-Javadoc)
	 * @see com.personalityextractor.entity.graph.ranking.IRanker#rank(com.personalityextractor.entity.graph.Graph)
	 */
	@Override
	public void rank() {
	}

	/* (non-Javadoc)
	 * @see com.personalityextractor.entity.graph.ranking.IRanker#getTopRankedNodes(int)
	 */
	@Override
	public List<Node> getTopRankedNodes(int n) {
		return null;
	}

}
