package edu.rit.goal.coreminer.ser;

import java.util.Objects;

public class SerVertex {

	public int id;
	public String program;

	public SerVertex(int id, String program) {
		this.id = id;
		this.program = program;
	}

	public String toString() {
		return id + ", " + program;
	}

	public int hashCode() {
		return Objects.hash(id, program);
	}

	public boolean equals(Object o) {
		if (!(o instanceof SerVertex))
			return false;
		SerVertex s = (SerVertex) o;
		return id == s.id && program.equals(s.program);
	}

}
