package edu.rit.goal.coreminer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LazyMap;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.rit.goal.coreminer.evaluation.InducedSubgraph;
import edu.rit.goal.sourcedg.Vertex;

public class SCANExtractor {

	private Graph<Vertex, DefaultWeightedEdge> A;
	private double eps;
	private int mu;
	private Map<Integer, CoreStmt> corestmts;

	private static int clusterId = 0;

	public SCANExtractor(Graph<Vertex, DefaultWeightedEdge> A, double eps, int mu) {
		this.A = A;
		this.eps = eps;
		this.mu = mu;
		this.corestmts = new HashMap<>();
	}

	public void computeCoreStmts() {
		Set<CoreStmt> corestmtsSet = new HashSet<>();

		SCAN<Vertex, DefaultWeightedEdge> scan = new SCAN<>(A, eps, mu);
		Map<Integer, Set<Vertex>> clusters = scan.getClusters();
		int outliers = scan.getOutliers().size();
		Set<Vertex> hubVtcs = scan.getHubs().stream().map(h -> h.getVertex()).collect(Collectors.toSet());

		for (Set<Vertex> CV : clusters.values()) {
			corestmtsSet.add(new CoreStmt(-1, CV));
		}

		for (CoreStmt cs : corestmtsSet) {
			cs.setId(clusterId);
			corestmts.put(clusterId++, cs);
		}

		System.out.println("SCAN outliers=" + outliers);
		System.out.println("hubs=" + hubVtcs.size());

		checkForDuped();
	}

	public void checkForDuped() {

		Set<Integer> duped = new HashSet<>();
		Map<Integer, CoreStmt> dedupedCorestmts = new HashMap<>();

		for (Entry<Integer, CoreStmt> csEntry : corestmts.entrySet()) {
			CoreStmt cs = csEntry.getValue();
			Set<String> submissions = new HashSet<>();

			Map<String, Set<Vertex>> sub2nodes = LazyMap.lazyMap(new HashMap<>(), () -> new HashSet<>());
			for (Vertex v : cs.getNodes()) {
				sub2nodes.get(v.getSubmission()).add(v);
				submissions.add(v.getSubmission());
			}

			if (cs.getNodes().size() != submissions.size()) {
				duped.add(csEntry.getKey());

				System.out.println();
				System.out.println("core_stmt_id=" + cs.getId());
				System.out.println("nodes=" + cs.getNodes().size());
				System.out.println("submissions=" + submissions.size());

				InducedSubgraph<Vertex, DefaultWeightedEdge> I = new InducedSubgraph<>(A, cs.getNodes());

				for (Entry<String, Set<Vertex>> e : sub2nodes.entrySet()) {
					if (e.getValue().size() > 1) {
						System.out.println(e.getKey());
						System.out.println(e.getValue());
					}
				}

				System.out.println("Dealing with duped stmts...");
				SCANExtractor ext = new SCANExtractor(I, 0.9, mu);
				ext.computeCoreStmts();
				System.out.println("\nDeduped into " + ext.corestmts.size() + " core stmts.");
				dedupedCorestmts.putAll(ext.corestmts);
			}

		}

		corestmts.putAll(dedupedCorestmts);
		duped.forEach(k -> corestmts.remove(k));

	}

	public Map<Integer, CoreStmt> getCoreStmts() {
		return corestmts;
	}

}
