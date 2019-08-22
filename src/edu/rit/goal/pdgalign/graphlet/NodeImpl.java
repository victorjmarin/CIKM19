package edu.rit.goal.pdgalign.graphlet;

import java.io.Serializable;
import java.util.HashSet;
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
 * A simple implementation of Node using a java.util.Set to store arcs to the
 * node's neighbors in an undirected graph.
 */
public class NodeImpl extends BaseGraphletSignature implements Node, Serializable
{

    /**
    * 
    */
    private static final long serialVersionUID = 3109738025336087094L;

    /**
     * Set of nodes that are adjacent to this node in an undirected graph.
     */
    private final Set<Node> neighbors = new HashSet<>();

    @Override
    public Set<Node> getNeighbors()
    {
	return neighbors;
    }

    @Override
    public void addNeighbor(final Node n)
    {
	neighbors.add(n);
	n.getNeighbors().add(this);
    }

    @Override
    public void removeEdge(final Node neighbor)
    {}

}