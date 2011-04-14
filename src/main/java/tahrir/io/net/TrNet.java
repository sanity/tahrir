package tahrir.io.net;

import java.io.*;
import java.lang.reflect.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.serialization.TrSerializer;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Function;
import com.google.common.collect.*;

public class TrNet<RA extends TrRemoteAddress> {

	private static final int hashCode(final Method method) {
		return method.hashCode() ^ Arrays.deepHashCode(method.getGenericParameterTypes());
	}

	public final Map<RA, TrRemoteConnection<RA>> activeConnections = new MapMaker().makeMap();

	private final Map<Class<? extends TrSession>, Class<? extends TrSessionImpl>> classesByInterface = Maps
	.newHashMap();

	private final ConcurrentLinkedQueue<Function<TrRemoteAddress, Void>> connectedListeners = new ConcurrentLinkedQueue<Function<TrRemoteAddress, Void>>();

	private final ConcurrentLinkedQueue<Function<TrRemoteAddress, Void>> disconnectedListeners = new ConcurrentLinkedQueue<Function<TrRemoteAddress, Void>>();

	private final Logger logger;

	private final Map<Integer, MethodPair> methodsById = Maps.newHashMap();

	private final TrNetworkInterface<RA> netIface;

	final Map<Tuple2<String, Integer>, TrSessionImpl> sessions = new MapMaker()
	.expiration(30, TimeUnit.MINUTES)
	.evictionListener(new MapEvictionListener<Tuple2<String, Integer>, TrSessionImpl>() {

		public void onEviction(final Tuple2<String, Integer> sessionInfo, final TrSessionImpl session) {
			session.terminate();
		}
	}).makeMap();

	private final TrNode<RA> trNode;

	public TrNet(final TrNode<RA> trNode, final TrNetworkInterface<RA> netIface, final boolean allowUnilateral) {
		this.netIface = netIface;
		this.logger = LoggerFactory.getLogger(" TrNet(" + netIface + ")");
		this.trNode = trNode;
		if (allowUnilateral) {
			netIface.registerListener(new TrNetMessageListener());
		}
	}

	public void addConnectedListener(final Function<TrRemoteAddress, Void> connectedListener) {
		connectedListeners.add(connectedListener);
	}

	public void addDisconnectedListener(final Function<TrRemoteAddress, Void> disconnectedListener) {
		disconnectedListeners.add(disconnectedListener);
	}

	public TrRemoteConnection<RA> connectTo(final RA address, final RSAPublicKey remotePubkey, final boolean unilateral) {
		final TrRemoteConnection<RA> rc = activeConnections.get(address);
		if (rc != null)
			return rc;
		final TrRemoteConnection<RA> connection = netIface.connect(address, remotePubkey,
				new TrNetMessageListener(),
				null, new Runnable() {

			public void run() {
				activeConnections.remove(address);
				for (final Function<TrRemoteAddress, Void> r : disconnectedListeners) {
					r.apply(address);
				}
			}

		}, unilateral);
		this.activeConnections.put(address, connection);
		return connection;
	}

	@SuppressWarnings("unchecked")
	public <T extends TrSessionImpl> T getOrCreateLocalSession(final Class<T> c, final int sessionId) {
		try {
			T session = (T) this.sessions.get(Tuple2.of(c.getName(), sessionId));
			if (session == null) {
				final Constructor<?> constructor = c.getConstructor(Integer.class, TrNode.class, TrNet.class);
				session = (T) constructor.newInstance(sessionId, trNode, this);
			}
			// We put regardless of whether it is new or not to reset cache
			// expiry time
			this.sessions.put(Tuple2.of(c.getName(), sessionId), session);
			TrSessionImpl.sender.set(null);
			return session;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public <T extends TrSession> T getOrCreateRemoteSession(final Class<T> c, final TrRemoteConnection<?> connection,
			final double priority) {
		return getRemoteSession(c, connection, TrUtils.rand.nextInt(), priority);
	}

	@SuppressWarnings("unchecked")
	public <T extends TrSession> T getRemoteSession(final Class<T> c, final TrRemoteConnection<?> connection,
			final int sessionId, final double priority) {
		return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, new IH(c, connection, sessionId,
				priority));
	}

	public void registerSessionClass(final Class<? extends TrSession> iface, final Class<? extends TrSessionImpl> cls) {
		if (!iface.isInterface())
			throw new RuntimeException(iface + " is not an interface");
		if (cls.isInterface())
			throw new RuntimeException(cls + " is an interface, not a class");
		if (!TrSessionImpl.class.isAssignableFrom(cls))
			throw new RuntimeException(cls + " isn't a subclass of TrSessionImpl");
		if (!iface.isAssignableFrom(cls))
			throw new RuntimeException(cls + " is not an implementation of " + iface);
		try {
			if (cls.getConstructor(Integer.class, TrNode.class, TrNet.class) == null)
				throw new RuntimeException(
						cls
						+ " must have a constructor that takes parameters (java.lang.Integer, tahrir.TrNode, tahrir.io.net.TrNet)");
		} catch (final Exception e1) {
			throw new RuntimeException(e1);
		}
		classesByInterface.put(iface, cls);
		for (final Method ifaceMethod : iface.getMethods()) {
			try {
				final MethodPair methodPair = new MethodPair(ifaceMethod, cls.getMethod(ifaceMethod.getName(),
						ifaceMethod.getParameterTypes()));
				final int modifiers = methodPair.cls.getModifiers();
				if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
					continue;
				}
				if (!methodPair.cls.getReturnType().equals(Void.TYPE))
					throw new RuntimeException("Session method " + methodPair.cls
							+ " has non-void return time, this isn't currently supported by TrNet");
				final MethodPair replacedMethodPair = methodsById.put(hashCode(methodPair.iface), methodPair);
				if (replacedMethodPair != null)
					throw new RuntimeException("Method " + methodPair.cls + " and method " + replacedMethodPair.cls
							+ " hash to the same value (" + hashCode(methodPair.cls) + "), one of them must be renamed");
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	public boolean removeConnectedListener(final Function<TrRemoteAddress, Void> connectedListener) {
		return connectedListeners.remove(connectedListener);
	}

	public boolean removeDisconnectedListener(final Function<TrRemoteAddress, Void> disconnectedListener) {
		return disconnectedListeners.remove(disconnectedListener);
	}

	public class IH implements InvocationHandler {

		private final Class<?> c;
		private final TrRemoteConnection<?> connection;
		private final double priority;
		private final int sessionId;

		public IH(final Class<?> c, final TrRemoteConnection<?> connection, final int sessionId, final double priority) {
			this.c = c;
			this.connection = connection;
			this.sessionId = sessionId;
			this.priority = priority;
		}

		public Object invoke(final Object object, final Method method, final Object[] arguments) throws Throwable {
			// We have to include the parameter types because for some dumb
			// reason Method.hashCode() ignores these.
			if (TrNet.this.logger.isDebugEnabled()) {
				final String args = Arrays.toString(arguments);
				TrNet.this.logger.debug("Sending " + method.getName() + "(" + args.substring(1, args.length() - 1)
						+ ")");
			}
			final int methodId = TrNet.hashCode(method);
			final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
			MessageType.METHOD_CALL.write(builder);
			builder.writeInt(sessionId);
			builder.writeInt(methodId);
			for (final Object argument : arguments) {
				TrSerializer.serializeTo(argument, builder);
			}

			connection.send(builder.build(), priority, TrNetworkInterface.nullSentListener);
			return null;
		}

	}

	public static final class MethodPair {
		public final Method iface, cls;

		public MethodPair(final Method iface, final Method cls) {
			this.iface = iface;
			this.cls = cls;
		}

	}

	private enum MessageType {
		METHOD_CALL(0);

		public static Map<Byte, MessageType> forBytes;
		static {
			forBytes = Maps.newHashMap();
			for (final MessageType t : MessageType.values()) {
				forBytes.put(t.id, t);
			}
		}

		public final byte id;

		MessageType(final int id) {
			this.id = (byte) id;
		}

		public void write(final DataOutputStream dos) throws IOException {
			dos.writeByte(id);
		}
	}

	private final class TrNetMessageListener implements TrMessageListener<RA> {
		public void received(final TrNetworkInterface<RA> iFace, final RA sender, final ByteArraySegment message) {
			final DataInputStream dis = message.toDataInputStream();
			try {
				final MessageType messageType = MessageType.forBytes.get(dis.readByte());
				switch (messageType) {
				case METHOD_CALL:
					final int sessionId = dis.readInt();
					final int methodId = dis.readInt();
					final MethodPair methodPair = methodsById.get(methodId);
					TrSessionImpl session = sessions.get(Tuple2.of(methodPair.cls.getDeclaringClass().getName(),
							sessionId));

					// Do we have a connection to this sender?
					TrRemoteConnection<RA> senderConnection = TrNet.this.activeConnections.get(sender);
					final boolean newConnection = (senderConnection == null);
					if (senderConnection == null) {
						senderConnection = TrNet.this.connectTo(sender, null, false);
					}

					if (session == null) {
						// New session, we need to create it
						final Constructor<?> constructor = methodPair.cls.getDeclaringClass().getConstructor(
								Integer.class, TrNode.class, TrNet.class);
						session = (TrSessionImpl) constructor.newInstance(sessionId, trNode, TrNet.this);

						if (newConnection) {
							// If this connection was created for this session,
							// then we probably want to delete it when the
							// session is done
							final TrRemoteConnection<RA> finalSenderConnection = senderConnection;
							session.addTerminateCallback(new Runnable() {

								public void run() {
									if (!TrNet.this.trNode.peerManager.shouldRetainConnectionTo(sender)) {
										finalSenderConnection.disconnect();
									}
								}

							});
						}
					}
					// We put regardless of whether it is new or not to
					// reset cache expiry time
					sessions.put(Tuple2.of(methodPair.cls.getDeclaringClass().getName(), sessionId), session);

					final Object[] args = new Object[methodPair.cls.getParameterTypes().length];
					for (int i = 0; i < args.length; i++) {
						args[i] = TrSerializer.deserializeFrom(methodPair.cls.getParameterTypes()[i], dis);
					}

					TrSessionImpl.sender.set(senderConnection);

					if (TrNet.this.logger.isDebugEnabled()) {
						final String argsStr = Arrays.toString(args);
						TrNet.this.logger.debug("Received " + methodPair.cls.getName() + "("
								+ argsStr.substring(1, argsStr.length() - 1) + ")");
					}

					methodPair.cls.invoke(session, args);
					break;
				}
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
