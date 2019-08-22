package edu.rit.goal.coreminer.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.unisalzburg.dbresearch.apted.costmodel.StringUnitCostModel;
import at.unisalzburg.dbresearch.apted.distance.APTED;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import at.unisalzburg.dbresearch.apted.parser.BracketStringInputParser;
import edu.rit.goal.sourcedg.Vertex;

public class ASTDistance {

	private Map<Vertex, String> vtx2tree;
	private Map<String, Map<String, Integer>> memo;

	public ASTDistance() {
		vtx2tree = new HashMap<>();
		memo = new HashMap<>();
	}

	public int get(Vertex n1, Vertex n2) {

		String br1 = vtx2tree.get(n1);
		String br2 = vtx2tree.get(n2);

		if (br1 == null)
			br1 = compute(n1);

		if (br2 == null)
			br2 = compute(n2);

		if (br1 == null || br2 == null)
			return 0;

		Integer result = memo(br1, br2, true);

		if (result == null)
			result = compute(br1, br2);

		return result;
	}

	private int compute(String br1, String br2) {
		BracketStringInputParser parser = new BracketStringInputParser();
		at.unisalzburg.dbresearch.apted.node.Node<StringNodeData> t1 = parser.fromString(br1);
		at.unisalzburg.dbresearch.apted.node.Node<StringNodeData> t2 = parser.fromString(br2);
		APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
		int result = (int) apted.computeEditDistance(t1, t2);
		Map<String, Integer> m = memo.getOrDefault(br1, new HashMap<>());
		if (m.isEmpty())
			memo.put(br1, m);
		m.put(br2, result);
		return result;
	}

	private Integer memo(String br1, String br2, boolean checkPermutation) {
		Map<String, Integer> m = memo.getOrDefault(br1, new HashMap<>());
		Integer result = m.get(br2);

		if (result == null && checkPermutation)
			return memo(br2, br1, false);

		return result;
	}

	private String compute(Vertex n) {
		com.github.javaparser.ast.Node ast = n.getAst();
		if (ast == null) {
			System.err.println("Missing AST node.");
			System.err.println("vertex=" + n.toString());
			System.err.println("program=" + n.getPDG().getPathToProgram());
			return null;
		}
		String br = bracketNotation(ast, new HashSet<>());
		vtx2tree.put(n, br);
		return br;
	}

	private String bracketNotation(com.github.javaparser.ast.Node root, Set<com.github.javaparser.ast.Node> V) {
		String result = "";
		List<com.github.javaparser.ast.Node> neighs = root.getChildNodes();
		V.add(root);
		result = "{" + root.getClass().getSimpleName();
		for (com.github.javaparser.ast.Node c : neighs) {
			if (V.contains(c))
				continue;
			result += bracketNotation(c, V);
		}
		return result + "}";
	}

}
