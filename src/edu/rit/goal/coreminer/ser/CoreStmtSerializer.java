package edu.rit.goal.coreminer.ser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.LazyMap;

import edu.rit.goal.coreminer.CoreStmt;
import edu.rit.goal.sourcedg.Vertex;

public class CoreStmtSerializer {

	public static void export(Collection<CoreStmt> cores, String saveTo) {

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(saveTo));
			for (CoreStmt cs : cores) {
				for (Vertex v : cs.getNodes()) {
					String program = v.getPDG().getPathToProgram();
					String id = String.valueOf(v.getId());
					String str = v.toString();
					List<String> labels = new ArrayList<>(v.getSubtypes());
					labels.add(0, v.getType().name());
					String core = String.valueOf(cs.getId());
					String line = program + "\t" + id + "\t" + str + "\t" + labels.toString() + "\t" + core;
					writer.write(line + "\n");
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void export(Collection<CoreStmt> cores, Map<CoreStmt, Integer[]> core2ItMu,
			Map<Vertex, Integer> vtx2coreness, String saveTo) {

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(saveTo));
			for (CoreStmt cs : cores) {
				int it = core2ItMu.get(cs)[0];
				int mu = core2ItMu.get(cs)[1];
				for (Vertex v : cs.getNodes()) {
					String coreness = String.valueOf(vtx2coreness.get(v));
					String program = v.getPDG().getPathToProgram();
					String id = String.valueOf(v.getId());
					String str = v.toString();
					List<String> labels = new ArrayList<>(v.getSubtypes());
					labels.add(0, v.getType().name());
					String core = String.valueOf(cs.getId());
					String line = program + "\t" + id + "\t" + str + "\t" + labels.toString() + "\t" + core + "\t" + it
							+ "\t" + mu + "\t" + coreness;
					writer.write(line + "\n");
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Set<SerCoreStmt> importCores(String path) {
		Map<Integer, Set<SerVertex>> core2vtcs = LazyMap.lazyMap(new HashMap<>(), () -> new HashSet<>());
		Set<SerCoreStmt> result = new HashSet<>();

		try {
			List<String> lines = Files.readAllLines(Paths.get(path));
			for (String line : lines) {
				String[] cols = line.split("\t");
				String program = cols[0];
				int vtxId = Integer.valueOf(cols[1]);
				try {
					int coreId = Integer.valueOf(cols[4]);
					SerVertex v = new SerVertex(vtxId, program);
					core2vtcs.get(coreId).add(v);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Entry<Integer, Set<SerVertex>> e : core2vtcs.entrySet()) {
			result.add(new SerCoreStmt(e.getKey(), e.getValue()));
		}

		return result;
	}

	public static void exportEmbeddings(Collection<CoreStmt> cs, String saveTo) {
		try {
			Map<String, Set<Integer>> program2cores = LazyMap.lazyMap(new HashMap<>(), () -> new HashSet<>());

			for (CoreStmt core : cs) {
				for (Vertex v : core.getNodes()) {
					program2cores.get(v.getPDG().getPathToProgram()).add(core.getId());
				}
			}

			Map<String, boolean[]> program2embedding = new HashMap<>();

			int bipartiteEdges = 0;

			for (Entry<String, Set<Integer>> e : program2cores.entrySet()) {
				boolean[] embedding = new boolean[cs.size()];
				for (Integer idx : e.getValue()) {
					embedding[idx] = true;
					bipartiteEdges += 1;
				}
				program2embedding.put(e.getKey(), embedding);
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(saveTo));
			BufferedWriter pWriter = new BufferedWriter(new FileWriter("./programs.txt"));
			writer.write(program2cores.keySet().size() + " " + cs.size() + "\n");
			for (Entry<String, boolean[]> e : program2embedding.entrySet()) {
				Stream<Boolean> stream = IntStream.range(0, e.getValue().length).mapToObj(idx -> e.getValue()[idx]);
				String line = stream.map(b -> b ? "1" : "0").collect(Collectors.joining(" "));
				writer.write(line + "\n");
				pWriter.write(e.getKey() + "\n");
			}
			writer.close();
			pWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void exportForFeedProp(Collection<CoreStmt> cs, String saveTo) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(saveTo));
			for (CoreStmt core : cs) {
				for (Vertex v : core.getNodes()) {
					String program = v.getPDG().getPathToProgram();
					String id = String.valueOf(v.getId());
					String coreId = String.valueOf(core.getId());
					String nodesOfInterest = String.valueOf(v.getPDG().nodesOfInterest().size());
					String line = program + "," + id + "," + coreId + "," + nodesOfInterest;
					writer.write(line + "\n");
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
