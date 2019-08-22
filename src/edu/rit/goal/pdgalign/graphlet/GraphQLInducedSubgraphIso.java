package edu.rit.goal.pdgalign.graphlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;

public class GraphQLInducedSubgraphIso<V1, V2, E1, E2> {
	// V1 and V2 are generic classes for query and data nodes.
	// E1 and E2 are generic classes for query and data edges.

	private DefaultDirectedGraph<V1, E1> query;
	private DefaultDirectedGraph<V2, E2> data;
	private V1 firstInOrder;
	private Set<Map<V1, V2>> solutions = new HashSet<>();

	public GraphQLInducedSubgraphIso(DefaultDirectedGraph<V1, E1> query, DefaultDirectedGraph<V2, E2> data) {
		super();
		this.query = query;
		this.data = data;
	}

	private Set<V1> getOutgoingQueryNeighbors(V1 u) {
		Set<V1> neigh = new HashSet<>();
		for (E1 e : query.outgoingEdgesOf(u))
			neigh.add(query.getEdgeTarget(e));
		return neigh;
	}

	private Set<V1> getIncomingQueryNeighbors(V1 u) {
		Set<V1> neigh = new HashSet<>();
		for (E1 e : query.incomingEdgesOf(u))
			neigh.add(query.getEdgeSource(e));
		return neigh;
	}

	private Set<V2> getOutgoingDataNeighbors(V2 v) {
		Set<V2> neigh = new HashSet<>();
		for (E2 e : data.outgoingEdgesOf(v))
			neigh.add(data.getEdgeTarget(e));
		return neigh;
	}

	private Set<V2> getIncomingDataNeighbors(V2 v) {
		Set<V2> neigh = new HashSet<>();
		for (E2 e : data.incomingEdgesOf(v))
			neigh.add(data.getEdgeSource(e));
		return neigh;
	}

	public Set<Map<V1, V2>> compute() {
		// Check size to compute subgraph only.
		if (query.vertexSet().size() <= data.vertexSet().size() && query.edgeSet().size() <= data.edgeSet().size()) {
			// Compute search space.
			Map<V1, List<V2>> searchSpace = computeSearchSpace();
			if (!searchSpace.isEmpty()) {
				List<V1> order = computeOrder(searchSpace);
				stateSearch(0, order, searchSpace, new HashMap<>());
			}
		}
		return solutions;
	}

	private Map<V1, List<V2>> computeSearchSpace() {
		Map<V1, List<V2>> searchSpace = new HashMap<>();

		int minSize = Integer.MAX_VALUE;
		boolean emptySS = false;
		for (V1 u : query.vertexSet()) {
			List<V2> list = new ArrayList<>();
			searchSpace.put(u, list);

			for (V2 v : data.vertexSet())
				if (query.outDegreeOf(u) <= data.outDegreeOf(v))
					list.add(v);

			// No results for this query.
			if (list.isEmpty()) {
				emptySS = true;
				break;
			}

			if (minSize > list.size()) {
				firstInOrder = u;
				minSize = list.size();
			}
		}

		if (emptySS)
			searchSpace.clear();

		return searchSpace;
	}

	private List<V1> computeOrder(Map<V1, List<V2>> searchSpace) {
		double d = edgeReduction(1, searchSpace);

		List<V1> order = new ArrayList<>();
		order.add(firstInOrder);
		Set<V1> toTreat = new HashSet<>(query.vertexSet());
		toTreat.remove(firstInOrder);

		while (toTreat.size() > 0) {
			V1 next = null;
			double minCost = Double.MAX_VALUE;

			for (V1 u : toTreat) {
				double cost = (double) searchSpace.get(u).size() * joinReduction(u, order, d);
				if (cost < minCost) {
					next = u;
					minCost = cost;
				}
			}

			order.add(next);
			toTreat.remove(next);
		}

		return order;
	}

	private double edgeReduction(double j, Map<V1, List<V2>> searchSpace) {
		double d = Math.log(j);
		for (V1 u : query.vertexSet())
			d -= Math.log(searchSpace.get(u).size());
		return Math.exp(d / query.vertexSet().size());
	}

	private double joinReduction(V1 u, List<V1> order, double d) {
		Set<V1> connections = getOutgoingQueryNeighbors(u);
		connections.retainAll(order);
		return Math.pow(0.5, connections.size());
	}

	private void stateSearch(int current, List<V1> order, Map<V1, List<V2>> searchSpace, Map<V1, V2> currentSolution) {
		V1 u = order.get(current);

		for (Iterator<V2> it = searchSpace.get(u).iterator(); it.hasNext();) {
			V2 v = it.next();

			if (!currentSolution.values().contains(v) && canMap(u, v, currentSolution)
					&& canMapInduced(u, v, currentSolution)) {
				currentSolution.put(u, v);

				if (current == order.size() - 1)
					processSolution(currentSolution);
				else
					stateSearch(current + 1, order, searchSpace, currentSolution);

				currentSolution.remove(u);
			}
		}
	}

	private void processSolution(Map<V1, V2> solution) {
		solutions.add(new HashMap<>(solution));
	}

	private boolean canMap(V1 u, V2 v, Map<V1, V2> currentSolution) {
		boolean canMap = true;

		Set<V1> qOutNeighbors = getOutgoingQueryNeighbors(u), qInNeighbors = getIncomingQueryNeighbors(u);
		Set<V2> dOutNeighbors = getOutgoingDataNeighbors(v), dInNeighbors = getIncomingDataNeighbors(v);

		for (Iterator<V1> it = qOutNeighbors.iterator(); canMap && it.hasNext();) {
			V1 qN = it.next();
			V2 dN = currentSolution.get(qN);

			canMap = dN == null || dOutNeighbors.contains(dN);
		}

		for (Iterator<V1> it = qInNeighbors.iterator(); canMap && it.hasNext();) {
			V1 qN = it.next();
			V2 dN = currentSolution.get(qN);

			canMap = dN == null || dInNeighbors.contains(dN);
		}

		return canMap;
	}

	private boolean canMapInduced(V1 u, V2 v, Map<V1, V2> currentSolution) {
		boolean canMap = true;

		// Let's get all the nodes that are not neighbors of u.
		Set<V1> allQNodes = new HashSet<>(query.vertexSet());

		// For the induced version, we need to check all neighbors of u.
		allQNodes.remove(u);
		allQNodes.removeAll(getOutgoingQueryNeighbors(u));
		Set<V2> dNeighbors = getOutgoingDataNeighbors(v);

		// For the remaining nodes, there should not be an edge connecting them.
		for (Iterator<V1> it = allQNodes.iterator(); canMap && it.hasNext();) {
			V1 x = it.next();
			V2 y = currentSolution.get(x);

			canMap = y == null || !dNeighbors.contains(y);
		}

		allQNodes = new HashSet<>(query.vertexSet());
		allQNodes.remove(u);
		allQNodes.removeAll(getIncomingQueryNeighbors(u));
		dNeighbors = getIncomingDataNeighbors(v);

		// For the remaining nodes, there should not be an edge connecting them.
		for (Iterator<V1> it = allQNodes.iterator(); canMap && it.hasNext();) {
			V1 x = it.next();
			V2 y = currentSolution.get(x);

			canMap = y == null || !dNeighbors.contains(y);
		}

		return canMap;
	}

}