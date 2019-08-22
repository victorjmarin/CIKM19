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
 * This interface abstracts details about a node in a network, allowing
 * different implementations (e.g. a minimal implementation vs. the Cytoscape
 * implementation of a node. All that GraphletCounter cares about is the set of
 * uniquely identified nodes that are adjacent to the current node in the
 * network.
 */
public interface Node extends HasGraphletSignature {

	/**
	 * Returns the set of Node objects that are adjacent to this node in the graph
	 * 
	 * @return
	 */
	public Set<Node> getNeighbors();

	/**
	 * Adds a neighbor to the node
	 * 
	 * @param n
	 */
	public void addNeighbor(Node n);

	/**
	 * Removes neighbor from the set of neighbors for this node.
	 * 
	 * @param neighbor
	 */
	public void removeEdge(Node neighbor);
}