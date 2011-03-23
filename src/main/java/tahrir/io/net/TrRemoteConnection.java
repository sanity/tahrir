package tahrir.io.net;

import java.util.EnumMap;

import com.google.common.collect.Maps;

public abstract class TrRemoteConnection {
	private ConnState state = ConnState.INIT;

	private final EnumMap<ConnState, StateChangeListener> stateChangeListeners = Maps.newEnumMap(ConnState.class);

	public ConnState getState() {
		return state;
	}

	public void registerStateChangeListener(final ConnState toState, final StateChangeListener listener) {
		stateChangeListeners.put(toState, listener);
	}

	public void removeStateChangeListener(final ConnState toState) {
		stateChangeListeners.remove(toState);
	}

	public abstract void send(byte[] data) throws WrongStateException;

	protected void changeState(final ConnState newState) {
		final ConnState oldState = state;
		state = newState;
		final StateChangeListener listener = stateChangeListeners.get(newState);
		if (listener != null) {
			listener.changed(oldState, newState);
		}
	}

	public enum ConnState {
		INIT, CONNECTING, ACTIVE, CLOSED
	}

	public static interface StateChangeListener {
		public void changed(ConnState fromState, ConnState toState);
	}

	public static class WrongStateException extends Exception {
		private static final long serialVersionUID = 8173918760094188269L;

		public WrongStateException() {
			super();
		}

		public WrongStateException(final String arg0, final Throwable arg1) {
			super(arg0, arg1);
		}

		public WrongStateException(final String arg0) {
			super(arg0);
		}

		public WrongStateException(final Throwable arg0) {
			super(arg0);
		}

	}
}
