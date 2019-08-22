package edu.rit.goal.coreminer;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.type.Type;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.rit.goal.sourcedg.Vertex;

public class CoreStmt implements Serializable {

	private static final long serialVersionUID = 1426140915971082123L;

	private int id;
	private Map<Multiset<String>, Set<Vertex>> classes;
	private Set<Vertex> nodes;

	public CoreStmt(int id, Set<Vertex> nodes) {
		this.id = id;
		this.nodes = nodes;
		this.classes = classes(nodes);
	}

	private Map<Multiset<String>, Set<Vertex>> classes(Set<Vertex> nodes) {
		Map<Multiset<String>, Set<Vertex>> result = new HashMap<>();
		Multiset<String> astSignature = null;
		for (Vertex v : nodes) {
			astSignature = astSignature(v);
			Set<Vertex> cls = result.get(astSignature);
			if (cls == null) {
				cls = new HashSet<>();
				result.put(astSignature, cls);
			}
			cls.add(v);
		}

		return result;
	}

	public static Multiset<String> astSignature(Vertex n) {
		Multiset<String> result = HashMultiset.create();
		Node root = n.getAst();
		if (root == null)
			return result;
		Queue<Node> Q = new ArrayDeque<>();
		Q.add(root);
		while (!Q.isEmpty()) {
			Node c = Q.poll();
			if (c instanceof Type)
				continue;
			if (c instanceof BinaryExpr) {
				BinaryExpr expr = (BinaryExpr) c;
				result.add(expr.getOperator().asString());
			} else if (c instanceof UnaryExpr) {
				UnaryExpr expr = (UnaryExpr) c;
				result.add(expr.getOperator().asString());
			} else if (c instanceof LiteralExpr) {
				LiteralExpr expr = (LiteralExpr) c;
				result.add(expr.toString());
			} else if (c.getChildNodes().isEmpty())
				result.add(c.getClass().getSimpleName());

			if (c instanceof MethodDeclaration)
				continue;

			if (c instanceof ClassOrInterfaceDeclaration)
				continue;

			Q.addAll(c.getChildNodes());
		}
		return result;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Map<Multiset<String>, Set<Vertex>> getClasses() {
		return classes;
	}

	public Set<Vertex> getNodes() {
		return nodes;
	}

}
