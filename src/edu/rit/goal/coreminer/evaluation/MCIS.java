package edu.rit.goal.coreminer.evaluation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jgrapht.Graph;
import org.jgrapht.alg.clique.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

public class MCIS<V, E> {

	private Graph<Pair<V, V>, E> Cg;
	private long timeout = 500;

	public MCIS(Graph<V, E> g1, Graph<V, E> g2, Class<? extends E> edgeClass, Comparator<V> vtxCmp,
			Comparator<Pair<V, V>> edgeCmp, long timeout) {
		Cg = new CompatibilityGraph<>(g1, g2, edgeClass, vtxCmp, edgeCmp);
		this.timeout = timeout;
	}

	public MCIS(Graph<Pair<V, V>, E> Cg) {
		this.Cg = Cg;
	}

	public List<Set<Pair<V, V>>> compute(int k) {
		BronKerboschCliqueFinder<Pair<V, V>, E> bron = new BronKerboschCliqueFinder<>(Cg, timeout,
				TimeUnit.MILLISECONDS);
		Iterator<Set<Pair<V, V>>> maximumCliques = bron.maximumIterator();
		List<Set<Pair<V, V>>> result = new ArrayList<>();
		while (maximumCliques.hasNext() && k-- > 0) {
			Set<Pair<V, V>> cl = maximumCliques.next();
			result.add(cl);
		}
		return result;
	}

	public List<Set<Pair<V, V>>> compute() {
		BronKerboschCliqueFinder<Pair<V, V>, E> bron = new BronKerboschCliqueFinder<>(Cg, timeout,
				TimeUnit.MILLISECONDS);
		Iterator<Set<Pair<V, V>>> maximumCliques = bron.maximumIterator();
		List<Set<Pair<V, V>>> result = new ArrayList<>();
		while (maximumCliques.hasNext()) {
			Set<Pair<V, V>> cl = maximumCliques.next();
			result.add(cl);
		}
		return result;
	}

	public static void main(String[] args) {
		Graph<String, DefaultEdge> g1 = new DefaultUndirectedGraph<>(DefaultEdge.class);
		Graph<String, DefaultEdge> g2 = new DefaultUndirectedGraph<>(DefaultEdge.class);

		g1.addVertex("a");
		g1.addVertex("b");
		g1.addVertex("c");

		g1.addEdge("a", "b");
		g1.addEdge("b", "c");

		g2.addVertex("1");
		g2.addVertex("2");
		g2.addVertex("3");

		g2.addEdge("1", "2");
		g2.addEdge("2", "3");
		g2.addEdge("1", "3");

		MCIS<String, DefaultEdge> mcis = new MCIS<>(g1, g2, DefaultEdge.class, null, null, 5);
		List<Set<Pair<String, String>>> S = mcis.compute(2);
		System.out.println(S);
	}

	public Graph<Pair<V, V>, E> getCompatibilityGraph() {
		return Cg;
	}

}
