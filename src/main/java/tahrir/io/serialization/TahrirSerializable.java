package tahrir.io.serialization;


public interface TahrirSerializable {
	public Iterable<byte[]> serialize();

	public static class TahrirSerializableException extends Exception {

		public TahrirSerializableException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public TahrirSerializableException(final String arg0, final Throwable arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
		}

		public TahrirSerializableException(final String arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		public TahrirSerializableException(final Throwable arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -2092348068330758560L;

	}
}
