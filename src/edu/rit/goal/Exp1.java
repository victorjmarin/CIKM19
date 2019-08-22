package edu.rit.goal;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LazyMap;
import org.jgrapht.alg.scoring.Coreness;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;
import org.jgrapht.io.GraphMLExporter.AttributeCategory;
import org.jgrapht.io.IntegerComponentNameProvider;

import edu.rit.goal.coreminer.CoreStmt;
import edu.rit.goal.coreminer.ProgramUtils;
import edu.rit.goal.coreminer.SCANExtractor;
import edu.rit.goal.coreminer.evaluation.CoreStmtsReport;
import edu.rit.goal.coreminer.evaluation.InducedSubgraph;
import edu.rit.goal.coreminer.ser.CoreStmtSerializer;
import edu.rit.goal.pdgalign.Aligner;
import edu.rit.goal.pdgalign.DirectedMatchingAligner;
import edu.rit.goal.pdgalign.PairwiseAligner;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public class Exp1 {

	private static final int MU = 5;

	private static final double[][] EXP = { { 0, 0.85 }, { 0, 0.9 }, { 0, 0.95 }, { 1, 0.85 }, { 1, 0.9 }, { 1, 0.95 },
			{ 2, 0.85 }, { 2, 0.9 }, { 2, 0.95 }, { 3, 0.85 }, { 3, 0.9 }, { 3, 0.95 }, { 4, 0.85 }, { 4, 0.9 },
			{ 4, 0.95 }, { 5, 0.85 }, { 5, 0.9 }, { 5, 0.95 }, { 6, 0.85 }, { 6, 0.9 }, { 6, 0.95 }, { 7, 0.85 },
			{ 7, 0.9 }, { 7, 0.95 }, { 8, 0.85 }, { 8, 0.9 }, { 8, 0.95 }, { 9, 0.85 }, { 9, 0.9 }, { 9, 0.95 } };

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ExportException {

		double[] arg0 = EXP[Integer.valueOf(args[0])];
		int ordinal = (int) arg0[0];
		double eps = arg0[1];

		Assignment assgn = Assignment.values()[ordinal];

		System.out.println(assgn.name());

		String path = assgn.pathToPrograms;

		List<Path> P = ProgramUtils.listPrograms(path);

		List<PDG> pdgs = ProgramUtils.buildPDGs(P);

		long alignStartTime = System.nanoTime();

		Aligner aligner = new DirectedMatchingAligner();
		DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> A = PairwiseAligner.getADirected(pdgs, 4, false,
				aligner);

		long alignTime = System.nanoTime() - alignStartTime;

		System.out.println("programs=" + P.size());
		System.out.println();

		Map<CoreStmt, Integer[]> core2ItMu = LazyMap.lazyMap(new HashMap<>(), () -> new Integer[2]);
		Map<Vertex, Integer> vtx2coreness = new HashMap<>();

		Map<Integer, CoreStmt> allCores = new HashMap<>();
		Map<Integer, CoreStmt> itCores = new HashMap<>();

		DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge> Ac = (DefaultUndirectedWeightedGraph<Vertex, DefaultWeightedEdge>) A
				.clone();

		System.out.println("eps=" + eps);

		int numSubs = Ac.vertexSet().stream().map(v -> v.getSubmission()).collect(Collectors.toSet()).size();

		System.out.println("num_subs=" + numSubs);

		Coreness<Vertex, DefaultWeightedEdge> coreness = new Coreness<>(Ac);
		Map<Vertex, Integer> vtx2score = coreness.getScores();

		long scanStartTime = System.nanoTime();

		itCores = computeSCAN(Ac, MU, eps);

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
			core2ItMu.get(cs)[0] = 1;
			core2ItMu.get(cs)[1] = MU;
			for (Vertex v : cs.getNodes()) {
				vtx2coreness.put(v, vtx2score.get(v));
			}
		}

		System.out.println();

		CoreStmtsReport report = new CoreStmtsReport(pdgs, allCores.values(), "exp1-cs-" + assgn.name() + "-" + eps);

		System.out.println(report.toString());

		System.out.println();

		String filename = "exp1-cs-" + assgn.name() + "-" + eps + ".txt";

		CoreStmtSerializer.export(allCores.values(), core2ItMu, vtx2coreness, filename);

		Set<DefaultWeightedEdge> visibleEdges = new HashSet<>();

		for (CoreStmt cs : itCores.values()) {
			InducedSubgraph<Vertex, DefaultWeightedEdge> I = new InducedSubgraph<>(A, cs.getNodes());
			visibleEdges.addAll(I.edgeSet());
		}

		Map<Vertex, Integer> vtx2cluster = new HashMap<>();

		for (CoreStmt cs : itCores.values()) {
			for (Vertex v : cs.getNodes()) {
				vtx2cluster.put(v, cs.getId());
			}
		}

		ComponentAttributeProvider<Vertex> vertexAttributeProvider = new ComponentAttributeProvider<Vertex>() {

			@Override
			public Map<String, Attribute> getComponentAttributes(Vertex v) {
				Map<String, Attribute> res = new HashMap<>();
				res.put("core", DefaultAttribute.createAttribute(vtx2cluster.getOrDefault(v, -1)));
				return res;
			}
		};

		ComponentAttributeProvider<DefaultWeightedEdge> edgeAttributeProvider = new ComponentAttributeProvider<DefaultWeightedEdge>() {

			@Override
			public Map<String, Attribute> getComponentAttributes(DefaultWeightedEdge e) {
				Map<String, Attribute> res = new HashMap<>();
				boolean hidden = !visibleEdges.contains(e);
				res.put("hidden", DefaultAttribute.createAttribute(hidden));
				return res;
			}
		};

		GraphMLExporter<Vertex, DefaultWeightedEdge> graphml = new GraphMLExporter<>(
				new IntegerComponentNameProvider<>(), null, vertexAttributeProvider,
				new IntegerComponentNameProvider<>(), null, edgeAttributeProvider);
		graphml.registerAttribute("core", AttributeCategory.NODE, AttributeType.INT);
		graphml.registerAttribute("hidden", AttributeCategory.EDGE, AttributeType.BOOLEAN);
		graphml.exportGraph(A, new File("exp1-" + assgn.name() + "-" + eps + "-cores.graphml"));

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
