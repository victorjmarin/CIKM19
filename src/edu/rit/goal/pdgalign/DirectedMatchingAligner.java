package edu.rit.goal.pdgalign;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.rit.goal.coreminer.evaluation.Triple;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;


public class DirectedMatchingAligner implements Aligner {

	@Override
	public Set<Triple<Vertex, Vertex, Double>> align(DirectedCostGraph g1, DirectedCostGraph g2) {
		DirectedOrbitsCost cost = new DirectedOrbitsCost(g1, g2);

		DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> K = completeBipartite(g1, g2, cost);

		MaximumWeightBipartiteMatching<Vertex, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(K,
				g1.nodesOfInterest(), g2.nodesOfInterest());

		Matching<Vertex, DefaultWeightedEdge> match = matching.getMatching();

		Set<Triple<Vertex, Vertex, Double>> result = new HashSet<>();

		for (DefaultWeightedEdge e : match.getEdges()) {
			double w = K.getEdgeWeight(e);
			result.add(new Triple<>(K.getEdgeSource(e), K.getEdgeTarget(e), w));
		}

		return result;
	}

	public Set<Triple<Vertex, Vertex, Double>> align(PDG g1, PDG g2) {
		return align(new DirectedCostGraph(g1, 4, false), new DirectedCostGraph(g2, 4, false));
	}

	public static DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> completeBipartite(DirectedCostGraph g1,
			DirectedCostGraph g2, DirectedOrbitsCost cost) {

		DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> result = new DefaultUndirectedWeightedGraph<>(
				DefaultWeightedEdge.class);

		for (Vertex v1 : g1.nodesOfInterest()) {
			for (Vertex v2 : g2.nodesOfInterest()) {
				double sim = 1.0 - cost.get(v1, v2);
				// Do not include edges with 0 similarity.
				// Commented because it was dramatically reducing coverable stmts.
				if (sim != 0d) {
					result.addVertex(v1);
					result.addVertex(v2);
					DefaultWeightedEdge e = new DefaultWeightedEdge();
					result.addEdge(v1, v2, e);
					result.setEdgeWeight(e, sim);
				}
			}
		}

		return result;
	}

}
