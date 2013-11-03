package tahrir.io.net;

public interface TrSession {
	public void registerFailureListener(Runnable listener);

    void terminate();
}
