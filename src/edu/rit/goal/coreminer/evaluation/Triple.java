package edu.rit.goal.coreminer.evaluation;

import java.util.Objects;

public class Triple<A, B, C> {
	public final A a;
	public final B b;
	public final C c;

	public Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof Triple<?, ?, ?>)) {
			return false;
		}

		Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
		return equals(a, other.a) && equals(b, other.b) && equals(c, other.c);
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, c);

	}

	private boolean equals(Object a, Object b) {
		if (a == null) {
			return b == null;
		}

		return a.equals(b);
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", a, b, c);
	}
}