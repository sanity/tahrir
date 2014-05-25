package tahrir.tools;

import java.util.Map.Entry;

public class Tuple2<A, B> {

	public final A a;
	public final B b;

	public static <A, B> Tuple2<A, B> of(final A a, final B b) {
		return new Tuple2<A, B>(a, b);
	}

	public Tuple2(final A a, final B b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tuple2))
			return false;
		final Tuple2 other = (Tuple2) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		return true;
	}

	public static <A, B> Tuple2<A, B> fromEntry(final Entry<A, B> entry) {
		return of(entry.getKey(), entry.getValue());
	}
}
