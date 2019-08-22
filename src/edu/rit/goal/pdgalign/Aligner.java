package edu.rit.goal.pdgalign;

import java.util.Set;

import edu.rit.goal.coreminer.evaluation.Triple;
import edu.rit.goal.sourcedg.PDG;
import edu.rit.goal.sourcedg.Vertex;

public interface Aligner {
	
	Set<Triple<Vertex, Vertex, Double>> align(DirectedCostGraph g1, DirectedCostGraph g2);

	Set<Triple<Vertex, Vertex, Double>> align(PDG g1, PDG g2);

}
