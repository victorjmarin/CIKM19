package edu.rit.goal.coreminer;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

public class SCAN<V, E> {

	private Graph<V, E> g;
	private double eps;
	private int mu;

	private Map<Integer, Set<V>> clusters;
	private Map<V, Set<V>> gammaCache;
	private Map<V, Set<V>> neighCache;
	private Map<V, Boolean> coreCache;
	private Set<Hub<V>> hubs;
	private Set<V> outliers;

	public Map<V, Set<Set<V>>> hub2bridgedclusters;

	public SCAN(Graph<V, E> g, double eps, int mu) {
		this.g = g;
		this.eps = eps;
		this.mu = mu;
		this.gammaCache = new HashMap<>();
		this.neighCache = new HashMap<>();
		this.coreCache = new HashMap<>();
		hub2bridgedclusters = new HashMap<>();
	}

	private void lazyRun() {
		Set<V> classified = new HashSet<>();
		Set<V> nonMember = new HashSet<>();
		clusters = new HashMap<>();
		hubs = new HashSet<>();
		outliers = new HashSet<>();
		Map<V, Integer> vertex2cluster = new HashMap<>();

		int clusterId = -1;

		Set<V> V = g.vertexSet();

		for (V v : V) {

			if (!classified.contains(v) && isCore(v)) {

				clusterId++;

				Set<V> cluster = new HashSet<>();
				clusters.put(clusterId, cluster);

				Set<V> epsNeighborhodd = epsNeighborhood(v);
				Queue<V> Q = new ArrayDeque<>(epsNeighborhodd);

				while (!Q.isEmpty()) {
					V y = Q.poll();

					if (!isCore(y))
						continue;

					for (V x : V) {
						if (dirReach(y, x) && !classified.contains(x)) {
							cluster.add(x);
							vertex2cluster.put(x, clusterId);
							Q.add(x);
							classified.add(x);
							nonMember.remove(x);
						}
					}
				}
			}

			if (!classified.contains(v))
				nonMember.add(v);
		}

		for (V v : nonMember) {
			Map<Integer, Set<V>> bridgedClusters = bridgedClusters(v, vertex2cluster);
			if (bridgedClusters.size() == 2 && bridgedClusters.containsKey(null)) {
				// outlier because it is connecting cluster with outliers.
				outliers.add(v);
			} else if (bridgedClusters.size() > 1) {
				Hub<V> hub = new Hub<>(v, bridgedClusters);
				hubs.add(hub);
			} else {
				outliers.add(v);
			}
		}
	}

	private Map<Integer, Set<V>> bridgedClusters(V v, Map<V, Integer> vertex2cluster) {
		// Bridged clusters by the hub and degree of the hub w.r.t. those clusters.
		Map<Integer, Set<V>> bridgedClusters = new HashMap<>();
		Map<Integer, Set<V>> cls2vtcs = new HashMap<>();
		Set<V> gamma = vertexStructure(v);
		for (V vtx : gamma) {

			if (vtx == v)
				continue;

			Integer cls = vertex2cluster.get(vtx);
			if (cls != null) {
				Set<V> V = cls2vtcs.get(cls);
				if (V == null) {
					V = new HashSet<>();
					cls2vtcs.put(cls, V);
				}
				V.add(vtx);
			}
			Set<V> bridgedVertices = bridgedClusters.get(cls);
			if (bridgedVertices == null) {
				bridgedVertices = new HashSet<>();
				bridgedClusters.put(cls, bridgedVertices);
			}
			bridgedVertices.add(vtx);
		}

		if (bridgedClusters.size() > 1) {
			Set<Set<V>> VV = hub2bridgedclusters.get(v);
			if (VV == null) {
				VV = new HashSet<>();
				hub2bridgedclusters.put(v, VV);
			}
			VV.addAll(cls2vtcs.values());
		}
		return bridgedClusters;
	}

//	private boolean isHub(V v, Map<V, Integer> vertex2cluster) {		
//		Set<V> gamma = vertexStructure(v);
//		List<V> l = new ArrayList<>(gamma);
//		for (int i = 0; i < l.size(); i++) {
//			for (int j = i + 1; j < l.size(); j++) {
//				V x = l.get(i);
//				V y = l.get(j);
//				if (vertex2cluster.get(x) != vertex2cluster.get(y))
//					return true;
//			}
//		}
//		return false;
//	}

	private boolean dirReach(V v, V w) {
		return isCore(v) && epsNeighborhood(v).contains(w);
	}

	private boolean isCore(V v) {
		Boolean result = coreCache.get(v);
		if (result == null) {
			Set<V> gamma = vertexStructure(v);
			if (gamma.size() < mu) {
				coreCache.put(v, false);
				return false;
			}
			Set<V> epsNeighborhood = epsNeighborhood(v);
			result = epsNeighborhood.size() >= mu;
			coreCache.put(v, result);
		}

		return result;
	}

	private Set<V> vertexStructure(V v) {
		Set<V> result = gammaCache.get(v);
		if (result == null) {
			result = Graphs.neighborSetOf(g, v);
			result.add(v);
			gammaCache.put(v, result);
		}
		return result;
	}

	private double structuralSim(V v, V w) {
		if (v == w)
			return 1.0;

		Set<V> gamma1 = new HashSet<>(vertexStructure(v));
		Set<V> gamma2 = vertexStructure(w);
		int size1 = gamma1.size();
		int size2 = gamma2.size();
		double geom = Math.sqrt(size1 * size2);
		gamma1.retainAll(gamma2);
		double result = gamma1.size() / geom;
		return result;
	}

	private Set<V> epsNeighborhood(V v) {
		Set<V> result = neighCache.get(v);
		if (result == null) {
			result = new HashSet<>();
			Set<V> gamma = vertexStructure(v);
			for (V w : gamma)
				if (structuralSim(v, w) >= eps)
					result.add(w);
			neighCache.put(v, result);
		}
		return result;
	}

	public Map<Integer, Set<V>> getClusters() {
		if (clusters == null)
			lazyRun();
		return clusters;
	}

	public Set<Hub<V>> getHubs() {
		if (hubs == null)
			lazyRun();
		return hubs;
	}

	public Set<V> getOutliers() {
		if (outliers == null)
			lazyRun();
		return outliers;
	}

}
