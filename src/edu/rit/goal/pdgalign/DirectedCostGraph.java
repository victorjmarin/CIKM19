package edu.rit.goal.pdgalign;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultDirectedGraph;

import edu.rit.goal.pdgalign.graphlet.DirectedGraphletCounter;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public class DirectedCostGraph implements Serializable {
	private static final long serialVersionUID = -8784162760888242186L;

	private PDG pdg;
	private Map<Vertex, int[]> signatures;
	private int maxDegree;
	private Map<List<Integer>, List<Vertex>> symmetricNodes;
	private Set<Vertex> indiscernibleNodes;
	private boolean useIncomingOnly;

	public DirectedCostGraph(PDG pdg, int graphletSize, boolean useIncomingOnly) {
		this.pdg = pdg;
		this.useIncomingOnly = useIncomingOnly;
		indiscernibleNodes = new HashSet<>();
		loadMaxDegree();
		loadSignatures(graphletSize);
		loadSymmetricNodes();
	}

	private void loadSymmetricNodes() {
		symmetricNodes = new HashMap<>();
		for (Vertex v : pdg.nodesOfInterest()) {
			int[] signature = signatures.get(v);
			List<Integer> l = Arrays.stream(signature).boxed().collect(Collectors.toList());
			List<Vertex> sym = symmetricNodes.get(l);
			if (sym == null) {
				sym = new LinkedList<>();
				symmetricNodes.put(l, sym);
			}
			sym.add(v);
		}
		for (List<Vertex> l : symmetricNodes.values()) {
			if (l.size() < 2)
				continue;
			Map<Set<String>, Set<Vertex>> m = new HashMap<>();
			for (Vertex v : l) {
				Set<String> labels = new HashSet<>(v.getSubtypes());
				labels.add(v.getType().toString());
				Set<Vertex> V = m.get(labels);
				if (V == null) {
					V = new HashSet<>();
					m.put(labels, V);
				}
				V.add(v);
			}
			for (Set<Vertex> lv : m.values()) {
				if (lv.size() > 1)
					indiscernibleNodes.addAll(lv);
			}
		}
	}

	public Map<List<Integer>, List<Vertex>> getSymmetricNodes() {
		return symmetricNodes;
	}

	public PDG getPDG() {
		return pdg;
	}

	public Map<Vertex, int[]> getSignatures() {
		return signatures;
	}

	public int getMaxDegree() {
		return maxDegree;
	}

	public int degreeOf(Vertex v) {
		return pdg.degreeOf(v);
	}

	public Set<Vertex> vertexSet() {
		return pdg.vertexSet();
	}

	public Set<Vertex> nodesOfInterest() {
		return pdg.nodesOfInterest();
	}

	public Set<Vertex> getIndiscernibleNodes() {
		return indiscernibleNodes;
	}

	private void loadMaxDegree() {
		maxDegree = pdg.vertexSet().stream().map(v -> pdg.degreeOf(v)).max(Integer::compare).get();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadSignatures(int graphletSize) {
		signatures = new DirectedGraphletCounter<>(graphletSize, useIncomingOnly)
				.computeTopologySignatures((DefaultDirectedGraph) pdg.asDefaultDirectedGraph());
	}

}
