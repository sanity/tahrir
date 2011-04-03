package tahrir.io.net;

public interface TrSession {
	public int getSessionId();

	public SessionType getSessionType();

	enum SessionType {
		LOCAL, REMOTE
	}

	public @interface Priority {
		double value();
	}
}
