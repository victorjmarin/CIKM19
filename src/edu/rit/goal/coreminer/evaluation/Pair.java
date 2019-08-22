package edu.rit.goal.coreminer.evaluation;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8930133405325206750L;

	public final A a;
	public final B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Pair<?, ?> pair = (Pair<?, ?>) o;

		if (a != null ? !a.equals(pair.a) : pair.a != null)
			return false;
		if (b != null ? !b.equals(pair.b) : pair.b != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = a != null ? a.hashCode() : 0;
		return 31 * result + (b != null ? b.hashCode() : 0);
	}

	@Override
	public String toString() {
		return String.format("<%s, %s>", a, b);
	}
}