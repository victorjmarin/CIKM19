package edu.rit.goal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LazyMap;
import org.jgrapht.alg.scoring.Coreness;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.ExportException;

import edu.rit.goal.coreminer.CoreStmt;
import edu.rit.goal.coreminer.ProgramUtils;
import edu.rit.goal.coreminer.SCANExtractor;
import edu.rit.goal.coreminer.evaluation.CoreStmtsReport;
import edu.rit.goal.coreminer.ser.CoreStmtSerializer;
import edu.rit.goal.pdgalign.Aligner;
import edu.rit.goal.pdgalign.DirectedMatchingAligner;
import edu.rit.goal.pdgalign.PairwiseAligner;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

/**
 * Runs an iterative process for core statement mining with µ set to a percentage of the submissions and fixed ε.
 *
 */
public class Exp2 {
	
	// Minimum number of statements to form a core statement.
	private static final int MIN_MU = 5;

	/** 
	 * Different experiment configurations expressed as {assignment_id, epsilon, percentage of submissions to use as mu}.
	 * 
	 * assignment	id		URL
	 * ---------------------------------------------------------------
	 * JOHNY		0		https://www.codechef.com/problems/JOHNY
	 * CARVANS		1		https://www.codechef.com/problems/CARVANS
	 * BUYING2		2		https://www.codechef.com/problems/BUYING2
	 * MUFFINS3		3		https://www.codechef.com/problems/MUFFINS3
	 * CLEANUP		4		https://www.codechef.com/problems/CLEANUP
	 * CONFLIP		5		https://www.codechef.com/problems/CONFLIP
	 * LAPIN		6		https://www.codechef.com/problems/LAPIN
	 * PERMUT2		7		https://www.codechef.com/problems/PERMUT2
	 * STONES		8		https://www.codechef.com/problems/STONES
	 * SUMTRIAN		9		https://www.codechef.com/problems/SUMTRIAN
	 * 
	 */
	private static final double[][] EXP = { { 7, 0.7, 0.05 }, { 7, 0.7, 0.1 }, { 0, 0.6, 0.05 }, { 0, 0.6, 0.1 },
			{ 0, 1.0, 0.05 }, { 0, 1.0, 0.1 }, { 1, 0.6, 0.05 }, { 1, 0.6, 0.1 }, { 1, 1.0, 0.05 }, { 1, 1.0, 0.1 },
			{ 2, 0.6, 0.05 }, { 2, 0.6, 0.1 }, { 2, 1.0, 0.05 }, { 2, 1.0, 0.1 }, { 3, 0.6, 0.05 }, { 3, 0.6, 0.1 },
			{ 3, 1.0, 0.05 }, { 3, 1.0, 0.1 }, { 4, 0.6, 0.05 }, { 4, 0.6, 0.1 }, { 4, 1.0, 0.05 }, { 4, 1.0, 0.1 },
			{ 5, 0.6, 0.05 }, { 5, 0.6, 0.1 }, { 5, 1.0, 0.05 }, { 5, 1.0, 0.1 }, { 6, 0.6, 0.05 }, { 6, 0.6, 0.1 },
			{ 6, 1.0, 0.05 }, { 6, 1.0, 0.1 }, { 7, 0.6, 0.05 }, { 7, 0.6, 0.1 }, { 7, 1.0, 0.05 }, { 7, 1.0, 0.1 },
			{ 8, 0.6, 0.05 }, { 8, 0.6, 0.1 }, { 8, 1.0, 0.05 }, { 8, 1.0, 0.1 }, { 9, 0.6, 0.05 }, { 9, 0.6, 0.1 },
			{ 9, 1.0, 0.05 }, { 9, 1.0, 0.1 } };

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ExportException {

		double[] arg0 = EXP[Integer.valueOf(args[0])];
		int ordinal = (int) arg0[0];
		double eps = arg0[1];
		double muPct = arg0[2];

		Assignment assgn = Assignment.values()[ordinal];

		System.out.println(assgn.name());

		String path = assgn.pathToPrograms;

		List<PDG> pdgs = ProgramUtils.readSerializedPDGs(path);
		
		if (pdgs.isEmpty())
			System.exit(-1);
		
		System.out.println("programs=" + pdgs.size());
		System.out.println();

		long alignStartTime = System.nanoTime();

		Aligner aligner = new DirectedMatchingAligner();
		DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> A = PairwiseAligner.getADirected(pdgs, 4, false,
				aligner);

		long alignTime = System.nanoTime() - alignStartTime;

		Map<CoreStmt, Integer[]> core2ItMu = LazyMap.lazyMap(new HashMap<>(), () -> new Integer[2]);
		Map<Vertex, Integer> vtx2coreness = new HashMap<>();

		Map<Integer, CoreStmt> allCores = new HashMap<>();
		Map<Integer, CoreStmt> itCores = new HashMap<>();

		DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> Ac = (DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge>) A
				.clone();

		System.out.println("eps=" + eps);

		int it = 1;

		do {

			int numSubs = Ac.vertexSet().stream().map(v -> v.getSubmission()).collect(Collectors.toSet()).size();

			System.out.println("num_subs=" + numSubs);

			Coreness<Vertex, DefaultWeightedEdge> coreness = new Coreness<>(Ac);
			Map<Vertex, Integer> vtx2score = coreness.getScores();

			long scanStartTime = System.nanoTime();

			int mu = Math.max(MIN_MU, (int) (numSubs * muPct));

			itCores = computeSCAN(Ac, mu, eps);

			long scanTime = System.nanoTime() - scanStartTime;

			System.out.println("\nalign_time=" + (alignTime * 1e-9) + " s.");
			System.out.println("scan_time=" + (scanTime * 1e-9) + " s.");

			for (CoreStmt cs : itCores.values()) {
				Ac.removeAllVertices(cs.getNodes());
			}

			if (allCores.isEmpty()) {
				allCores.putAll(itCores);
			} else {

				int allCoresSize = allCores.size();
				for (Entry<Integer, CoreStmt> e : itCores.entrySet()) {
					int newId = allCoresSize + e.getKey();
					e.getValue().setId(newId);
					allCores.put(newId, e.getValue());
				}

			}

			for (CoreStmt cs : itCores.values()) {
				core2ItMu.get(cs)[0] = it;
				core2ItMu.get(cs)[1] = mu;
				for (Vertex v : cs.getNodes()) {
					vtx2coreness.put(v, vtx2score.get(v));
				}
			}

			it++;
			System.out.println();

		} while (!itCores.isEmpty());

		CoreStmtsReport report = new CoreStmtsReport(pdgs, allCores.values(),
				"exp2-cs-" + assgn.name() + "-" + eps + "-" + muPct);

		System.out.println(report.toString());

		System.out.println();

		String filename = "exp2-cs-" + assgn.name() + "-" + eps + "-" + muPct + ".txt";

		CoreStmtSerializer.export(allCores.values(), core2ItMu, vtx2coreness, filename);

	}

	public static Map<Integer, CoreStmt> computeSCAN(DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> A,
			int mu, double eps) {

		System.out.println("mu=" + mu);
		System.out.println("eps=" + eps);

		System.out.println("nodes=" + A.vertexSet().size());
		System.out.println("edges=" + A.edgeSet().size());

		SCANExtractor featExtractor = new SCANExtractor(A, eps, mu);

		featExtractor.computeCoreStmts();

		Map<Integer, CoreStmt> coreStmts = featExtractor.getCoreStmts();

		System.out.println("core_stmts=" + coreStmts.size());

		return coreStmts;
	}

}
