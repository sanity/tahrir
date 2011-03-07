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
}
