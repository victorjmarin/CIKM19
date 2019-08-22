package edu.rit.goal.coreminer.evaluation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jgrapht.Graphs;

import edu.rit.goal.pdgalign.Cost;
import edu.rit.goal.pdgalign.DirectedInducedSubgraph;
import edu.rit.goal.sourcedg.Edge;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public class NeighborhoodCost implements Cost {

	private PDG g1;
	private PDG g2;

	private static Comparator<Vertex> vertexComparator;
	private Comparator<Pair<Vertex, Vertex>> edgeComparator;

	private Map<Vertex, Set<String>> typesCache = new HashMap<>();
	private Map<Integer, Float> jaccardCache = new HashMap<>();

	private float[][] costMatrix;

	static {
		vertexComparator = lenientVertexComparator();
	}

	public NeighborhoodCost(PDG g1, PDG g2) {
		this.g1 = g1;
		this.g2 = g2;
		init();
	}

	private void init() {
		int d1 = g1.vertexSet().size();
		int d2 = g2.vertexSet().size();
		costMatrix = new float[d1][d2];
		edgeComparator = edgeComparator(g1, g2);

		// Load cost matrix.
		for (Vertex v1 : g1.nodesOfInterest()) {
			Map<Float, Set<Vertex>> cost2vtx = new HashMap<>();
			int idx1 = (int) v1.getId(), idx2;
			for (Vertex v2 : g2.nodesOfInterest()) {
				idx2 = (int) v2.getId();
				float cost = costMatrix[idx1][idx2] = computeCost1(v1, v2);
				Set<Vertex> V = cost2vtx.getOrDefault(cost, new HashSet<>());
				if (V.isEmpty())
					cost2vtx.put(cost, V);
				V.add(v2);
			}

			// Break ties using nesting levels.
			int v1NestingLevel = g1.nestingLevel(g1.getEntry(), v1, false);
			float eps = Float.MIN_VALUE;
			for (Set<Vertex> V : cost2vtx.values()) {
				if (V.size() == 1)
					continue;
				for (Vertex v : V) {
					idx2 = (int) v.getId();
					int vNestingLevel = g2.nestingLevel(g2.getEntry(), v, false);
					int diff = Math.abs(v1NestingLevel - vNestingLevel);
					costMatrix[idx1][idx2] += diff * eps;
				}
			}
		}
	}

	private float computeCost2(Vertex v1, Vertex v2) {
		float[] contextualCost1 = topologicalCost(v1, v2, 1);
		float[] contextualCost2 = topologicalCost(v1, v2, 2);
		float topological1 = contextualCost1[0];
		float topological2 = contextualCost2[0];
		float semantic1 = contextualCost1[1];
		float semantic2 = contextualCost2[1];
		float topological = (topological1 + topological2) / 2;
		float semantic = (semantic1 + semantic2 + semanticCost(v1, v2)) / 3;
		// float result = (topological + semantic) / 2;
		float result = (float) ((1 + semantic) * topological);
		// result = Math.min(result, 1);
		// float result = (topological1 + topological2 + semantic1 + semantic2 +
		// semanticCost(v1, v2)) / 5;

//		System.out.println(v1 + " -- " + v2);
//		System.out.println("t1=" + topological1 + ", t2=" + topological2 + ", s=" + semantic + ", total=" + result);
//		System.out.println();

		return result;
	}

	private float computeCost1(Vertex v1, Vertex v2) {
		float[] contextualCost = topologicalCost(v1, v2, 1);
		float topological = contextualCost[0];
		float semanticCtx = contextualCost[1];
		float semantic = (semanticCtx + semanticCost(v1, v2)) / 2;
		float result = (float) ((1 + semantic) * topological);
		// result = Math.min(result, 1);

//		System.out.println(v1 + " -- " + v2);
//		System.out.println("t=" + topological + ", s=" + semantic + ", total=" + result);
//		System.out.println();

		return result;
	}

	private float[] topologicalCost(Vertex v1, Vertex v2, int k) {
		Set<Vertex> N1 = kSphere(v1, g1, new HashSet<>(), k);
		Set<Vertex> N2 = kSphere(v2, g2, new HashSet<>(), k);
		N1.add(v1);
		N2.add(v2);

		DirectedInducedSubgraph<Vertex, Edge> I1 = new DirectedInducedSubgraph<>(g1.asDefaultDirectedGraph(), N1,
				Edge.class);
		DirectedInducedSubgraph<Vertex, Edge> I2 = new DirectedInducedSubgraph<>(g2.asDefaultDirectedGraph(), N2,
				Edge.class);

		MCIS<Vertex, Edge> mcis = new MCIS<>(I1, I2, Edge.class, vertexComparator, edgeComparator, 1);
		List<Set<Pair<Vertex, Vertex>>> cliques = mcis.compute(1);

		if (cliques.isEmpty())
			return new float[] { 1f, 1f };

		double size = cliques.get(0).size();
		double normalizer = 1.0 * Math.max(N1.size(), N2.size());
		float topological = (float) (1 - (size / normalizer));

		// Impute neighbors' labels.
		SummaryStatistics ss = new SummaryStatistics();
		for (Pair<Vertex, Vertex> p : cliques.get(0)) {
			ss.addValue(semanticCost(p.a, p.b));
		}

		float sphereSemantics = (float) ss.getMean();
//		float result = (float) ((1 + sphereSemantics) * topological);
//		result = Math.min(result, 1);

		return new float[] { topological, sphereSemantics };
	}

	private float semanticCost(Vertex v1, Vertex v2) {
		Set<String> v1Types = typesCache.get(v1);
		Set<String> v2Types = typesCache.get(v2);

		if (v1Types == null) {
			v1Types = new HashSet<>();
			v1Types.add(v1.getType().toString());
			v1Types.addAll(v1.getSubtypes());
			typesCache.put(v1, v1Types);
		}

		if (v2Types == null) {
			v2Types = new HashSet<>();
			v2Types.add(v2.getType().toString());
			v2Types.addAll(v2.getSubtypes());
			typesCache.put(v2, v2Types);
		}

		return jaccardDistance(v1Types, v2Types);
	}

	public float jaccardIndex(Set<String> s1, Set<String> s2) {

		if (s1.isEmpty() && s2.isEmpty())
			return 1;

		Integer hash = s1.hashCode() + s2.hashCode();
		Float result = jaccardCache.get(hash);

		if (result == null) {
			Set<String> intersection = new HashSet<>(s1);
			intersection.retainAll(s2);

			Set<String> union = new HashSet<>(s1);
			union.addAll(s2);
			result = (float) intersection.size() / union.size();
			jaccardCache.put(hash, result);
		}

		return result;
	}

	public float jaccardDistance(Set<String> s1, Set<String> s2) {
		return 1 - jaccardIndex(s1, s2);
	}

	public static Comparator<Vertex> strictVertexComparator() {
		return new Comparator<Vertex>() {
			@Override
			public int compare(Vertex o1, Vertex o2) {
				Set<String> s1 = new HashSet<>(o1.getSubtypes());
				s1.add(o1.getType().name());
				Set<String> s2 = new HashSet<>(o2.getSubtypes());
				s2.add(o2.getType().name());
				if (s1.equals(s2))
					return 0;
				if (s1.size() < s2.size())
					return -1;
				return 1;
			}
		};
	}

	public static Comparator<Vertex> lenientVertexComparator() {
		return new Comparator<Vertex>() {
			@Override
			public int compare(Vertex o1, Vertex o2) {
				return o1.getType().compareTo(o2.getType());
			}
		};
	}

	public static Comparator<Pair<Vertex, Vertex>> edgeComparator(PDG g1, PDG g2) {
		return new Comparator<Pair<Vertex, Vertex>>() {
			@Override
			public int compare(Pair<Vertex, Vertex> v1, Pair<Vertex, Vertex> v2) {
				Set<Edge> edges1 = new HashSet<>(g1.getAllEdges(v1.a, v2.a));
				edges1.addAll(g1.getAllEdges(v2.a, v1.a));
				Set<Edge> edges2 = new HashSet<>(g2.getAllEdges(v1.b, v2.b));
				edges2.addAll(g2.getAllEdges(v2.b, v1.b));
				Set<String> E1 = edges1.stream().map(e -> e.getType().name()).collect(Collectors.toSet());
				Set<String> E2 = edges2.stream().map(e -> e.getType().name()).collect(Collectors.toSet());
				if (E1.equals(E2))
					return 0;
				if (E1.size() < E2.size())
					return -1;
				return 1;
			}
		};
	}

	private Set<Vertex> kSphere(Vertex v, PDG g, Set<Vertex> visited, int k) {
		Set<Vertex> result = new HashSet<>();
		if (k == 0 || visited.contains(v))
			return result;
		Set<Vertex> N = Graphs.neighborSetOf(g, v);
		visited.add(v);

		if (k == 1)
			result.addAll(N);

		for (Vertex n : N)
			result.addAll(kSphere(n, g, visited, --k));

		return result;
	}

	@Override
	public float cost(Vertex v1, Vertex v2) {
		int idx1 = (int) v1.getId();
		int idx2 = (int) v2.getId();
		return costMatrix[idx1][idx2];
	}

}
