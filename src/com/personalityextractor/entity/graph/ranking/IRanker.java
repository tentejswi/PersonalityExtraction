package com.personalityextractor.entity.graph.ranking;

import java.util.List;

import com.personalityextractor.entity.graph.Graph;
import com.personalityextractor.entity.graph.Node;

/**
 * Interface for all graph ranking algorithms
 * @author semanticvoid
 *
 */
public interface IRanker {
	
	public void rank();
	
	public List<Node> getTopRankedNodes(int n);
}
