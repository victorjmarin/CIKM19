package edu.rit.goal.coreminer.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import edu.rit.goal.coreminer.CoreStmt;
import edu.rit.goal.coreminer.ser.CoreStmtSerializer;
import edu.rit.goal.coreminer.ser.SerCoreStmt;
import edu.rit.goal.coreminer.ser.SerVertex;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public class CoreStmtStats {

	private CoreStmt cs;
	private String groundTruth;
	protected Map<Double, Set<Pair<Vertex, Vertex>>> m_lbld;
	protected Map<Double, Set<Pair<Vertex, Vertex>>> m_astd;
	// AST leaves.
	protected Map<Double, Set<Pair<Vertex, Vertex>>> m_astld;
	protected Map<Double, Set<Pair<Vertex, Vertex>>> m_cdd;
	// Loop distance.
	protected Map<Double, Set<Pair<Vertex, Vertex>>> m_ldd;
	protected Map<Double, Set<Pair<Vertex, Vertex>>> m_ddd;

	private ASTDistance _astd;
	private ControlDepDistance _cdd;
	private DataDepDistance _ddd;

	public CoreStmtStats(CoreStmt cs, String groundTruth) {
		this.cs = cs;
		this.groundTruth = groundTruth;
		_astd = new ASTDistance();
		_cdd = new ControlDepDistance();
		_ddd = new DataDepDistance();
		m_lbld = new HashMap<>();
		m_astd = new HashMap<>();
		m_astld = new HashMap<>();
		m_cdd = new HashMap<>();
		m_ldd = new HashMap<>();
		m_ddd = new HashMap<>();
		compute();
	}

	private List<Vertex> bucketRepresentatives(Set<Vertex> nodes) {
		List<Vertex> result = new ArrayList<>();
		Set<SerCoreStmt> gtBuckets = CoreStmtSerializer.importCores(groundTruth);

		Map<String, Integer> vtx2bucket = new HashMap<>();

		for (SerCoreStmt bucket : gtBuckets) {
			for (SerVertex v : bucket.nodes) {
				String nodeStr = v.program + "-" + v.id;
				vtx2bucket.put(nodeStr, bucket.id);
			}
		}

		Set<Integer> coveredBuckets = new HashSet<>();

		for (Vertex v : nodes) {
			String nodeStr = v.getPDG().getPathToProgram() + "-" + v.getId();
			Integer bucket = vtx2bucket.get(nodeStr);
			if (coveredBuckets.contains(bucket))
				continue;
			result.add(v);
			coveredBuckets.add(bucket);
		}
		return result;
	}

	private void compute() {
		List<Vertex> nodes = new ArrayList<>(cs.getNodes());

		// Filter out nodes that are not of interest.
		nodes = nodes.stream().filter(n -> PDG.isNodeOfInterest(n)).collect(Collectors.toList());

		for (int i = 0; i < nodes.size(); i++) {
			Vertex n1 = nodes.get(i);
			for (int j = i + 1; j < nodes.size(); j++) {
				Vertex n2 = nodes.get(j);
				double lbld = jaccardDistance(n1.getTypeAndSubtypes(), n2.getTypeAndSubtypes());
				int astd = _astd.get(n1, n2);
				double astld = jaccardDistance(CoreStmt.astSignature(n1), CoreStmt.astSignature(n2));
				int[] cldd = _cdd.get(n1, n2);
				int cdd = cldd[0];
				int ldd = cldd[1];
				int ddd = _ddd.get(n1, n2);
				putValue(m_lbld, lbld, n1, n2);
				putValue(m_astd, astd, n1, n2);
				putValue(m_astld, astld, n1, n2);
				putValue(m_cdd, cdd, n1, n2);
				putValue(m_ldd, ldd, n1, n2);
				putValue(m_ddd, ddd, n1, n2);
			}
		}
	}

	public Set<Vertex> getAstOutliers(int k) {
		SummaryStatistics ss = getAstDistanceStats();
		double avg = ss.getMean();
		double dev = ss.getStandardDeviation();
		return outliers(m_astd, avg, dev, k);
	}

	public Set<Vertex> getControlDepOutliers(int k) {
		SummaryStatistics ss = getControlDepDistanceStats();
		double avg = ss.getMean();
		double dev = ss.getStandardDeviation();
		return outliers(m_cdd, avg, dev, k);
	}

	public Set<Vertex> getLoopDepOutliers(int k) {
		SummaryStatistics ss = getControlDepDistanceStats();
		double avg = ss.getMean();
		double dev = ss.getStandardDeviation();
		return outliers(m_ldd, avg, dev, k);
	}

	public Set<Vertex> getDataDepOutliers(int k) {
		SummaryStatistics ss = getDataDepDistanceStats();
		double avg = ss.getMean();
		double dev = ss.getStandardDeviation();
		return outliers(m_ddd, avg, dev, k);
	}

	public SummaryStatistics getLabelDistanceStats() {
		return stats(m_lbld);
	}

	public SummaryStatistics getAstDistanceStats() {
		return stats(m_astd);
	}

	public SummaryStatistics getAstLeavesDistanceStats() {
		return stats(m_astld);
	}

	public SummaryStatistics getControlDepDistanceStats() {
		return stats(m_cdd);
	}

	public SummaryStatistics getLoopDepDistanceStats() {
		return stats(m_ldd);
	}

	public SummaryStatistics getDataDepDistanceStats() {
		return stats(m_ddd);
	}

	public CoreStmt getCoreStmt() {
		return cs;
	}

	private Set<Vertex> outliers(Map<Double, Set<Pair<Vertex, Vertex>>> m, double avg, double dev, int k) {
		Map<Vertex, List<Double>> vtx2dists = new HashMap<>();

		for (Entry<Double, Set<Pair<Vertex, Vertex>>> e : m.entrySet()) {
			Double d = e.getKey();
			for (Pair<Vertex, Vertex> p : e.getValue()) {
				putValue(vtx2dists, p.a, d);
				putValue(vtx2dists, p.b, d);
			}
		}

		Map<Vertex, Double> vtx2avgdist = new HashMap<>();

		for (Entry<Vertex, List<Double>> e : vtx2dists.entrySet()) {
			Vertex v = e.getKey();
			List<Double> distances = e.getValue();
			double mean = distances.stream().mapToDouble(i -> i).sum() / distances.size();
			vtx2avgdist.put(v, mean);
		}

		Set<Vertex> result = new HashSet<>();

		double threshold = avg + k * dev;
		for (Entry<Vertex, Double> e : vtx2avgdist.entrySet())
			if (e.getValue() > threshold)
				result.add(e.getKey());
		return result;
	}

	private <T extends Number> SummaryStatistics stats(Map<T, Set<Pair<Vertex, Vertex>>> m) {
		SummaryStatistics result = new SummaryStatistics();
		for (Entry<T, Set<Pair<Vertex, Vertex>>> e : m.entrySet()) {
			for (int i = 0; i < e.getValue().size(); i++) {
				result.addValue(e.getKey().doubleValue());
			}
		}

		// Add distance 0 when there is a single element in the core stmt. (0 pairs)
		if (result.getN() == 0)
			result.addValue(0);

		return result;
	}

	private void putValue(Map<Double, Set<Pair<Vertex, Vertex>>> m, double d, Vertex n1, Vertex n2) {
		Set<Pair<Vertex, Vertex>> S = m.getOrDefault(d, new HashSet<>());
		if (S.isEmpty())
			m.put(d, S);
		S.add(new Pair<>(n1, n2));
	}

	private <T1, T2> void putValue(Map<T1, List<T2>> m, T1 key, T2 value) {
		List<T2> S = m.getOrDefault(key, new ArrayList<>());
		if (S.isEmpty())
			m.put(key, S);
		S.add(value);
	}

	private <T> double jaccardDistance(Set<T> a, Set<T> b) {
		Set<T> union = new HashSet<>(a);
		union.addAll(b);

		Set<T> intersection = new HashSet<>(a);
		intersection.retainAll(b);

		return 1.0 - (double) intersection.size() / union.size();
	}

	private <T> double jaccardDistance(Multiset<T> a, Multiset<T> b) {

		if (a.isEmpty() && b.isEmpty()) {
			return 1.0;
		}

		if (a.isEmpty() || b.isEmpty()) {
			return 0.0;
		}

		final int intersection = intersection(a, b).size();

		double sim = intersection / (float) (a.size() + b.size() - intersection);

		double res = 1 - sim;

		if (res < 0)
			System.out.println(res);

		return res;

	}

	static <T> Multiset<T> intersection(Multiset<T> a, Multiset<T> b) {
		if (a.size() < b.size()) {
			return Multisets.intersection(a, b);
		}

		return Multisets.intersection(b, a);
	}

}
