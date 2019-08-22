package edu.rit.goal.pdgalign.graphlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Splitter;

public class DirectedGraphletCounter<V, E> {

	private static final String GRAPH_START = "t #";
	private static final String ORBITS_START = "Orbits: ";

	private static Map<Integer, List<DefaultDirectedGraph<String, DefaultEdge>>> graphlets = new HashMap<>();
	private static Map<Integer, Orbit> orbits = new HashMap<>();
	private static Map<DefaultDirectedGraph<String, DefaultEdge>, List<Orbit>> graphletOrbits = new HashMap<>();
	private static Map<Integer, Integer> orbitWeights = new HashMap<>();
	private static boolean computeOrbitWeights = false;

	// This is the size of the topology array.
	public static int numberOfOrbits = 0;

	public static final double[] weights = new double[] { 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 3.0, 2.0, 2.0, 2.0, 3.0, 2.0,
			3.0, 3.0, 3.0, 4.0, 3.0, 5.0, 3.0, 4.0, 5.0, 3.0, 3.0, 4.0, 5.0, 4.0, 3.0, 3.0, 5.0, 5.0, 4.0, 3.0, 4.0,
			5.0, 3.0, 4.0, 4.0, 5.0, 4.0, 5.0, 4.0, 6.0, 4.0, 5.0, 5.0, 6.0, 3.0, 5.0, 4.0, 5.0, 3.0, 4.0, 5.0, 5.0,
			4.0, 4.0, 4.0, 6.0, 4.0, 8.0, 5.0, 4.0, 4.0, 4.0, 6.0, 4.0, 5.0, 6.0, 5.0, 6.0, 4.0, 4.0, 6.0, 4.0, 5.0,
			4.0, 5.0, 6.0, 6.0, 6.0, 5.0, 6.0, 4.0, 6.0, 4.0, 4.0, 6.0, 6.0, 6.0, 5.0, 5.0, 6.0, 4.0, 4.0, 6.0, 6.0,
			6.0, 6.0, 5.0, 10.0, 6.0, 5.0, 6.0, 4.0, 4.0, 4.0, 5.0, 3.0, 5.0, 5.0, 3.0, 3.0, 6.0, 5.0, 6.0, 6.0, 5.0,
			3.0, 6.0, 4.0, 4.0, 6.0, 4.0, 7.0, 6.0, 6.0, 8.0, 4.0, 6.0, 7.0, 4.0, 4.0, 6.0, 7.0, 6.0, 6.0, 6.0, 8.0,
			6.0, 4.0, 6.0, 6.0, 4.0, 7.0, 8.0, 7.0, 5.0, 9.0, 9.0, 7.0, 6.0, 8.0, 9.0, 8.0, 5.0, 5.0, 9.0, 5.0, 9.0,
			5.0, 9.0, 6.0, 4.0, 7.0, 8.0, 5.0, 4.0, 6.0, 6.0, 6.0, 8.0, 6.0, 7.0, 4.0, 6.0, 6.0, 7.0, 6.0, 8.0, 6.0,
			9.0, 7.0, 7.0, 7.0, 9.0, 6.0, 7.0, 6.0, 8.0, 8.0, 5.0, 9.0, 9.0, 9.0, 7.0, 8.0, 11.0, 7.0, 11.0, 9.0, 11.0,
			5.0, 9.0, 8.0, 9.0, 6.0, 6.0, 7.0, 8.0, 7.0, 6.0, 6.0, 9.0, 8.0, 8.0, 13.0, 7.0, 6.0, 6.0, 7.0, 6.0, 7.0,
			9.0, 9.0, 8.0, 11.0, 9.0, 9.0, 9.0, 9.0, 9.0, 7.0, 9.0, 11.0, 5.0, 10.0, 6.0, 9.0, 7.0, 9.0, 10.0, 9.0,
			11.0, 6.0, 10.0, 8.0, 9.0, 11.0, 5.0, 9.0, 6.0, 7.0, 8.0, 10.0, 8.0, 11.0, 5.0, 8.0, 6.0, 9.0, 5.0, 8.0,
			9.0, 11.0, 8.0, 8.0, 11.0, 6.0, 8.0, 10.0, 9.0, 11.0, 7.0, 7.0, 8.0, 11.0, 9.0, 6.0, 7.0, 7.0, 18.0, 10.0,
			8.0, 9.0, 9.0, 5.0, 10.0, 6.0, 9.0, 7.0, 9.0, 8.0, 8.0, 11.0, 5.0, 7.0, 7.0, 8.0, 10.0, 8.0, 6.0, 8.0, 10.0,
			10.0, 7.0, 8.0, 8.0, 9.0, 7.0, 6.0, 9.0, 10.0, 8.0, 5.0, 10.0, 9.0, 8.0, 6.0, 8.0, 10.0, 6.0, 11.0, 10.0,
			10.0, 5.0, 9.0, 8.0, 8.0, 5.0, 5.0, 8.0, 9.0, 9.0, 6.0, 6.0, 8.0, 10.0, 10.0, 5.0, 6.0, 7.0, 9.0, 10.0, 9.0,
			5.0, 7.0, 10.0, 6.0, 5.0, 6.0, 10.0, 9.0, 6.0, 6.0, 11.0, 5.0, 9.0, 9.0, 8.0, 10.0, 6.0, 6.0, 8.0, 7.0, 8.0,
			7.0, 8.0, 7.0, 9.0, 9.0, 9.0, 10.0, 11.0, 9.0, 9.0, 8.0, 11.0, 8.0, 9.0, 10.0, 7.0, 9.0, 9.0, 10.0, 11.0,
			9.0, 7.0, 10.0, 11.0, 9.0, 7.0, 10.0, 11.0, 6.0, 10.0, 8.0, 10.0, 5.0, 10.0, 7.0, 9.0, 7.0, 9.0, 10.0, 10.0,
			11.0, 7.0, 9.0, 10.0, 10.0, 11.0, 7.0, 10.0, 8.0, 10.0, 12.0, 5.0, 10.0, 8.0, 10.0, 9.0, 5.0, 9.0, 7.0, 7.0,
			6.0, 9.0, 8.0, 9.0, 10.0, 10.0, 8.0, 11.0, 7.0, 10.0, 8.0, 10.0, 11.0, 6.0, 8.0, 7.0, 10.0, 5.0, 8.0, 8.0,
			9.0, 5.0, 8.0, 8.0, 9.0, 6.0, 8.0, 7.0, 10.0, 12.0, 8.0, 10.0, 11.0, 11.0, 8.0, 8.0, 11.0, 5.0, 8.0, 10.0,
			10.0, 9.0, 7.0, 8.0, 10.0, 10.0, 12.0, 7.0, 7.0, 8.0, 10.0, 11.0, 7.0, 7.0, 8.0, 10.0, 11.0, 11.0, 8.0, 9.0,
			9.0, 7.0, 7.0, 12.0, 9.0, 10.0, 11.0, 11.0, 9.0, 10.0, 11.0, 7.0, 8.0, 16.0, 9.0, 8.0, 10.0, 7.0, 5.0, 10.0,
			7.0, 9.0, 11.0, 10.0, 10.0, 7.0, 9.0, 8.0, 8.0, 11.0, 7.0, 8.0, 16.0, 6.0, 7.0, 7.0, 9.0, 9.0, 7.0, 7.0,
			6.0, 6.0, 9.0, 9.0, 8.0, 10.0, 8.0, 6.0, 10.0, 8.0, 11.0, 8.0, 10.0, 7.0, 11.0, 12.0, 9.0, 12.0, 9.0, 12.0,
			12.0, 6.0, 8.0, 10.0, 9.0, 6.0, 6.0, 12.0, 9.0, 10.0, 10.0, 11.0, 8.0, 10.0, 8.0, 16.0, 7.0, 9.0, 9.0, 10.0,
			8.0, 6.0, 6.0, 6.0, 7.0, 6.0, 9.0, 9.0, 9.0, 10.0, 9.0, 9.0, 7.0, 10.0, 8.0, 6.0, 10.0, 10.0, 8.0, 11.0,
			9.0, 10.0, 8.0, 11.0, 7.0, 10.0, 8.0, 11.0, 11.0, 12.0, 11.0, 9.0, 12.0, 9.0, 12.0, 12.0, 9.0, 12.0, 9.0,
			11.0, 11.0, 6.0, 8.0, 10.0, 10.0, 9.0, 7.0, 7.0, 9.0, 6.0, 6.0, 12.0, 9.0, 10.0, 10.0, 12.0, 9.0, 10.0,
			10.0, 11.0, 8.0, 10.0, 9.0, 11.0, 8.0, 10.0, 7.0, 11.0, 11.0, 8.0, 11.0, 12.0, 9.0, 9.0, 10.0, 9.0, 9.0,
			9.0, 10.0, 7.0, 6.0, 6.0, 7.0, 5.0, 6.0, 6.0, 8.0, 6.0, 7.0, 4.0, 5.0, 7.0, 7.0, 4.0, 6.0, 7.0, 4.0, 8.0,
			6.0, 6.0, 4.0, 4.0, 8.0, 7.0, 7.0, 7.0, 9.0, 7.0, 4.0, 5.0, 6.0, 4.0, 8.0, 7.0, 6.0, 4.0, 5.0, 5.0, 7.0,
			10.0, 7.0, 8.0, 9.0, 6.0, 10.0, 5.0, 9.0, 7.0, 7.0, 10.0, 4.0, 7.0, 7.0, 6.0, 8.0, 7.0, 6.0, 7.0, 8.0, 8.0,
			11.0, 10.0, 5.0, 8.0, 14.0, 7.0, 6.0, 7.0, 6.0, 9.0, 7.0, 7.0, 5.0, 9.0, 6.0, 10.0, 5.0, 8.0, 7.0, 7.0,
			10.0, 8.0, 6.0, 7.0, 6.0, 4.0, 7.0, 6.0, 7.0, 8.0, 5.0, 9.0, 6.0, 6.0, 10.0, 5.0, 9.0, 13.0, 4.0, 8.0, 6.0,
			7.0, 5.0, 7.0, 5.0, 8.0, 9.0, 6.0, 10.0, 8.0, 9.0, 7.0, 7.0, 10.0, 6.0, 7.0, 7.0, 6.0, 8.0, 7.0, 8.0, 9.0,
			8.0, 8.0, 9.0, 9.0, 12.0, 6.0, 6.0, 9.0, 8.0, 10.0, 9.0, 9.0, 9.0, 7.0, 7.0, 9.0, 8.0, 10.0, 6.0, 6.0, 5.0,
			7.0, 11.0, 10.0, 7.0, 9.0, 10.0, 9.0, 10.0, 10.0, 8.0, 10.0, 7.0, 9.0, 10.0, 11.0, 10.0, 12.0, 9.0, 11.0,
			11.0, 10.0, 12.0, 9.0, 11.0, 10.0, 12.0, 11.0, 9.0, 10.0, 10.0, 10.0, 11.0, 8.0, 12.0, 10.0, 9.0, 9.0, 11.0,
			10.0, 12.0, 12.0, 8.0, 13.0, 8.0, 11.0, 9.0, 10.0, 8.0, 12.0, 11.0, 11.0, 9.0, 12.0, 11.0, 11.0, 9.0, 9.0,
			11.0, 8.0, 9.0, 10.0, 12.0, 12.0, 9.0, 8.0, 10.0, 9.0, 9.0, 9.0, 10.0, 11.0, 11.0, 9.0, 14.0, 10.0, 11.0,
			9.0, 9.0, 9.0, 11.0, 12.0, 11.0, 12.0, 9.0, 11.0, 10.0, 9.0, 12.0, 10.0, 10.0, 11.0, 9.0, 11.0, 12.0, 11.0,
			11.0, 10.0, 12.0, 12.0, 10.0, 10.0, 10.0, 11.0, 11.0, 13.0, 10.0, 10.0, 10.0, 15.0, 11.0, 12.0, 12.0, 10.0,
			11.0, 9.0, 10.0, 12.0, 9.0, 23.0, 11.0, 8.0, 8.0, 11.0, 11.0, 10.0, 9.0, 8.0, 11.0, 12.0, 11.0, 9.0, 16.0,
			10.0, 9.0, 10.0, 11.0, 10.0, 13.0, 8.0, 9.0, 11.0, 11.0, 9.0, 16.0, 13.0, 9.0, 8.0, 9.0, 11.0, 9.0, 8.0,
			12.0, 10.0, 9.0, 11.0, 11.0, 7.0, 10.0, 11.0, 9.0, 11.0, 9.0, 12.0, 12.0, 12.0, 9.0, 12.0, 9.0, 10.0, 9.0,
			10.0, 12.0, 12.0, 11.0, 8.0, 10.0, 12.0, 9.0, 11.0, 9.0, 11.0, 12.0, 11.0, 12.0, 8.0, 11.0, 12.0, 9.0, 11.0,
			9.0, 10.0, 11.0, 11.0, 11.0, 9.0, 10.0, 12.0, 12.0, 11.0, 9.0, 11.0, 10.0, 11.0, 12.0, 9.0, 11.0, 10.0,
			12.0, 11.0, 8.0, 10.0, 12.0, 12.0, 6.0, 9.0, 10.0, 12.0, 11.0, 12.0, 9.0, 11.0, 12.0, 12.0, 12.0, 9.0, 11.0,
			12.0, 11.0, 12.0, 9.0, 10.0, 11.0, 7.0, 9.0, 10.0, 12.0, 11.0, 11.0, 10.0, 11.0, 10.0, 11.0, 9.0, 11.0,
			10.0, 12.0, 12.0, 9.0, 8.0, 11.0, 12.0, 11.0, 8.0, 8.0, 11.0, 11.0, 11.0, 9.0, 9.0, 12.0, 12.0, 12.0, 8.0,
			9.0, 12.0, 11.0, 11.0, 11.0, 9.0, 11.0, 11.0, 9.0, 9.0, 12.0, 12.0, 11.0, 11.0, 9.0, 9.0, 11.0, 9.0, 9.0,
			9.0, 11.0, 11.0, 13.0, 12.0, 12.0, 10.0, 10.0, 11.0, 12.0, 10.0, 12.0, 15.0, 12.0, 11.0, 11.0, 11.0, 12.0,
			10.0, 7.0, 10.0, 11.0, 12.0, 12.0, 11.0, 11.0, 11.0, 12.0, 12.0, 12.0, 10.0, 10.0, 12.0, 12.0, 11.0, 11.0,
			10.0, 12.0, 12.0, 11.0, 10.0, 11.0, 12.0, 10.0, 11.0, 14.0, 11.0, 11.0, 11.0, 10.0, 10.0, 10.0, 10.0, 11.0,
			11.0, 10.0, 19.0, 11.0, 9.0, 11.0, 12.0, 12.0, 9.0, 12.0, 18.0, 11.0, 9.0, 12.0, 12.0, 12.0, 9.0, 9.0, 12.0,
			12.0, 6.0, 11.0, 9.0, 12.0, 10.0, 12.0, 13.0, 9.0, 11.0, 11.0, 11.0, 8.0, 9.0, 10.0, 11.0, 11.0, 8.0, 9.0,
			7.0, 13.0, 10.0, 10.0, 7.0, 11.0, 10.0, 10.0, 12.0, 12.0, 13.0, 11.0, 11.0, 11.0, 11.0, 11.0, 12.0, 12.0,
			10.0, 14.0, 8.0, 9.0, 11.0, 10.0, 8.0, 9.0, 12.0, 11.0, 14.0, 9.0, 11.0, 12.0, 9.0, 12.0, 18.0, 11.0, 9.0,
			12.0, 10.0, 11.0, 10.0, 9.0, 12.0, 12.0, 11.0, 11.0, 14.0, 10.0, 12.0, 10.0, 14.0, 11.0, 11.0, 9.0, 9.0,
			9.0, 12.0, 10.0, 12.0, 9.0, 9.0, 11.0, 12.0, 17.0, 12.0, 9.0, 11.0, 7.0, 8.0, 10.0, 10.0, 8.0, 8.0, 10.0,
			8.0, 12.0, 6.0, 7.0, 10.0, 10.0, 12.0, 8.0, 9.0, 8.0, 9.0, 6.0, 18.0, 10.0, 7.0, 9.0, 10.0, 10.0, 12.0, 7.0,
			9.0, 9.0, 9.0, 12.0, 6.0, 9.0, 10.0, 10.0, 11.0, 9.0, 9.0, 10.0, 10.0, 7.0, 9.0, 10.0, 10.0, 12.0, 10.0,
			9.0, 8.0, 12.0, 6.0, 9.0, 8.0, 10.0, 11.0, 8.0, 6.0, 9.0, 8.0, 10.0, 8.0, 10.0, 12.0, 7.0, 8.0, 10.0, 10.0,
			12.0, 5.0, 6.0, 10.0, 10.0, 9.0, 5.0, 6.0, 10.0, 10.0, 9.0, 7.0, 8.0, 10.0, 8.0, 12.0, 7.0, 8.0, 10.0, 10.0,
			12.0, 5.0, 8.0, 9.0, 8.0, 6.0, 17.0, 11.0, 7.0, 9.0, 10.0, 10.0, 12.0, 7.0, 12.0, 9.0, 9.0, 6.0, 9.0, 10.0,
			10.0, 10.0, 6.0, 9.0, 10.0, 8.0, 11.0, 13.0, 9.0, 10.0, 12.0, 7.0, 12.0, 8.0, 8.0, 11.0, 9.0, 8.0, 10.0,
			5.0, 9.0, 8.0, 9.0, 7.0, 8.0, 8.0, 10.0, 12.0, 7.0, 8.0, 14.0, 10.0, 6.0, 10.0, 8.0, 6.0, 9.0, 8.0, 6.0,
			8.0, 9.0, 6.0, 10.0, 9.0, 10.0, 10.0, 12.0, 8.0, 9.0, 7.0, 10.0, 6.0, 8.0, 10.0, 10.0, 10.0, 7.0, 10.0,
			10.0, 7.0, 12.0, 6.0, 10.0, 10.0, 8.0, 10.0, 4.0, 8.0, 9.0, 6.0, 7.0, 4.0, 10.0, 6.0, 7.0, 9.0, 10.0, 9.0,
			12.0, 7.0, 9.0, 10.0, 9.0, 12.0, 4.0, 10.0, 7.0, 4.0, 6.0, 9.0, 8.0, 7.0, 6.0, 7.0, 9.0, 6.0, 10.0, 7.0,
			7.0, 16.0, 6.0, 6.0, 9.0, 7.0, 10.0, 11.0, 10.0, 10.0, 10.0, 7.0, 7.0, 16.0, 12.0, 10.0, 8.0, 10.0, 8.0,
			9.0, 6.0, 6.0, 4.0, 10.0, 7.0, 7.0, 9.0, 10.0, 9.0, 12.0, 7.0, 9.0, 10.0, 9.0, 12.0, 4.0, 10.0, 6.0, 7.0,
			9.0, 9.0, 7.0, 10.0, 12.0, 11.0, 12.0, 10.0, 11.0, 8.0, 11.0, 9.0, 7.0, 11.0, 11.0, 9.0, 11.0, 11.0, 11.0,
			11.0, 9.0, 12.0, 11.0, 8.0, 12.0, 8.0, 12.0, 12.0, 10.0, 10.0, 8.0, 11.0, 11.0, 7.0, 9.0, 9.0, 11.0, 12.0,
			12.0, 11.0, 9.0, 12.0, 11.0, 12.0, 12.0, 9.0, 12.0, 8.0, 12.0, 12.0, 9.0, 11.0, 8.0, 11.0, 10.0, 8.0, 9.0,
			9.0, 12.0, 12.0, 9.0, 11.0, 11.0, 12.0, 12.0, 9.0, 11.0, 12.0, 12.0, 10.0, 6.0, 9.0, 7.0, 9.0, 10.0, 9.0,
			9.0, 11.0, 10.0, 9.0, 9.0, 9.0, 11.0, 7.0, 10.0, 11.0, 9.0, 12.0, 11.0, 12.0, 11.0, 9.0, 11.0, 8.0, 11.0,
			11.0, 9.0, 11.0, 12.0, 10.0, 11.0, 9.0, 12.0, 12.0, 12.0, 12.0, 9.0, 9.0, 12.0, 12.0, 12.0, 9.0, 9.0, 11.0,
			11.0, 14.0, 12.0, 11.0, 11.0, 11.0, 8.0, 19.0, 12.0, 12.0, 9.0, 9.0, 11.0, 11.0, 7.0, 7.0, 15.0, 12.0, 12.0,
			11.0, 16.0, 11.0, 12.0, 11.0, 14.0, 9.0, 12.0, 11.0, 13.0, 9.0, 11.0, 8.0, 11.0, 12.0, 12.0, 12.0, 10.0,
			11.0, 11.0, 12.0, 11.0, 12.0, 12.0, 12.0, 11.0, 11.0, 12.0, 12.0, 11.0, 12.0, 11.0, 12.0, 9.0, 11.0, 11.0,
			9.0, 9.0, 9.0, 12.0, 12.0, 10.0, 10.0, 11.0, 11.0, 9.0, 10.0, 12.0, 11.0, 12.0, 9.0, 12.0, 12.0, 12.0, 11.0,
			11.0, 12.0, 12.0, 12.0, 11.0, 11.0, 12.0, 12.0, 11.0, 12.0, 11.0, 11.0, 11.0, 11.0, 12.0, 12.0, 12.0, 8.0,
			11.0, 10.0, 19.0, 11.0, 12.0, 11.0, 12.0, 11.0, 11.0, 9.0, 17.0, 9.0, 11.0, 9.0, 10.0, 7.0, 6.0, 7.0, 9.0,
			10.0, 9.0, 9.0, 8.0, 19.0, 9.0, 8.0, 11.0, 10.0, 11.0, 8.0, 7.0, 7.0, 11.0, 11.0, 9.0, 11.0, 11.0, 12.0,
			12.0, 9.0, 10.0, 18.0, 8.0, 12.0, 12.0, 10.0, 10.0, 8.0, 11.0, 11.0, 9.0, 7.0, 9.0, 11.0, 12.0, 12.0, 12.0,
			9.0, 12.0, 11.0, 12.0, 11.0, 9.0, 8.0, 18.0, 9.0, 11.0, 8.0, 11.0, 8.0, 12.0, 9.0, 12.0, 12.0, 14.0, 11.0,
			11.0, 11.0, 9.0, 18.0, 8.0, 9.0, 7.0, 7.0, 9.0, 6.0, 10.0, 9.0, 8.0, 9.0, 12.0, 13.0, 11.0, 12.0, 9.0, 12.0,
			13.0, 9.0, 12.0, 8.0, 10.0, 9.0, 12.0, 12.0, 8.0, 12.0, 9.0, 9.0, 10.0, 12.0, 8.0, 8.0, 13.0, 13.0, 12.0,
			12.0, 9.0, 12.0, 10.0, 12.0, 11.0, 11.0, 12.0, 10.0, 12.0, 8.0, 8.0, 9.0, 9.0, 10.0, 9.0, 11.0, 10.0, 11.0,
			12.0, 13.0, 12.0, 12.0, 24.0, 10.0, 9.0, 10.0, 10.0, 12.0, 12.0, 11.0, 12.0, 10.0, 12.0, 12.0, 14.0, 12.0,
			10.0, 10.0, 12.0, 13.0, 13.0, 11.0, 11.0, 12.0, 13.0, 13.0, 10.0, 10.0, 12.0, 12.0, 12.0, 12.0, 12.0, 16.0,
			12.0, 12.0, 12.0, 16.0, 17.0, 12.0, 11.0, 13.0, 12.0, 12.0, 10.0, 12.0, 8.0, 8.0, 12.0, 11.0, 10.0, 19.0,
			11.0, 10.0, 12.0, 12.0, 10.0, 9.0, 9.0, 10.0, 10.0, 11.0, 9.0, 8.0, 8.0, 14.0, 10.0, 12.0, 12.0, 9.0, 10.0,
			12.0, 9.0, 12.0, 8.0, 9.0, 10.0, 12.0, 12.0, 12.0, 12.0, 9.0, 8.0, 12.0, 8.0, 8.0, 8.0, 13.0, 13.0, 12.0,
			12.0, 9.0, 12.0, 19.0, 9.0, 13.0, 12.0, 12.0, 9.0, 6.0, 10.0, 8.0, 9.0, 9.0, 12.0, 10.0, 7.0, 8.0, 12.0,
			8.0, 10.0, 10.0, 13.0, 13.0, 12.0, 12.0, 8.0, 13.0, 13.0, 8.0, 11.0, 10.0, 13.0, 12.0, 12.0, 7.0, 10.0,
			13.0, 13.0, 12.0, 12.0, 10.0, 12.0, 10.0, 12.0, 11.0, 10.0, 12.0, 10.0, 12.0, 11.0, 9.0, 12.0, 10.0, 10.0,
			8.0, 12.0, 8.0, 10.0, 10.0, 13.0, 12.0, 12.0, 12.0, 10.0, 13.0, 13.0, 12.0, 12.0, 10.0, 12.0, 10.0, 12.0,
			11.0, 10.0, 12.0, 10.0, 12.0, 11.0, 8.0, 10.0, 6.0, 9.0, 12.0, 10.0, 10.0, 8.0, 10.0, 9.0, 10.0, 10.0, 12.0,
			10.0, 12.0, 12.0, 8.0, 8.0, 8.0, 9.0, 8.0, 10.0, 10.0, 10.0, 12.0, 10.0, 10.0, 10.0, 12.0, 9.0, 10.0, 12.0,
			13.0, 13.0, 12.0, 11.0, 13.0, 10.0, 18.0, 12.0, 13.0, 13.0, 13.0, 11.0, 12.0, 13.0, 12.0, 13.0, 11.0, 13.0,
			12.0, 10.0, 13.0, 12.0, 10.0, 13.0, 18.0, 10.0, 12.0, 10.0, 10.0, 10.0, 12.0, 9.0, 7.0, 13.0, 13.0, 13.0,
			13.0, 11.0, 13.0, 13.0, 12.0, 13.0, 11.0, 12.0, 12.0, 10.0, 13.0, 12.0, 12.0, 12.0, 10.0, 13.0, 7.0, 10.0,
			10.0, 8.0, 12.0, 10.0, 9.0, 10.0, 10.0, 8.0, 6.0, 13.0, 5.0, 12.0, 13.0, 13.0, 12.0, 12.0, 12.0, 12.0, 12.0,
			12.0, 12.0, 13.0, 13.0, 9.0, 12.0, 13.0, 13.0, 12.0, 12.0, 13.0, 5.0, 6.0, 8.0, 9.0, 6.0, 6.0, 10.0, 8.0,
			10.0, 11.0, 11.0, 9.0, 9.0, 12.0, 11.0, 8.0, 12.0, 9.0, 12.0, 11.0, 11.0, 12.0, 8.0, 10.0, 9.0, 7.0, 9.0,
			8.0, 10.0, 10.0, 12.0, 11.0, 9.0, 12.0, 11.0, 12.0, 12.0, 9.0, 12.0, 11.0, 11.0, 10.0, 8.0, 10.0, 6.0, 10.0,
			11.0, 6.0, 6.0, 9.0, 11.0, 9.0, 6.0, 6.0, 12.0, 10.0, 10.0, 10.0, 8.0, 12.0, 10.0, 7.0, 10.0, 12.0, 10.0,
			9.0, 9.0, 10.0, 9.0, 7.0, 6.0, 12.0, 13.0, 10.0, 12.0, 10.0, 13.0, 13.0, 11.0, 11.0, 12.0, 12.0, 12.0, 11.0,
			10.0, 11.0, 13.0, 12.0, 7.0, 11.0, 12.0, 12.0, 12.0, 11.0, 12.0, 11.0, 13.0, 12.0, 11.0, 11.0, 11.0, 13.0,
			13.0, 11.0, 11.0, 11.0, 12.0, 13.0, 11.0, 11.0, 8.0, 12.0, 13.0, 11.0, 11.0, 11.0, 13.0, 13.0, 9.0, 12.0,
			9.0, 12.0, 8.0, 19.0, 12.0, 12.0, 7.0, 11.0, 7.0, 10.0, 10.0, 11.0, 12.0, 9.0, 12.0, 12.0, 11.0, 11.0, 12.0,
			12.0, 12.0, 12.0, 12.0, 11.0, 10.0, 10.0, 12.0, 11.0, 9.0, 10.0, 8.0, 11.0, 12.0, 10.0, 9.0, 10.0, 18.0,
			12.0, 9.0, 12.0, 12.0, 11.0, 10.0, 8.0, 11.0, 10.0, 6.0, 10.0, 8.0, 10.0, 10.0, 12.0, 9.0, 9.0, 10.0, 12.0,
			10.0, 10.0, 7.0, 10.0, 6.0, 7.0, 9.0, 12.0, 10.0, 19.0, 12.0, 12.0, 10.0, 11.0, 11.0, 13.0, 13.0, 12.0,
			11.0, 11.0, 13.0, 12.0, 12.0, 11.0, 7.0, 10.0, 8.0, 9.0, 11.0, 11.0, 12.0, 9.0, 12.0, 11.0, 11.0, 9.0, 19.0,
			8.0, 10.0, 8.0, 9.0, 7.0, 9.0, 6.0, 6.0, 9.0, 8.0, 6.0, 10.0, 4.0, 7.0, 8.0, 7.0, 4.0, 5.0, 7.0, 9.0, 5.0,
			10.0, 11.0, 10.0, 9.0, 5.0, 5.0, 11.0, 7.0, 9.0, 11.0, 9.0, 7.0, 9.0, 10.0, 11.0, 5.0, 10.0, 10.0, 10.0,
			10.0, 10.0, 11.0, 7.0, 5.0, 9.0, 9.0, 10.0, 10.0, 9.0, 8.0, 7.0, 11.0, 7.0, 10.0, 11.0, 11.0, 10.0, 11.0,
			11.0 };

	public static double weightSum;

	static {
		// Graphlet sizes
		for (int i = 2; i <= 5; i++) {
			// Read all directed graphlets with orbits.
			try (Scanner sc = new Scanner(new File("rsc/gd" + i + ".txt"))) {
				List<DefaultDirectedGraph<String, DefaultEdge>> allIGraphs = new ArrayList<>();
				graphlets.put(i, allIGraphs);

				DefaultDirectedGraph<String, DefaultEdge> g = null;

				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (line.startsWith(GRAPH_START))
						// Init graph.
						g = new DefaultDirectedGraph<>(DefaultEdge.class);
					else if (line.startsWith(ORBITS_START)) {
						line = line.replace(ORBITS_START, "");

						// Store graph.
						allIGraphs.add(g);

						// Store orbits.
						Iterator<String> it = Splitter.on("--").split(line).iterator();
						while (it.hasNext()) {
							String str = it.next();
							List<String> nodes = Splitter.on(", ").splitToList(str.substring(1, str.length() - 1));

							Orbit o = new Orbit();
							o.graph = g;
							o.nodes = nodes;
							o.orbitId = numberOfOrbits;

							orbits.put(numberOfOrbits, o);
							orbitWeights.put(numberOfOrbits, 1);
							numberOfOrbits++;

							if (!graphletOrbits.containsKey(g))
								graphletOrbits.put(g, new ArrayList<>());
							graphletOrbits.get(g).add(o);
						}
					} else {
						// Add nodes (if they do not exist) and directed edge.
						String[] edge = line.split(" ");
						if (!g.containsVertex(edge[0]))
							g.addVertex(edge[0]);
						if (!g.containsVertex(edge[1]))
							g.addVertex(edge[1]);
						g.addEdge(edge[0], edge[1]);
					}
				}
			} catch (FileNotFoundException oops) {
				oops.printStackTrace();
				System.exit(-1);
			}
		}

		// Compute orbit weights based on smaller graphlets included in each
		// graphlet.
		if (computeOrbitWeights) {
			for (int i = 0; i < numberOfOrbits; i++) {
				Orbit oi = orbits.get(i);

				for (int j = 0; j < numberOfOrbits; j++)
					if (i != j) {
						Orbit oj = orbits.get(j);

						GraphQLInducedSubgraphIso<String, String, DefaultEdge, DefaultEdge> subgraph = new GraphQLInducedSubgraphIso<>(
								oi.graph, oj.graph);
						Set<Map<String, String>> solutions = subgraph.compute();

						// Let's find whether there is a solution of graphlet i
						// inside graphlet j such that orbit j maps to orbit i.
						boolean solutionFound = false;
						for (Iterator<Map<String, String>> solIt = solutions.iterator(); !solutionFound
								&& solIt.hasNext();) {
							Map<String, String> sol = solIt.next();
							for (Iterator<String> nodeIt = oi.nodes.iterator(); !solutionFound && nodeIt.hasNext();) {
								String node = nodeIt.next();
								solutionFound = oj.nodes.contains(sol.get(node));
							}
						}

						if (solutionFound)
							orbitWeights.put(j, orbitWeights.get(j) + 1);
					}
			}

			for (int i = 0; i < numberOfOrbits; i++)
				System.out.print(orbitWeights.get(i) + ".0, ");
			System.out.println();
		}

		// Update weights.
		weightSum = 0.0;
		double den = Math.log(numberOfOrbits);
		for (int i = 0; i < numberOfOrbits; i++) {
			weights[i] = 1.0 - (Math.log(weights[i]) / den);
			weightSum += weights[i];
		}
	}

	private static class Orbit {
		DefaultDirectedGraph<String, DefaultEdge> graph;
		List<String> nodes;
		int orbitId;
	}

	private int graphletSize;
	private boolean useIncomingOnly;

	public DirectedGraphletCounter(int graphletSize, boolean useIncomingOnly) {
		super();
		this.graphletSize = graphletSize;
		this.useIncomingOnly = useIncomingOnly;
	}

	public Map<V, int[]> computeTopologySignatures(DefaultDirectedGraph<V, E> graph) {
		Map<V, int[]> ret = new HashMap<>();
		// Initialize signatures.
		for (V v : graph.vertexSet()) {
			int[] orbitArr = new int[numberOfOrbits];
			ret.put(v, orbitArr);
		}

		// Graphlet sizes
		for (int i = 2; i <= 5; i++)
			if (i <= graphletSize)
				// For each graphlet.
				for (DefaultDirectedGraph<String, DefaultEdge> graphlet : graphlets.get(i)) {
					List<Orbit> orbitsToUse = new ArrayList<>(graphletOrbits.get(graphlet));
					for (int j = 0; useIncomingOnly && j < orbitsToUse.size(); j++) {
						Orbit o = orbitsToUse.get(j);
						if (graphlet.outDegreeOf(o.nodes.get(0)) > 0) {
							orbitsToUse.remove(j);
							j--;
						}
					}

					if (!orbitsToUse.isEmpty()) {
						GraphQLInducedSubgraphIso<String, V, DefaultEdge, E> subgraph = new GraphQLInducedSubgraphIso<>(
								graphlet, graph);
						Set<Map<String, V>> solutions = subgraph.compute();

						// For each orbit belonging to this graphlet.
						for (Orbit o : orbitsToUse) {
							// Get one single node (all of them are equivalent).
							String orbit = o.nodes.get(0);

							// For each solution.
							for (Map<String, V> sol : solutions)
								ret.get(sol.get(orbit))[o.orbitId]++;
						}
					}
				}

		return ret;
	}

	public static double getDistance(int[] sig1, int[] sig2) {
		double num = 0;
		for (int i = 0; i < numberOfOrbits; i++)
			// Only if one of them is greater than zero to avoid divided by zero error.
			if (sig1[i] > 0 || sig2[i] > 0)
				num += weights[i] * Math.abs(Math.log1p(sig1[i]) - Math.log1p(sig2[i]))
						/ Math.log1p(Math.max(sig1[i], sig2[i]));
		return num / weightSum;
	}

}