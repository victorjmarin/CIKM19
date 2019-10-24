package edu.rit.goal.pdgalign;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.rit.goal.pdgalign.graphlet.DirectedGraphletCounter;
import edu.rit.goal.sourcedg.Vertex;


public class DirectedOrbitsCost {

	private static final double BETA = .75;

	private DirectedCostGraph g1;
	private DirectedCostGraph g2;
	private double[][] costMat;
	private int minG1, minG2;

	private Map<Vertex, Set<String>> typesCache = new ConcurrentHashMap<>();
	private Map<Integer, Float> jaccardCache = new ConcurrentHashMap<>();

	public DirectedOrbitsCost(DirectedCostGraph g1, DirectedCostGraph g2) {
		this.g1 = g1;
		this.g2 = g2;
	}

	private void buildCostMatrix() {
		// Initialize cost matrix.
		minG1 = (int) g1.vertexSet().stream().map(v -> v.getId()).mapToLong(Long::longValue).min().getAsLong();
		minG2 = (int) g2.vertexSet().stream().map(v -> v.getId()).mapToLong(Long::longValue).min().getAsLong();
		int maxG1 = (int) g1.vertexSet().stream().map(v -> v.getId()).mapToLong(Long::longValue).max().getAsLong();
		int maxG2 = (int) g2.vertexSet().stream().map(v -> v.getId()).mapToLong(Long::longValue).max().getAsLong();
		int dim1 = maxG1 - minG1 + 1;
		int dim2 = maxG2 - minG2 + 1;
		costMat = new double[dim1][dim2];

		for (List<Vertex> l1 : g1.getSymmetricNodes().values()) {
			Vertex representative1 = l1.get(0);
			for (List<Vertex> l2 : g2.getSymmetricNodes().values()) {
				Vertex representative2 = l2.get(0);
				double topologicalCost = topologicalCost(representative1, representative2);

				// Topological cost is shared among symmetric vertices.
				for (Vertex v1 : l1)
					for (Vertex v2 : l2) {
						int i = (int) (v1.getId() - minG1);
						int j = (int) (v2.getId() - minG2);
						double semanticCost = semanticCost(v1, v2);

						costMat[i][j] = (float) ((BETA * topologicalCost + (1 - BETA) * semanticCost) / 2);

						// Semantic cost as a modifier of topological cost.
						// double finalCost = (1 + semanticCost) * topologicalCost;

//						double harmonicCost = 2 * topologicalCost * semanticCost / (topologicalCost + semanticCost);
//						double topSim = 1 - topologicalCost;
//						double semSim = 1 - semanticCost;
//						double harmCost = 1 - (2 * topSim * semSim) / (topSim + semSim);
//						if (Double.isNaN(harmonicCost) || harmonicCost == 0d)
//							if (!Double.isNaN(harmCost))
//								harmonicCost = harmCost;
//						costMat[i][j] = (float) harmonicCost;
					}
			}
		}
	}

	private float topologicalCost(Vertex v1, Vertex v2) {
		final int[] signV1 = g1.getSignatures().get(v1);
		final int[] signV2 = g2.getSignatures().get(v2);
		double num = 0;
		for (int i = 0; i < DirectedGraphletCounter.numberOfOrbits; i++)
			// Only if one of them is greater than zero to avoid divided by zero error.
			if (signV1[i] > 0 || signV2[i] > 0)
				num += DirectedGraphletCounter.weights[i] * Math.abs(Math.log1p(signV1[i]) - Math.log1p(signV2[i]))
						/ Math.log1p(Math.max(signV1[i], signV2[i]));
		return (float) (num / DirectedGraphletCounter.weightSum);
	}

	public double euclidean(int[] p1, int[] p2) {
		double sum = .0;
		for (int i = 0; i < p1.length; i++) {
			double dp = p1[i] - p2[i];
			dp *= DirectedGraphletCounter.weights[i];
			sum += dp * dp;
		}
		return Math.sqrt(sum);
	}

	private double semanticCost(Vertex v1, Vertex v2) {
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

	public double jaccardIndex(Set<String> s1, Set<String> s2) {
		if (s1.isEmpty() && s2.isEmpty())
			return 1.0;

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

	public double jaccardDistance(Set<String> s1, Set<String> s2) {
		return 1.0 - jaccardIndex(s1, s2);
	}

	public double get(Vertex v1, Vertex v2) {
		if (costMat == null) {
			buildCostMatrix();
		}
		int i = (int) (v1.getId() - minG1);
		int j = (int) (v2.getId() - minG2);
		return costMat[i][j];
	}

}
