package tahrir.tools;

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
}
