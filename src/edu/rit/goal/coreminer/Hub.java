package edu.rit.goal.coreminer;

import java.util.Map;
import java.util.Set;

public class Hub<V> {

	private V vertex;

	// Bridged clusters by the hub and degree of the hub w.r.t. those clusters.
	private Map<Integer, Set<V>> bridgedClusters;

	public Hub(V vertex, Map<Integer, Set<V>> bridgedClusters) {
		this.vertex = vertex;
		this.bridgedClusters = bridgedClusters;
	}

	public V getVertex() {
		return vertex;
	}

	public Map<Integer, Set<V>> getBridgedClusters() {
		return bridgedClusters;
	}

}
