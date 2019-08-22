package edu.rit.goal.coreminer.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import edu.rit.goal.coreminer.CoreStmt;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public class CoreStmtsReport {

	public double lbldMin = Double.MAX_VALUE;
	public double lbldMax;
	public double lbldAvg;
	public double lbldDev;
	public double lbldMedian;

	public double astdMin = Integer.MAX_VALUE;
	public double astdMax;
	public double astdAvg;
	public double astdDev;
	public double astdMedian;

	public double astldMin = Integer.MAX_VALUE;
	public double astldMax;
	public double astldAvg;
	public double astldDev;
	public double astldMedian;

	public double cddMin = Integer.MAX_VALUE;
	public double cddMax;
	public double cddAvg;
	public double cddDev;
	public double cddMedian;

	public double lddMin = Integer.MAX_VALUE;
	public double lddMax;
	public double lddAvg;
	public double lddDev;
	public double lddMedian;

	public double dddMin = Integer.MAX_VALUE;
	public double dddMax;
	public double dddAvg;
	public double dddDev;
	public double dddMedian;

	public double coverableStatements;
	public int coreStmts;
	public double relativeReach;
	public double absoluteReach;

	public Map<CoreStmt, CoreStmtStats> core2stats;
	public Map<PDG, Set<Vertex>> pdg2coveredNodes;

	private Collection<PDG> pdgs;
	private String groundTruth;

	public CoreStmtsReport(Collection<PDG> pdgs, Collection<CoreStmt> cs, String saveTo) {
		this.pdgs = pdgs;
		double totalNodes = pdgs.stream().map(g -> g.nodesOfInterest().size()).mapToInt(i -> i).sum();
		int coveredNodes = nodesOfInterest(cs).size();
		coverableStatements = coveredNodes / totalNodes;
		coreStmts = cs.size();
		relativeReach = (coverableStatements * 100) / coreStmts;
		absoluteReach = relativeReach * coverableStatements;
		generateReport(cs, saveTo);
		coveredNodesPerPdg(cs);
	}

	private Set<CoreStmt> touchedCores(Collection<PDG> P) {
		Set<CoreStmt> result = new HashSet<>();

		csLbl: for (CoreStmt cs : core2stats.keySet()) {
			for (PDG p : P) {
				for (Vertex v : p.vertexSet()) {
					if (cs.getNodes().contains(v)) {
						result.add(cs);
						continue csLbl;
					}
				}
			}
		}

		return result;
	}

	public Set<PDG> fullyCovered;
	public Set<PDG> almostCovered;

	private String getCoverageHistogram() {
		Map<Double, Set<PDG>> coverageHistogram = LazyMap.lazyMap(new TreeMap<>(), () -> new HashSet<>());
		for (Entry<PDG, Set<Vertex>> e : pdg2coveredNodes.entrySet()) {
			double coverable = coverableStmts(e);
			coverageHistogram.get(coverable).add(e.getKey());
		}

		TreeMap<Double, Set<PDG>> t = new TreeMap<>(coverageHistogram);
		SortedMap<Double, Set<PDG>> g1 = t.subMap(0.0, .2);
		SortedMap<Double, Set<PDG>> g2 = t.subMap(.2, .4);
		SortedMap<Double, Set<PDG>> g3 = t.subMap(.4, .6);
		SortedMap<Double, Set<PDG>> g4 = t.subMap(.6, .8);
		SortedMap<Double, Set<PDG>> g5 = t.subMap(.8, 1.0);
		SortedMap<Double, Set<PDG>> g6 = t.tailMap(1.0);

		fullyCovered = t.get(1.0);
		almostCovered = g5.values().stream().flatMap(s -> s.stream()).collect(Collectors.toSet());

		Function<Collection<Set<PDG>>, Integer> count = c -> c.stream().flatMap(v -> v.stream())
				.collect(Collectors.toSet()).size();

		int c1 = count.apply(g1.values());
		int c2 = count.apply(g2.values());
		int c3 = count.apply(g3.values());
		int c4 = count.apply(g4.values());
		int c5 = count.apply(g5.values());
		int c6 = count.apply(g6.values());

		int partiallyCovered = c1 + c2 + c3 + c4 + c5 + c6;

		int notCoveredAtAll = pdgs.size() - partiallyCovered;

		c1 += notCoveredAtAll;

		int g1Cores = touchedCores(g1.values().stream().flatMap(v -> v.stream()).collect(Collectors.toSet())).size();
		int g2Cores = touchedCores(g2.values().stream().flatMap(v -> v.stream()).collect(Collectors.toSet())).size();
		int g3Cores = touchedCores(g3.values().stream().flatMap(v -> v.stream()).collect(Collectors.toSet())).size();
		int g4Cores = touchedCores(g4.values().stream().flatMap(v -> v.stream()).collect(Collectors.toSet())).size();
		int g5Cores = touchedCores(g5.values().stream().flatMap(v -> v.stream()).collect(Collectors.toSet())).size();
		int g6Cores = touchedCores(g6.values().stream().flatMap(v -> v.stream()).collect(Collectors.toSet())).size();

		StringBuilder sb = new StringBuilder();
		sb.append("Coverage histogram" + "\n");
		sb.append("[ 0, .2)=" + c1 + " (" + g1Cores + " cs)" + "\n");
		sb.append("[.2, .4)=" + c2 + " (" + g2Cores + " cs)" + "\n");
		sb.append("[.4, .6)=" + c3 + " (" + g3Cores + " cs)" + "\n");
		sb.append("[.6, .8)=" + c4 + " (" + g4Cores + " cs)" + "\n");
		sb.append("[.8,  1)=" + c5 + " (" + g5Cores + " cs)" + "\n");
		sb.append("[ 1,  1]=" + c6 + " (" + g6Cores + " cs)");

		return sb.toString();
	}

	public double coverableStmts(Entry<PDG, Set<Vertex>> e) {
		double nodes = e.getKey().nodesOfInterest().size();
		double result = e.getValue().stream().filter(v -> PDG.isNodeOfInterest(v)).collect(Collectors.toSet()).size()
				/ nodes;
		return result;
	}

	private void coveredNodesPerPdg(Collection<CoreStmt> cs) {
		pdg2coveredNodes = LazyMap.lazyMap(new HashMap<>(), () -> new HashSet<>());
		for (CoreStmt c : cs) {
			for (Vertex v : c.getNodes()) {
				pdg2coveredNodes.get(v.getPDG()).add(v);
			}
		}
	}

	private Set<Vertex> nodesOfInterest(Collection<CoreStmt> cs) {
		Set<Vertex> result = new HashSet<>();
		for (CoreStmt c : cs) {
			for (Vertex v : c.getNodes()) {
				if (PDG.isNodeOfInterest(v))
					result.add(v);
			}
		}
		return result;
	}

	private void logStats(Set<CoreStmtStats> stats, String saveTo) {
		for (CoreStmtStats csStats : stats) {
			log(csStats.m_cdd, csStats.getCoreStmt().getId(), saveTo + "-cdd.txt");
			log(csStats.m_ddd, csStats.getCoreStmt().getId(), saveTo + "-ddd.txt");
			log(csStats.m_ldd, csStats.getCoreStmt().getId(), saveTo + "-ldd.txt");
			log(csStats.m_lbld, csStats.getCoreStmt().getId(), saveTo + "-lbld.txt");
			log(csStats.m_astld, csStats.getCoreStmt().getId(), saveTo + "-astld.txt");
		}
	}

	private void log(Map<Double, Set<Pair<Vertex, Vertex>>> m, int coreId, String saveTo) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(saveTo, true);
			for (Entry<Double, Set<Pair<Vertex, Vertex>>> e : m.entrySet()) {
				for (int i = 0; i < e.getValue().size(); i++) {
					writer.write(coreId + "," + e.getKey() + "\n");
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void generateReport(Collection<CoreStmt> cs, String saveTo) {
		core2stats = new HashMap<>();
		int coresOfInterest = 0;

		Set<CoreStmtStats> stats = cs.parallelStream().map(c -> new CoreStmtStats(c, groundTruth))
				.collect(Collectors.toSet());

		if (saveTo != null)
			logStats(stats, saveTo);

		List<Double> lbldMedians = new ArrayList<>();
		List<Double> astdMedians = new ArrayList<>();
		List<Double> astldMedians = new ArrayList<>();
		List<Double> cddMedians = new ArrayList<>();
		List<Double> lddMedians = new ArrayList<>();
		List<Double> dddMedians = new ArrayList<>();

		for (CoreStmtStats st : stats) {

			if (st.getAstDistanceStats().getN() == 0)
				continue;

			if (!st.m_lbld.isEmpty())
				lbldMedians.add(median(st.m_lbld));
			if (!st.m_astd.isEmpty())
				astdMedians.add(median(st.m_astd));
			if (!st.m_astld.isEmpty())
				astldMedians.add(median(st.m_astld));
			if (!st.m_cdd.isEmpty())
				cddMedians.add(median(st.m_cdd));
			if (!st.m_ldd.isEmpty())
				lddMedians.add(median(st.m_ldd));
			if (!st.m_ddd.isEmpty())
				dddMedians.add(median(st.m_ddd));

			SummaryStatistics lbldStats = st.getLabelDistanceStats();
			SummaryStatistics astdStats = st.getAstDistanceStats();
			SummaryStatistics astldStats = st.getAstLeavesDistanceStats();
			SummaryStatistics cddStats = st.getControlDepDistanceStats();
			SummaryStatistics lddStats = st.getLoopDepDistanceStats();
			SummaryStatistics dddStats = st.getDataDepDistanceStats();

			lbldMin = Math.min(lbldMin, lbldStats.getMin());
			astdMin = Math.min(astdMin, astdStats.getMin());
			astldMin = Math.min(astldMin, astldStats.getMin());
			cddMin = Math.min(cddMin, cddStats.getMin());
			lddMin = Math.min(lddMin, lddStats.getMin());
			dddMin = Math.min(dddMin, dddStats.getMin());

			lbldMax = Math.max(lbldMax, lbldStats.getMax());
			astdMax = Math.max(astdMax, astdStats.getMax());
			astldMax = Math.max(astldMax, astldStats.getMax());
			cddMax = Math.max(cddMax, cddStats.getMax());
			lddMax = Math.max(lddMax, lddStats.getMax());
			dddMax = Math.max(dddMax, dddStats.getMax());

			lbldAvg += lbldStats.getMean();
			astdAvg += astdStats.getMean();
			astldAvg += astldStats.getMean();
			cddAvg += cddStats.getMean();
			lddAvg += lddStats.getMean();
			dddAvg += dddStats.getMean();

			lbldDev += lbldStats.getStandardDeviation();
			astdDev += astdStats.getStandardDeviation();
			astldDev += astldStats.getStandardDeviation();
			cddDev += cddStats.getStandardDeviation();
			lddDev += lddStats.getStandardDeviation();
			dddDev += dddStats.getStandardDeviation();

			coresOfInterest++;

			core2stats.put(st.getCoreStmt(), st);
		}

		lbldMedian = median(lbldMedians);
		astdMedian = median(astdMedians);
		astldMedian = median(astldMedians);
		cddMedian = median(cddMedians);
		lddMedian = median(lddMedians);
		dddMedian = median(dddMedians);

		lbldAvg /= coresOfInterest;
		astdAvg /= coresOfInterest;
		astldAvg /= coresOfInterest;
		cddAvg /= coresOfInterest;
		lddAvg /= coresOfInterest;
		dddAvg /= coresOfInterest;

		lbldDev /= coresOfInterest;
		astdDev /= coresOfInterest;
		astldDev /= coresOfInterest;
		cddDev /= coresOfInterest;
		lddDev /= coresOfInterest;
		dddDev /= coresOfInterest;
	}

	private double getCoverablePerProgram() {
		SummaryStatistics ss = new SummaryStatistics();
		for (Entry<PDG, Set<Vertex>> e : pdg2coveredNodes.entrySet()) {
			double coverable = (e.getValue().size() * 1.0) / e.getKey().nodesOfInterest().size();
			ss.addValue(coverable);
		}
		return ss.getMean();
	}

	private double median(Map<Double, Set<Pair<Vertex, Vertex>>> m) {
		return median(getValues(m));
	}

	private double median(List<Double> lst) {
		if (lst.isEmpty()) {
			System.err.println("Empty list for median.");
			return 0;
		}
		Collections.sort(lst);
		double result = lst.get(lst.size() / 2).doubleValue();
		if (lst.size() % 2 == 0)
			result = (result + lst.get(lst.size() / 2 - 1).doubleValue()) / 2;
		return result;
	}

	private <T> List<T> getValues(Map<T, Set<Pair<Vertex, Vertex>>> m) {
		List<T> result = new ArrayList<>();
		for (Entry<T, Set<Pair<Vertex, Vertex>>> e : m.entrySet()) {
			for (int i = 0; i < e.getValue().size(); i++) {
				result.add(e.getKey());
			}
		}
		return result;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("lbld_min=" + lbldMin);
		sb.append("\n");
		sb.append("lbld_mean=" + lbldAvg);
		sb.append("\n");
		sb.append("lbld_dev=" + lbldDev);
		sb.append("\n");
		sb.append("lbld_median=" + lbldMedian);
		sb.append("\n");
		sb.append("lbld_max=" + lbldMax);
		sb.append("\n");
		sb.append("\n");
		sb.append("astld_min=" + astldMin);
		sb.append("\n");
		sb.append("astld_mean=" + astldAvg);
		sb.append("\n");
		sb.append("astld_dev=" + astldDev);
		sb.append("\n");
		sb.append("astld_median=" + astldMedian);
		sb.append("\n");
		sb.append("astld_max=" + astldMax);
		sb.append("\n");
		sb.append("\n");
		sb.append("astd_min=" + astdMin);
		sb.append("\n");
		sb.append("astd_mean=" + astdAvg);
		sb.append("\n");
		sb.append("astd_dev=" + astdDev);
		sb.append("\n");
		sb.append("astd_median=" + astdMedian);
		sb.append("\n");
		sb.append("astd_max=" + astdMax);
		sb.append("\n");
		sb.append("\n");
		sb.append("cdd_min=" + cddMin);
		sb.append("\n");
		sb.append("cdd_mean=" + cddAvg);
		sb.append("\n");
		sb.append("cdd_dev=" + cddDev);
		sb.append("\n");
		sb.append("cdd_median=" + cddMedian);
		sb.append("\n");
		sb.append("cdd_max=" + cddMax);
		sb.append("\n");
		sb.append("\n");
		sb.append("ldd_min=" + lddMin);
		sb.append("\n");
		sb.append("ldd_mean=" + lddAvg);
		sb.append("\n");
		sb.append("ldd_dev=" + lddDev);
		sb.append("\n");
		sb.append("ldd_median=" + lddMedian);
		sb.append("\n");
		sb.append("ldd_max=" + lddMax);
		sb.append("\n");
		sb.append("\n");
		sb.append("ddd_in_min=" + dddMin);
		sb.append("\n");
		sb.append("ddd_in_mean=" + dddAvg);
		sb.append("\n");
		sb.append("ddd_in_dev=" + dddDev);
		sb.append("\n");
		sb.append("ddd_in_median=" + dddMedian);
		sb.append("\n");
		sb.append("ddd_in_max=" + dddMax);
		sb.append("\n");
		sb.append("\n");
		sb.append("core_stmts=" + coreStmts);
		sb.append("\n");
		sb.append("coverable_stmts=" + coverableStatements);
		sb.append("\n");
		sb.append("coverable_per_program=" + getCoverablePerProgram());
		sb.append("\n");
		sb.append("R_rel=" + relativeReach);
		sb.append("\n");
		sb.append("R_abs=" + absoluteReach);
		sb.append("\n");
		sb.append(getCoverageHistogram());
		return sb.toString();
	}

}
