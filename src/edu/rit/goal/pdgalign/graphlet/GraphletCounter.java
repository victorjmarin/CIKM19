package edu.rit.goal.pdgalign.graphlet;

import java.util.Set;

/**
 * Copyright 2010 Christopher W Whelan
 *
 * This file is part of GraphletCounter.
 *
 * GraphletCounter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GraphletCounter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GraphletCounter.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class does the actual work of computing the graphlet signature of a
 * Node. Please refer to the list of labeled graphlets 0-29 and orbits 0-73 from
 * Przulj (2007).
 */
public class GraphletCounter {

	public static final int NUM_ORBITS = 73;

	public static double ORBIT_WEIGHTS_SUM = 0d;

	/**
	 * Orbit weight factors (o_i) and weights (w_i) as described in Milenkovic and
	 * Przulj, Cancer Informatics 2008:6 257–273. These weight factors are the same
	 * as those found in GraphCrunch, http://bio-nets.doc.ic.ac.uk/graphcrunch2/
	 */
	public static final double orbitWeightFactors[] = { 1, 2, 2, 2, 3, 4, 3, 3, 4, 3, 4, 4, 4, 4, 3, 4, 6, 5, 4, 5, 6,
			6, 4, 4, 4, 5, 7, 4, 6, 6, 7, 4, 6, 6, 6, 5, 6, 7, 7, 5, 7, 6, 7, 6, 5, 5, 6, 8, 7, 6, 6, 8, 6, 9, 5, 6, 4,
			6, 6, 7, 8, 6, 6, 8, 7, 6, 7, 7, 8, 5, 6, 6, 4 };
	public static final double orbitWeights[] = new double[NUM_ORBITS];

	static {
		for (int i = 0; i < NUM_ORBITS; i++) {
			double w = 1 - (Math.log(orbitWeightFactors[i]) / Math.log(NUM_ORBITS));
			orbitWeights[i] = w;
			ORBIT_WEIGHTS_SUM += w;
		}
	}

	private int graphletSize;

	public GraphletCounter() {
		graphletSize = 5;
	}

	public GraphletCounter(int graphletSize) {
		this.graphletSize = graphletSize;
	}

	/**
	 * Computes the graphlet count for a given node, a. The algorithm works by
	 * examining all neighbors through the neighbor set of a, examining their
	 * neighbors, and so on. Could probably be sped up with a different
	 * implementation. See GraphletCounterTest for test cases for all automorphism
	 * orbits.
	 * 
	 * @param a
	 * @return an int array of length 73 which holds graphlet counts for all 73
	 *         automorphism orbits
	 */
	public int[] getGraphletSignature(Node a) {
		int[] results = initializeResults();

		Set<Node> neighbors = a.getNeighbors();

		for (Node n : neighbors) {
			if (n.equals(a))
				continue;

			incrementResultsForG0(a, n, results);

			if (graphletSize > 2)
				for (Node nn : n.getNeighbors()) {
					if (nn.equals(a))
						continue;

					if (nn.getNeighbors().contains(a)) {
						incrementResultsForPathThroughG2(a, n, nn, results);
					} else {
						incrementResultsForPathThroughG1(a, n, nn, results);
					}

					if (graphletSize > 3) {
						boolean aNeighborOfNN = nn.getNeighbors().contains(a);
						for (Node nnn : nn.getNeighbors()) {
							if (nnn.equals(nn))
								continue;
							if (nnn.equals(n))
								continue;
							if (nnn.equals(a))
								continue;

							boolean aNeighborOfNNN = nnn.getNeighbors().contains(a);
							boolean n2nnn = nnn.getNeighbors().contains(n);
							if (!aNeighborOfNN && aNeighborOfNNN && !n2nnn) {
								incrementResultsForPathThroughG5(a, n, nn, nnn, results);

								if (graphletSize == 5)
									for (Node a1n : a.getNeighbors()) {
										if (a1n.equals(n))
											continue;
										if (a1n.equals(nn))
											continue;
										if (a1n.equals(nnn))
											continue;
										if (a1n.getNeighbors().contains(nnn))
											continue;
										if (a1n.getNeighbors().contains(n))
											continue;
										if (a1n.getNeighbors().contains(nn)) {
											incrementResultsForPathThroughG20(a, n, nn, nnn, a1n, results);
										}
									}
							} else if (aNeighborOfNN && !aNeighborOfNNN && !n2nnn) {
								incrementResultsForPathThroughG6FromO10(a, results, n, nn, nnn);
							} else if (!aNeighborOfNN && !aNeighborOfNNN && n2nnn) {
								incrementResultsForPathThroughG6FromO9(a, results, n, nn, nnn);

							} else if (!aNeighborOfNN && !aNeighborOfNNN && !n2nnn) {
								incrementResultsForPathThroughG3(a, results, n, nn, nnn);
							}

							boolean nNeighborOfNNN = n.getNeighbors().contains(nnn);
							if (!aNeighborOfNN && aNeighborOfNNN && nNeighborOfNNN) {
								incrementResultsForPathThroughG7FromO12(a, results, n, nn, nnn);
							}

							if (aNeighborOfNN && aNeighborOfNNN && !nNeighborOfNNN) {
								incrementResultsForPathThroughG7FromO13(a, results, n, nn, nnn);
							}

							if (aNeighborOfNN && aNeighborOfNNN && nNeighborOfNNN) {
								incrementResultsForPathThroughG8(a, n, nn, nnn, results);
							}

							if (graphletSize == 5)
								countFiveNodeGraphletsStartingWithPath(a, results, n, nn, nnn);
						}

						if (!aNeighborOfNN) {
							for (Node nn1 : n.getNeighbors()) {
								if (nn1.equals(nn))
									continue;
								if (nn1.equals(a))
									continue;
								if (nn1.getNeighbors().contains(nn))
									continue;
								if (nn1.getNeighbors().contains(a))
									continue;
								incrementResultsForG4FromO6(a, results, n, nn, nn1);
							}
						}
					}
				}
		}

		// claw forms
		for (Node n1 : a.getNeighbors()) {
			if (n1.equals(a))
				continue;
			for (Node n2 : a.getNeighbors()) {
				if (n2.equals(a))
					continue;
				if (n2.equals(n1))
					continue;

				if (!n2.getNeighbors().contains(n1)) {
					incrementResultsForG1FromO2(a, n1, n2, results);

					if (graphletSize > 2)
						for (Node n1n : n1.getNeighbors()) {
							if (n1n.equals(n1))
								continue;
							if (n1n.equals(n2))
								continue;

							if (!n1n.getNeighbors().contains(n2) && !n1n.getNeighbors().contains(a)) {
								incrementResultsForG3FromO5(results);
								if (graphletSize == 5)
									countFiveNodeGraphletsFromG3O5(a, results, n1, n2, n1n);
							}
						}
				}

				if (graphletSize > 2)
					for (Node n3 : a.getNeighbors()) {
						if (n3.equals(a))
							continue;
						if (n3.equals(n1))
							continue;
						if (n3.equals(n2))
							continue;

						if (!n1.getNeighbors().contains(n2) && !n1.getNeighbors().contains(n3)
								&& !n2.getNeighbors().contains(n3)) {
							incrementResultsForG4O7(results);
							countFiveNodeGraphletsFromG4O7(a, results, n1, n2, n3);
						}

						if (n1.getNeighbors().contains(n2) && !n1.getNeighbors().contains(n3)
								&& !n2.getNeighbors().contains(n3) && !n3.getNeighbors().contains(n2)
								&& !n3.getNeighbors().contains(n1)) // extra checks in case directed graphs
						{
							incrementResultsForG6O11(results);
							if (graphletSize == 5)
								countFiveNodeGraphletsFromG6O11(a, results, n1, n2, n3);

						}
					}
			}
		}

		adjustForOvercounting(results);

		return results;
	}

	/**
	 * The main algorithm overcounts graphlet counts for certain orbits by double or
	 * triple counting links. This method returns a set of graphlet counts that have
	 * had those overcounts divided out.
	 * 
	 * @param results
	 */
	protected void adjustForOvercounting(int[] results) {
		results[1] = results[1];
		results[2] = results[2] / 2;
		results[3] = results[3] / 2;
		results[4] = results[4];
		results[5] = results[5];
		results[6] = results[6] / 2;
		results[7] = results[7] / 6;
		results[8] = results[8] / 2;
		results[9] = results[9] / 2;
		results[10] = results[10];
		results[11] = results[11] / 2;
		results[12] = results[12] / 2;
		results[13] = results[13] / 2;
		results[14] = results[14] / 6;
		results[15] = results[15];
		results[16] = results[16];
		results[17] = results[17] / 2;
		results[18] = results[18] / 2;
		results[19] = results[19];
		results[20] = results[20] / 2;
		results[21] = results[21] / 2;
		results[22] = results[22] / 6;
		results[23] = results[23] / 24;
		results[24] = results[24];
		results[25] = results[25] / 2;
		results[26] = results[26];
		results[27] = results[27] / 2;
		results[28] = results[28] / 2;
		results[29] = results[29];
		results[30] = results[30] / 2;
		results[31] = results[31] / 2;
		results[32] = results[32] / 2;
		results[33] = results[33] / 4;
		results[34] = results[34] / 2;
		results[35] = results[35] / 2;
		results[36] = results[36] / 2;
		results[37] = results[37];
		results[38] = results[38] / 2;
		results[39] = results[39] / 2;
		results[40] = results[40];
		results[41] = results[41] / 2;
		results[42] = results[42] / 2;
		results[43] = results[43] / 2;
		results[44] = results[44] / 8;
		results[45] = results[45] / 2;
		results[46] = results[46] / 2;
		results[47] = results[47] / 2;
		results[48] = results[48];
		results[49] = results[49] / 4;
		results[50] = results[50] / 6;
		results[51] = results[51] / 3;
		results[52] = results[52] / 4;
		results[53] = results[53] / 2;
		results[54] = results[54] / 4;
		results[55] = results[55] / 6;
		results[56] = results[56] / 6;
		results[57] = results[57] / 2;
		results[58] = results[58] / 6;
		results[59] = results[59];
		results[60] = results[60];
		results[61] = results[61] / 2;
		results[62] = results[62] / 4;
		results[63] = results[63] / 2;
		results[64] = results[64] / 2;
		results[65] = results[65] / 4;
		results[66] = results[66] / 2;
		results[67] = results[67] / 2;
		results[68] = results[68] / 2;
		results[69] = results[69] / 8;
		results[70] = results[70] / 6;
		results[71] = results[71] / 4;
		results[72] = results[72] / (4 * 3 * 2);
	}

	/**
	 * This method handles the special cases around computing orbits 26, 30, 33, 44,
	 * given that we've identified an instance of graphlet G6.
	 * 
	 * @param a       A node identified as residing in orbit 11 in graphlet G6
	 * @param results updated count array
	 * @param n1      one of the neighbors of a in graphlet G6
	 * @param n2      one of the neighbors of a in graphlet G6
	 * @param n3      one of the neighbors of a in graphlet G6
	 */
	private void countFiveNodeGraphletsFromG6O11(Node a, int[] results, Node n1, Node n2, Node n3) {
		results[26] += countNeighborsOfSourceNotConnectedToGraphlet(n1, a, n2, n3);
		results[30] += countNeighborsOfSourceNotConnectedToGraphlet(n3, a, n1, n2);
		results[33] += countNeighborsOfSourceNotConnectedToGraphlet(a, n1, n2, n3);
		for (Node n4 : a.getNeighbors()) {
			if (n4.equals(n1))
				continue;
			if (n4.equals(n2))
				continue;
			if (n4.equals(n3))
				continue;
			if (n4.getNeighbors().contains(n1))
				continue;
			if (n4.getNeighbors().contains(n2))
				continue;
			if (n4.getNeighbors().contains(n3)) {
				results[44]++;
			}
		}
	}

	/**
	 * This method handles the special cases around computing orbits 21, 23, given
	 * that we've identified an instance of graphlet G4.
	 * 
	 * @param a       The node occupying orbit 7 in graphlet G4
	 * @param results Count array to be updated
	 * @param n1      Neighbor of a in graphlet G4.
	 * @param n2      Neighbor of a in graphlet G4.
	 * @param n3      Neighbor of a in graphlet G4.
	 */
	private void countFiveNodeGraphletsFromG4O7(Node a, int[] results, Node n1, Node n2, Node n3) {
		if (graphletSize == 5) {
			results[21] += countNeighborsOfSourceNotConnectedToGraphlet(n1, a, n2, n3);
			results[23] += countNeighborsOfSourceNotConnectedToGraphlet(a, n1, n2, n3);
		}
	}

	/**
	 * This method handles the special cases around computing orbit 17 given that
	 * we've identified an instance of graphlet G3.
	 * 
	 * @param a       The node occupying orbit 5 in graphlet G3
	 * @param results Count array to be updated
	 * @param n1      Neighbor of a in graphlet G3.
	 * @param n2      Neighbor of a in graphlet G3.
	 * @param n1n     Neighbor of a in graphlet G3.
	 */
	private void countFiveNodeGraphletsFromG3O5(Node a, int[] results, Node n1, Node n2, Node n1n) {
		for (Node n2n : n2.getNeighbors()) {
			if (n2n.equals(n1))
				continue;
			if (n2n.equals(a))
				continue;
			if (n2n.equals(n1n))
				continue;
			if (n2n.getNeighbors().contains(a))
				continue;
			if (n2n.getNeighbors().contains(n1))
				continue;
			if (n2n.getNeighbors().contains(n1n))
				continue;
			results[17]++;
		}
	}

	private void incrementResultsForG6O11(int[] results) {
		results[11]++;
	}

	private void incrementResultsForG4O7(int[] results) {
		results[7]++;
	}

	private void incrementResultsForG3FromO5(int[] results) {
		results[5]++;
	}

	protected void incrementResultsForG1FromO2(Node a, Node n1, Node n2, int[] results) {
		results[2]++;
	}

	private void incrementResultsForG4FromO6(Node a, int[] results, Node n, Node nn, Node nn1) {
		results[6]++;

		if (graphletSize == 5) {
			results[20] += countNeighborsOfSourceNotConnectedToGraphlet(a, n, nn, nn1);
			results[22] += countNeighborsOfSourceNotConnectedToGraphlet(n, a, nn, nn1);
		}
	}

	/**
	 * Given a four node path starting with a and ending with nnn, increments counts
	 * for orbits from graphlets that have a four node path.
	 * 
	 * @param a       Beginning of the path
	 * @param results Graphlet count array to be updated
	 * @param n       Neighbor of a
	 * @param nn      Neighbor of neigbor of a
	 * @param nnn     Neighbor of neighbor of neighbor of a
	 */
	private void countFiveNodeGraphletsStartingWithPath(Node a, int[] results, Node n, Node nn, Node nnn) {
		for (Node nnnn : nnn.getNeighbors()) {
			if (nnnn.equals(nnn))
				continue;
			if (nnnn.equals(nn))
				continue;
			if (nnnn.equals(n))
				continue;
			if (nnnn.equals(a))
				continue;

			boolean aNeighborOfNN = a.getNeighbors().contains(nn);
			boolean aNeighborOfNNN = a.getNeighbors().contains(nnn);
			boolean aNeighborOfNNNN = a.getNeighbors().contains(nnnn);
			boolean nNeighborOfNNN = n.getNeighbors().contains(nnn);
			boolean nNeighborOfNNNN = n.getNeighbors().contains(nnnn);
			boolean nnNeighborOfNNNN = nn.getNeighbors().contains(nnnn);
			if (!aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[15]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[24]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[27]++;
			}

			if (aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[29]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[34]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[35]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[37]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[39]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[40]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[45]++;
			}

			if (aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[46]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[48]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[49]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[51]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[51]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && aNeighborOfNNNN && nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[51]++;
			}

			if (aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[52]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[52]++;
			}

			if (aNeighborOfNN && !aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[53]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[53]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[54]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[56]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[57]++;
			}

			if (!aNeighborOfNN && !aNeighborOfNNN && aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[59]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[61]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && nNeighborOfNNN && !nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[60]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[62]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[63]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[64]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[65]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[66]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && !nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[67]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && !aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[68]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& !nnNeighborOfNNNN) {
				results[69]++;
			}

			if (!aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[70]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && !nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[71]++;
			}

			if (aNeighborOfNN && aNeighborOfNNN && aNeighborOfNNNN && nNeighborOfNNN && nNeighborOfNNNN
					&& nnNeighborOfNNNN) {
				results[72]++;
			}

		}
	}

	private void incrementResultsForPathThroughG8(Node a, Node n, Node nn, Node nnn, int[] results) {
		results[14]++;
		if (graphletSize == 5)
			results[58] += countNeighborsOfSourceNotConnectedToGraphlet(a, n, nn, nnn);
	}

	private void incrementResultsForPathThroughG7FromO13(Node a, int[] results, Node n, Node nn, Node nnn) {
		results[13]++;

		if (graphletSize == 5) {
			results[42] += countNeighborsOfSourceNotConnectedToGraphlet(a, n, nn, nnn);
			results[41] += countNeighborsOfSourceNotConnectedToGraphlet(nn, nnn, a, n);
			for (Node a1n : a.getNeighbors()) {
				if (a1n.equals(n))
					continue;
				if (a1n.equals(nn))
					continue;
				if (a1n.equals(nnn))
					continue;
				if (a1n.getNeighbors().contains(nnn))
					continue;
				if (a1n.getNeighbors().contains(n))
					continue;
				if (a1n.getNeighbors().contains(nn)) {
					results[55]++;
				}
			}
		}
	}

	private void incrementResultsForPathThroughG7FromO12(Node a, int[] results, Node n, Node nn, Node nnn) {
		results[12]++;
		if (graphletSize == 5)
			results[47] += countNeighborsOfSourceNotConnectedToGraphlet(a, n, nn, nnn);
	}

	private void incrementResultsForPathThroughG3(Node a, int[] results, Node n, Node nn, Node nnn) {
		results[4]++;

		if (graphletSize == 5) {
			results[16] += countNeighborsOfSourceNotConnectedToGraphlet(a, n, nn, nnn);

			for (Node nn2 : nn.getNeighbors()) {
				if (nn2.equals(a))
					continue;
				if (nn2.equals(n))
					continue;
				if (nn2.equals(nnn))
					continue;
				if (nn2.getNeighbors().contains(a))
					continue;
				if (nn2.getNeighbors().contains(n))
					continue;
				if (nn2.getNeighbors().contains(nnn))
					continue;
				results[18]++;
			}

			for (Node n2 : n.getNeighbors()) {
				if (n2.equals(a))
					continue;
				if (n2.equals(nn))
					continue;
				if (n2.equals(nnn))
					continue;
				if (n2.getNeighbors().contains(a))
					continue;
				if (n2.getNeighbors().contains(nnn))
					continue;
				if (n2.getNeighbors().contains(nn))
					continue;
				results[19]++;
			}
		}
	}

	private void incrementResultsForPathThroughG6FromO9(Node a, int[] results, Node n, Node nn, Node nnn) {
		results[9]++;
		if (graphletSize == 5) {
			results[28] += countNeighborsOfSourceNotConnectedToGraphlet(a, n, nn, nnn);
			results[31] += countNeighborsOfSourceNotConnectedToGraphlet(n, a, nn, nnn);
			for (Node a1n : a.getNeighbors()) {
				if (a1n.equals(n))
					continue;
				if (a1n.equals(nn))
					continue;
				if (a1n.equals(nnn))
					continue;
				if (a1n.getNeighbors().contains(nnn))
					continue;
				if (a1n.getNeighbors().contains(nn))
					continue;
				if (a1n.getNeighbors().contains(n)) {
					results[43]++;
				}
			}
		}
	}

	private void incrementResultsForPathThroughG6FromO10(Node a, int[] results, Node n, Node nn, Node nnn) {
		results[10]++;
		if (graphletSize == 5) {
			results[25] += countNeighborsOfSourceNotConnectedToGraphlet(n, a, nn, nnn);
			results[32] += countNeighborsOfSourceNotConnectedToGraphlet(nn, nnn, n, a);
		}
	}

	private void incrementResultsForPathThroughG20(Node a, Node n, Node nn, Node nnn, Node a1n, int[] results) {
		results[50]++;
	}

	private void incrementResultsForPathThroughG5(Node a, Node n, Node nn, Node nnn, int[] results) {
		incrementResultsForPathThroughG8(results);
		if (graphletSize == 5) {
			results[36] += countNeighborsOfSourceNotConnectedToGraphlet(nn, nnn, a, n);
			results[38] += countNeighborsOfSourceNotConnectedToGraphlet(a, n, nn, nnn);
		}
	}

	private void incrementResultsForPathThroughG8(int[] results) {
		results[8]++;
	}

	protected void incrementResultsForPathThroughG1(Node a, Node n, Node nn, int[] results) {
		results[1]++;
	}

	protected void incrementResultsForPathThroughG2(Node a, Node n, Node nn, int[] results) {
		results[3]++;
	}

	protected void incrementResultsForG0(Node a, Node n, int[] results) {
		results[0]++;
	}

	protected int[] initializeResults() {
		return new int[NUM_ORBITS];
	}

	/**
	 * Utility method to count the neighbors of Node source that are not connected
	 * to any of the given four neighbor nodes (including source)
	 * 
	 * @param source
	 * @param n      A neigbor of source.
	 * @param nn     A neigbor of n.
	 * @param nnn    A neigbor of nn.
	 * @return
	 */
	private int countNeighborsOfSourceNotConnectedToGraphlet(Node source, Node n, Node nn, Node nnn) {
		int neighborsOfSourceNotConnectedToGraphlet = 0;
		for (Node n1 : source.getNeighbors()) {
			if (n1.equals(n))
				continue;
			if (n1.equals(nn))
				continue;
			if (n1.equals(nnn))
				continue;
			if (n1.getNeighbors().contains(n))
				continue;
			if (n1.getNeighbors().contains(nn))
				continue;
			if (n1.getNeighbors().contains(nnn))
				continue;
			neighborsOfSourceNotConnectedToGraphlet++;
		}
		return neighborsOfSourceNotConnectedToGraphlet;
	}

}