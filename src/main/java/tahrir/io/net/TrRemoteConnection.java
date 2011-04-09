package tahrir.io.net;

import java.io.IOException;
import java.util.Map;

import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.tools.ByteArraySegment;

import com.google.common.collect.Maps;

public abstract class TrRemoteConnection<RA extends TrRemoteAddress> {

	State state = State.CONNECTING;

	Map<State, StateChangeListener> scl = Maps.newConcurrentMap();

	public State getState() {
		return state;
	}

	protected void changeStateTo(final State toState) {
		final State oldState = state;
		state = toState;
		final StateChangeListener s = scl.get(toState);
		if (s != null) {
			s.stateChanged(oldState, toState);
		}
	}

	public void setStateChangeListener(final State toState, final StateChangeListener listener) {
		scl.put(toState, listener);
	}

	public abstract void send(final ByteArraySegment message, final double priority,
			final TrSentReceivedListener sentListener) throws IOException;

	public void unsetStateChangeListener(final State toState) {
		scl.remove(toState);
	}

	public static interface StateChangeListener {
		public void stateChanged(State fromState, State toState);
	}

	public enum State {
		CONNECTING, CONNECTED, DISCONNECTED;
	}

	public abstract void received(TrNetworkInterface<RA> iFace, RA sender, ByteArraySegment message);
}
