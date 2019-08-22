package edu.rit.goal.coreminer;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;

import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.PDGBuilder;
import edu.rit.goal.sourcedg.PDGBuilderConfig;
import edu.rit.goal.sourcedg.Vertex;
import edu.rit.goal.sourcedg.VertexType;

public class ProgramUtils {

	public static Set<PDG> buildPDGs(String pathToPrograms) {

		File folder = new File(pathToPrograms);
		File[] files = folder.listFiles();
		Set<PDG> pdgs = new HashSet<>();

		for (File file : files) {
			if (!file.getName().endsWith(".java"))
				continue;
			pdgs.add(pdg(file.toPath()));
		}

		setVerticesPDG(pdgs);

		return pdgs;
	}

	public static List<PDG> buildPDGs(Collection<Path> programs) {
		List<PDG> pdgs = programs.parallelStream().map(p -> pdg(p)).collect(Collectors.toList());
		setVerticesPDG(pdgs);
		removeUnusedDefinitions(pdgs);
		return pdgs;
	}

	private static void removeUnusedDefinitions(Collection<PDG> programs) {
		int totalUnused = 0;
		for (PDG g : programs) {
			Set<Vertex> unused = g.removeUnusedDefinitions();
			totalUnused += unused.size();
		}
	}

	public static PDG pdg(Path path) {
		PDGBuilderConfig config = PDGBuilderConfig.create().keepLines();
		final PDGBuilder builder = new PDGBuilder(config, Level.OFF);
		builder.build(path);
		PDG pdg = builder.getPDG();
		// String[] splitted = path.toString().split("/");
		// String programPath = splitted[splitted.length - 1];
		// pdg.setPathToProgram(programPath);
		pdg.setPathToProgram(path.toAbsolutePath().toString());
		// pdg.collapseNodes(VertexType.ACTUAL_IN);
		pdg.collapseNodes(VertexType.ACTUAL_OUT);
		pdg.collapseNodes(VertexType.ARRAY_IDX);
		return pdg;
	}

	public static Map<PDG, Set<CoreStmt>> pdgToCoreStmtCluster(Map<Integer, CoreStmt> coreStmtsClusters) {
		Map<PDG, Set<CoreStmt>> result = new HashMap<>();
		for (Entry<Integer, CoreStmt> e : coreStmtsClusters.entrySet()) {
			Set<Vertex> V = e.getValue().getNodes();
			for (Vertex v : V) {
				PDG pdg = v.getPDG();
				Set<CoreStmt> clusters = result.get(pdg);
				if (clusters == null) {
					clusters = new HashSet<>();
					result.put(pdg, clusters);
				}
				clusters.add(e.getValue());
			}
		}
		return result;
	}

	public static Map<PDG, boolean[]> oneHotEncoding(Map<PDG, Set<CoreStmt>> pdgToCoreStmts) {
		Map<PDG, boolean[]> result = new HashMap<>();
		Set<Integer> allCharStmts = pdgToCoreStmts.values().stream().flatMap(v -> v.stream()).map(v -> v.getId())
				.collect(Collectors.toSet());
		int dim = allCharStmts.size();
		for (Entry<PDG, Set<CoreStmt>> e : pdgToCoreStmts.entrySet()) {
			boolean[] bs = new boolean[dim];
			for (CoreStmt cs : e.getValue())
				bs[cs.getId()] = true;
			result.put(e.getKey(), bs);
		}
		return result;
	}

	private static void setVerticesPDG(Collection<PDG> pdgs) {
		for (PDG pdg : pdgs) {
			for (Vertex v : pdg.vertexSet()) {
				v.setPDG(pdg);
				// String[] splitted = pdg.getPathToProgram().split("/");
				// String submission = splitted[splitted.length - 1].replace(".java", "");
				v.setSubmission(pdg.getPathToProgram());
			}
		}
	}

	public static List<Path> listPrograms(String dir) {
		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
			for (Path file : stream) {
				if (file.getFileName().toString().endsWith(".java")) {
					CompilationUnit cu = JavaParser.parse(file);
					if (cu.findAll(MethodDeclaration.class).size() > 1)
						continue;
					if (cu.findAll(FieldDeclaration.class).size() > 0)
						continue;
					if (cu.findAll(SwitchStmt.class).size() > 0)
						continue;
					if (cu.findAll(LabeledStmt.class).size() > 0)
						continue;
					if (cu.findAll(TryStmt.class).size() > 0)
						continue;
//					if (cu.findAll(ArrayAccessExpr.class).size() > 0)
//						continue;
//		    if (cu.findAll(ConditionalExpr.class).size() > 0)
//			continue;
					if (cu.findAll(MethodCallExpr.class, m -> m.getName().asString().equals("stream")).size() > 0)
						continue;
//		    if (cu.findAll(TryStmt.class).size() > 0)
//			continue;
					if (cu.findAll(LambdaExpr.class).size() > 0)
						continue;
					if (cu.findAll(ConstructorDeclaration.class).size() > 0)
						continue;
					result.add(file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<Path> loadChronFiltered(String submission) {
		List<Path> res = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(submission + "/details.csv"));
			for (String l : lines) {
				String[] columns = l.split(",");
				if (columns[3].equals("accepted")) {
					Path programPath = Paths.get(submission + "/submissions/correct/" + columns[0] + ".java");
					CompilationUnit cu = JavaParser.parse(programPath);
					if (cu.findAll(MethodDeclaration.class).size() > 1)
						continue;
					if (cu.findAll(SwitchStmt.class).size() > 0)
						continue;
					if (cu.findAll(LabeledStmt.class).size() > 0)
						continue;
//			    if (cu.findAll(ConditionalExpr.class).size() > 0)
//				continue;
					if (cu.findAll(MethodCallExpr.class, m -> m.getName().asString().equals("stream")).size() > 0)
						continue;
//			    if (cu.findAll(TryStmt.class).size() > 0)
//				continue;
					if (cu.findAll(LambdaExpr.class).size() > 0)
						continue;
					if (cu.findAll(ConstructorDeclaration.class).size() > 0)
						continue;
					res.add(0, programPath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

}
