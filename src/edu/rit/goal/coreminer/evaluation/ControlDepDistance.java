package edu.rit.goal.coreminer.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import edu.rit.goal.sourcedg.Edge;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;
import edu.rit.goal.sourcedg.VertexType;


public class ControlDepDistance {

	private Map<Vertex, EdgesAndLoops> vtx2data;

	public ControlDepDistance() {
		vtx2data = new HashMap<>();
	}

	public int[] get(Vertex n1, Vertex n2) {

		EdgesAndLoops data1 = vtx2data.get(n1);
		EdgesAndLoops data2 = vtx2data.get(n2);

		if (data1 == null)
			data1 = compute(n1);

		if (data2 == null)
			data2 = compute(n2);

		return new int[] { Math.abs(data1.edges - data2.edges), Math.abs(data1.loops - data2.loops) };
	}

	public EdgesAndLoops compute(Vertex n) {
		PDG pdg = n.getPDG();

		Vertex entry = pdg.getEntry();

		DefaultDirectedGraph<Vertex, Edge> cdg = pdg.getCDG();

		DijkstraShortestPath<Vertex, Edge> sp = new DijkstraShortestPath<>(cdg);

		GraphPath<Vertex, Edge> path = sp.getPath(entry, n);

		int tryNodes = (int) path.getVertexList().stream().filter(v -> VertexType.TRY.equals(v.getType())).count();

		List<Edge> E = path.getEdgeList();

		List<Vertex> N = path.getVertexList();

		List<String> ctrlSeq = E.stream().map(e -> e.getType().name()).collect(Collectors.toList());

		int loops = loopCount(N, cdg);

		// Do not take try into account.
		int nestingLevel = E.size() - tryNodes;

		EdgesAndLoops result = new EdgesAndLoops(nestingLevel, ctrlSeq, loops);

		vtx2data.put(n, result);

		return result;
	}

	private <V, E> int loopCount(List<V> l, Graph<V, E> g) {
		int result = 0;

		for (V v : l)
			for (E e : g.incomingEdgesOf(v))
				if (g.getEdgeSource(e).equals(g.getEdgeTarget(e)))
					result++;

		return result;
	}

	public class EdgesAndLoops {

		public int edges;
		public List<String> ctrlSeq;
		public int loops;

		public EdgesAndLoops(int edges, List<String> ctrlSeq, int loops) {
			super();
			this.edges = edges;
			this.ctrlSeq = ctrlSeq;
			this.loops = loops;
		}

	}

}
