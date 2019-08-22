package edu.rit.goal.coreminer.evaluation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.rit.goal.sourcedg.Edge;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public class CompatibilityGraph<V, E> extends DefaultUndirectedGraph<Pair<V, V>, E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2785995250609865015L;

	private Graph<V, E> g1;
	private Graph<V, E> g2;

	public CompatibilityGraph(Graph<V, E> g1, Graph<V, E> g2, Class<? extends E> edgeClass, Comparator<V> vtxCmp,
			Comparator<Pair<V, V>> edgeCmp) {
		super(edgeClass);
		this.g1 = g1;
		this.g2 = g2;
		build(vtxCmp, edgeCmp);
	}

	private void build(Comparator<V> vtxCmp, Comparator<Pair<V, V>> edgeCmp) {
		// Add vertices V1 x V2.
		for (V v1 : g1.vertexSet()) {
			for (V v2 : g2.vertexSet()) {
				if (vtxCmp != null && vtxCmp.compare(v1, v2) != 0)
					continue;
				addVertex(new Pair<>(v1, v2));
			}
		}

		// For all pair of vertices.
		List<Pair<V, V>> V = new ArrayList<>(vertexSet());
		for (int i = 0; i < V.size(); i++) {
			Pair<V, V> v1 = V.get(i);
			for (int j = i + 1; j < V.size(); j++) {
				Pair<V, V> v2 = V.get(j);
				boolean edgeCompatible = edgeCmp.compare(v1, v2) == 0;
				if (edgeCompatible && v1.a != v2.a && v1.b != v2.b) {
					addEdge(v1, v2);
				}
			}
		}
	}

	public static DefaultUndirectedGraph<Pair<Vertex, Vertex>, DefaultWeightedEdge> build(
			Set<Triple<Vertex, Vertex, Double>> A, PDG g1, PDG g2) {
		DefaultUndirectedGraph<Pair<Vertex, Vertex>, DefaultWeightedEdge> g = new DefaultUndirectedGraph<>(
				DefaultWeightedEdge.class);

		for (Triple<Vertex, Vertex, Double> t : A) {
			if (NeighborhoodCost.strictVertexComparator().compare(t.a, t.b) != 0)
				continue;
			if (vertexNeighborhoodComparator(g1, g2).compare(t.a, t.b) != 0)
				continue;

			g.addVertex(new Pair<>(t.a, t.b));
		}

		Comparator<Pair<Vertex, Vertex>> edgeCmp = new Comparator<Pair<Vertex, Vertex>>() {

			@Override
			public int compare(Pair<Vertex, Vertex> v1, Pair<Vertex, Vertex> v2) {
				Set<Edge> edges1 = new HashSet<>(g1.getAllEdges(v1.a, v2.a));
				edges1.addAll(g1.getAllEdges(v2.a, v1.a));
				Set<Edge> edges2 = new HashSet<>(g2.getAllEdges(v1.b, v2.b));
				edges2.addAll(g2.getAllEdges(v2.b, v1.b));
				Multiset<String> E1 = HashMultiset
						.create(edges1.stream().map(e -> e.getType().name()).collect(Collectors.toList()));
				Multiset<String> E2 = HashMultiset
						.create(edges2.stream().map(e -> e.getType().name()).collect(Collectors.toList()));
				if (E1.equals(E2))
					return 0;
				if (E1.size() < E2.size())
					return -1;
				return 1;
			}
		};

		// For all pair of vertices.
		List<Pair<Vertex, Vertex>> V = new ArrayList<>(g.vertexSet());
		for (int i = 0; i < V.size(); i++) {
			Pair<Vertex, Vertex> v1 = V.get(i);
			for (int j = i + 1; j < V.size(); j++) {
				Pair<Vertex, Vertex> v2 = V.get(j);
				boolean edgeCompatible = edgeCmp.compare(v1, v2) == 0;
				if (edgeCompatible && v1.a != v2.a && v1.b != v2.b) {
					g.addEdge(v1, v2);
				}
			}
		}

		return g;
	}

	private static Comparator<Vertex> vertexNeighborhoodComparator(PDG g1, PDG g2) {
		return new Comparator<Vertex>() {

			@Override
			public int compare(Vertex o1, Vertex o2) {
				Set<Edge> edges1 = g1.edgesOfInterestOf(o1);
				Set<Edge> edges2 = g2.edgesOfInterestOf(o2);

				if (edges1.isEmpty() && !edges2.isEmpty())
					return -1;

				if (edges2.isEmpty() && !edges1.isEmpty())
					return 1;

				if (edges1.isEmpty() && edges2.isEmpty())
					return 0;

				DefaultDirectedGraph<Vertex, Edge> I1 = edgeGraph(edges1, g1);
				DefaultDirectedGraph<Vertex, Edge> I2 = edgeGraph(edges2, g2);

				DefaultDirectedGraph<Vertex, Edge> query = I1.vertexSet().size() < I2.vertexSet().size() ? I1 : I2;
				DefaultDirectedGraph<Vertex, Edge> data = query == I1 ? I2 : I1;

				VF2SubgraphIsomorphismInspector<Vertex, Edge> vf2 = new VF2SubgraphIsomorphismInspector<>(data, query,
						null, Comparator.comparing(Edge::getType));

				if (vf2.isomorphismExists())
					return 0;
				if (I1.vertexSet().size() < I2.vertexSet().size())
					return -1;
				return 1;
			}
		};
	}

	public static DefaultDirectedGraph<Vertex, Edge> edgeGraph(Set<Edge> E, PDG g) {
		DefaultDirectedGraph<Vertex, Edge> res = new DefaultDirectedGraph<>(Edge.class);
		Graphs.addAllEdges(res, g, E);
		return res;
	}

}
