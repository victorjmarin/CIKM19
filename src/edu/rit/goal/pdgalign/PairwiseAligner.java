package edu.rit.goal.pdgalign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.rit.goal.coreminer.evaluation.Pair;
import edu.rit.goal.coreminer.evaluation.Triple;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;


public class PairwiseAligner {


	public static DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> getADirected(Collection<PDG> pdgs,
			int graphletSize, boolean useIncomingOnly, Aligner aligner) {

		// Build cost graphs.
		Set<DirectedCostGraph> costGraphs = pdgs.parallelStream()
				.map(g -> new DirectedCostGraph(g, graphletSize, useIncomingOnly)).collect(Collectors.toSet());

		DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> result = new DefaultUndirectedWeightedGraph<>(
				DefaultWeightedEdge.class);

		List<Pair<DirectedCostGraph, DirectedCostGraph>> combinations = chooseTwo(costGraphs);

		// Pairwise alignment.
		List<Set<Triple<Vertex, Vertex, Double>>> partialA = combinations.parallelStream().map(p -> {
			Set<Triple<Vertex, Vertex, Double>> alignment = aligner.align(p.a, p.b);
			return alignment;
		}).collect(Collectors.toList());

		for (Set<Triple<Vertex, Vertex, Double>> s : partialA) {
			for (Triple<Vertex, Vertex, Double> t : s) {
				DefaultWeightedEdge e = new DefaultWeightedEdge();
				result.addVertex(t.a);
				result.addVertex(t.b);
				result.setEdgeWeight(e, t.c);
				result.addEdge(t.a, t.b, e);
			}
		}
		
		return result;
	}

	public static <T> List<Pair<T, T>> chooseTwo(Collection<T> s) {
		List<Pair<T, T>> result = new ArrayList<>();
		List<T> l = new ArrayList<>(s);
		for (int i = 0; i < l.size(); i++)
			for (int j = i + 1; j < l.size(); j++)
				result.add(new Pair<>(l.get(i), l.get(j)));
		return result;
	}

}
