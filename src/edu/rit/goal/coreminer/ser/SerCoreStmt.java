package edu.rit.goal.coreminer.ser;

import java.util.Set;

public class SerCoreStmt {

	public int id;
	public Set<SerVertex> nodes;

	public SerCoreStmt(int id, Set<SerVertex> nodes) {
		this.id = id;
		this.nodes = nodes;
	}

	public String toString() {
		return String.valueOf(id);
	}

}
