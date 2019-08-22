package edu.rit.goal.coreminer.evaluation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;

import edu.rit.goal.sourcedg.Edge;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public class DataDepDistance {

	Map<Vertex, InducedSubgraph<Vertex, Edge>> vtx2subgraph;

	public DataDepDistance() {
		vtx2subgraph = new HashMap<>();
	}

	public int get(Vertex n1, Vertex n2) {

		InducedSubgraph<Vertex, Edge> I1 = vtx2subgraph.get(n1);
		InducedSubgraph<Vertex, Edge> I2 = vtx2subgraph.get(n2);

		if (I1 == null)
			I1 = compute(n1);

		if (I2 == null)
			I2 = compute(n2);

		final InducedSubgraph<Vertex, Edge> fI1 = I1;
		final InducedSubgraph<Vertex, Edge> fI2 = I2;

		CompatibilityGraph<Vertex, Edge> Cg = new CompatibilityGraph<>(I1, I2, Edge.class,
				NeighborhoodCost.lenientVertexComparator(), new Comparator<Pair<Vertex, Vertex>>() {
					@Override
					public int compare(Pair<Vertex, Vertex> v1, Pair<Vertex, Vertex> v2) {
						Set<Edge> edges1 = new HashSet<>(fI1.getAllEdges(v1.a, v2.a));
						edges1.addAll(fI1.getAllEdges(v2.a, v1.a));
						Set<Edge> edges2 = new HashSet<>(fI2.getAllEdges(v1.b, v2.b));
						edges2.addAll(fI2.getAllEdges(v2.b, v1.b));
						if (edges1.size() == edges2.size())
							return 0;
						if (edges1.size() < edges1.size())
							return -1;
						return 1;
					}
				});

		MCIS<Vertex, Edge> mcis = new MCIS<>(Cg);
		List<Set<Pair<Vertex, Vertex>>> K = mcis.compute(1);

		int common = 0;

		if (!K.isEmpty()) {
			common = K.get(0).size();
		}

		int result = Math.max(I1.vertexSet().size(), I2.vertexSet().size()) - common;
		return result;

	}

	public InducedSubgraph<Vertex, Edge> compute(Vertex n) {
		PDG pdg = n.getPDG();

		DefaultDirectedGraph<Vertex, Edge> ddg = pdg.getDDG();

		// Set<Vertex> V1 = Graphs.neighborSetOf(ddg, n);
		Set<Vertex> V1 = new HashSet<>();
		Set<Edge> inEdges = ddg.incomingEdgesOf(n);

		for (Edge e : inEdges) {
			V1.add(ddg.getEdgeSource(e));
			V1.add(ddg.getEdgeTarget(e));
		}

		V1.add(n);

		InducedSubgraph<Vertex, Edge> I = new InducedSubgraph<>(ddg, V1);

		vtx2subgraph.put(n, I);

		return I;
	}

}
