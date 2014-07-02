package tahrir.transport.rpc;

public interface TrSession {
	public void registerFailureListener(Runnable listener);
}
