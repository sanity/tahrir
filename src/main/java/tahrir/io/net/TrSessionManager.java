package tahrir.io.net;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrNode;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.io.net.sessions.Priority;
import tahrir.io.net.udpV1.UdpNetworkLocation;
import tahrir.io.serialization.TrSerializer;
import tahrir.tools.ByteArraySegment;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;
import tahrir.tools.TrUtils;
import tahrir.tools.Tuple2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class TrSessionManager {

	private static final int hashCode(final Method method) {
		return method.hashCode() ^ Arrays.deepHashCode(method.getGenericParameterTypes());
	}

	private final Map<Class<? extends TrSession>, Class<? extends TrSessionImpl>> classesByInterface = Maps
			.newHashMap();

	private final ConcurrentLinkedQueue<Function<PhysicalNetworkLocation, Void>> connectedListeners = new ConcurrentLinkedQueue<Function<PhysicalNetworkLocation, Void>>();

	private final ConcurrentLinkedQueue<Function<PhysicalNetworkLocation, Void>> disconnectedListeners = new ConcurrentLinkedQueue<Function<PhysicalNetworkLocation, Void>>();

	private static final Logger logger = LoggerFactory.getLogger(TrSessionManager.class);

	private final Map<Integer, MethodPair> methodsById = Maps.newHashMap();

	public final Map<Tuple2<String, Integer>, TrSessionImpl> sessions = CacheBuilder.newBuilder()
			.expireAfterWrite(30, TimeUnit.MINUTES)
			.removalListener(new RemovalListener<Tuple2<String, Integer>, TrSessionImpl>() {

				@Override
				public void onRemoval(final RemovalNotification<Tuple2<String, Integer>, TrSessionImpl> sessionInfo) {
                    if (!sessionInfo.getCause().equals(RemovalCause.REPLACED)) {
					    sessionInfo.getValue().terminate();
                    }
				}

			}).<Tuple2<String, Integer>, TrSessionImpl> build().asMap();

	private final TrNode trNode;

	private final Map<Class<? extends PhysicalNetworkLocation>, TrNetworkInterface> interfacesByAddressType;

	public TrSessionManager(final TrNode trNode, final TrNetworkInterface i, final boolean allowUnilateral) {
		this(trNode, Collections.singleton(i), allowUnilateral);
	}

	public TrSessionManager(final TrNode trNode, final Iterable<TrNetworkInterface> interfaces,
			final boolean allowUnilateral) {
		interfacesByAddressType = Maps.newHashMap();
		for (final TrNetworkInterface iface : interfaces) {
			interfacesByAddressType.put(iface.getAddressClass(), iface);
		}
		this.trNode = trNode;
		if (allowUnilateral) {
			for (final TrNetworkInterface netIface : interfacesByAddressType.values()) {
				netIface.allowUnsolicitedInbound(new TrNetMessageListener());
			}
		}
	}

	public void addConnectedListener(final Function<PhysicalNetworkLocation, Void> connectedListener) {
		connectedListeners.add(connectedListener);
	}

	public void addDisconnectedListener(final Function<PhysicalNetworkLocation, Void> disconnectedListener) {
		disconnectedListeners.add(disconnectedListener);
	}


	@SuppressWarnings("unchecked")
	public <T extends TrSessionImpl> T getOrCreateLocalSession(final Class<T> c, final int sessionId) {
		try {
			T session = (T) sessions.get(Tuple2.of(c.getName(), sessionId));
			if (session == null) {
				final Constructor<?> constructor = c.getConstructor(Integer.class, TrNode.class, TrSessionManager.class);
				session = (T) constructor.newInstance(sessionId, trNode, this);
			}
			// We put regardless of whether it is new or not to reset cache
			// expiry time
			sessions.put(Tuple2.of(c.getName(), sessionId), session);
			TrSessionImpl.sender.set(null);
			return session;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public <T extends TrSession> T getOrCreateRemoteSession(final Class<T> c, final TrRemoteConnection connection) {
		return getOrCreateRemoteSession(c, connection, TrUtils.rand.nextInt());
	}

	@SuppressWarnings("unchecked")
	public <T extends TrSession> T getOrCreateRemoteSession(final Class<T> c, final TrRemoteConnection connection,
			final int sessionId) {
		return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, new IH(c, connection, sessionId));
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
			if (cls.getConstructor(Integer.class, TrNode.class, TrSessionManager.class) == null)
				throw new RuntimeException(
						cls
						+ " must have a constructor that takes parameters (java.lang.Integer, tahrir.TrNode, tahrir.io.net.TrNet)");
		} catch (final Exception e1) {
			throw new RuntimeException(e1);
		}
		classesByInterface.put(iface, cls);
		for (final Method ifaceMethod : iface.getMethods()) {
			if (ifaceMethod.getName().equals("registerFailureListener")) {
				continue;
			}
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
	public boolean removeConnectedListener(final Function<PhysicalNetworkLocation, Void> connectedListener) {
		return connectedListeners.remove(connectedListener);
	}

	public boolean removeDisconnectedListener(final Function<PhysicalNetworkLocation, Void> disconnectedListener) {
		return disconnectedListeners.remove(disconnectedListener);
	}

	public static class IH implements InvocationHandler {

		private final Class<?> c;
		private final TrRemoteConnection connection;
		private final int sessionId;
		private Runnable failureCallback = null;

		public IH(final Class<?> c, final TrRemoteConnection connection, final int sessionId) {
			this.c = c;
			this.connection = connection;
			this.sessionId = sessionId;
		}

		public Object invoke(final Object object, final Method method, final Object[] arguments) throws Throwable {
			if (method.getName().equals("registerFailureListener")) {
				if (arguments.length != 1)
					throw new RuntimeException("registerFailureListener() must have only one parameter");
				if (failureCallback != null)
					throw new RuntimeException("Only one failureCallback may be registered per remote session");
				failureCallback = (Runnable) arguments[0];
				return null;
			}

			// We have to include the parameter types because for some dumb
			// reason Method.hashCode() ignores these.
			if (logger.isDebugEnabled() && arguments != null) {
				final String args = Joiner.on(",").join(Iterables.transform(Lists.newArrayList(arguments), toStringer));
				logger.debug("\tSending " + method.getName() + "(" + args
						+ ")\t -> "+connection.remoteAddress);
			}
			final int methodId = TrSessionManager.hashCode(method);
			final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
			MessageType.METHOD_CALL.write(builder);
			builder.writeInt(sessionId);
			builder.writeInt(methodId);
			if (arguments != null) {
				for (final Object argument : arguments) {
					TrSerializer.serializeTo(argument, builder);
				}
			}

			final Priority priority = method.getAnnotation(Priority.class);

			if (priority == null)
				throw new RuntimeException("Required @Priority annotation missing on method " + method
						+ " in interface "
						+ method.getDeclaringClass());

			final ByteArraySegment messageBAS = builder.build();

			connection.send(messageBAS, priority.value(), new TrSentReceivedListener() {

				public void sent() {

				}

				public void failure() {
					connection.disconnect();
					if (failureCallback != null) {
						failureCallback.run();
					}
				}

				public void received() {

				}
			});
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

	private final class TrNetMessageListener implements TrMessageListener {
		public void received(final TrNetworkInterface iFace, final PhysicalNetworkLocation sender,
				final ByteArraySegment message) {
			final DataInputStream dis = message.toDataInputStream();
			try {
				final byte messageTypeByte = dis.readByte();
				final MessageType messageType = MessageType.forBytes.get(messageTypeByte);
				switch (messageType) {
				case METHOD_CALL:
					final int sessionId = dis.readInt();
					final int methodId = dis.readInt();
					final MethodPair methodPair = methodsById.get(methodId);
					TrSessionImpl session = sessions.get(Tuple2.of(methodPair.cls.getDeclaringClass().getName(),
							sessionId));


					if (session == null) {
						// New session, we need to create it
						final Constructor<?> constructor = methodPair.cls.getDeclaringClass().getConstructor(
								Integer.class, TrNode.class, TrSessionManager.class);
						session = (TrSessionImpl) constructor.newInstance(sessionId, trNode, TrSessionManager.this);
					}
					// We put regardless of whether it is new or not to
					// reset cache expiry time
					sessions.put(Tuple2.of(methodPair.cls.getDeclaringClass().getName(), sessionId), session);

					final Object[] args = new Object[methodPair.cls.getParameterTypes().length];
					for (int i = 0; i < args.length; i++) {
						args[i] = TrSerializer.deserializeFromType(methodPair.cls.getGenericParameterTypes()[i], dis);
					}

					TrSessionImpl.sender.set(sender);

					if (logger.isDebugEnabled()) {
						final String argsStr = Joiner.on(",").join(Iterables.transform(Lists.newArrayList(args), toStringer));
						logger.debug("Received " + methodPair.cls.getName() + "("
								+ argsStr + ")\t <- "+sender);
					}

					methodPair.cls.invoke(session, args);
					break;
				}
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public ConnectionManager connectionManager = new ConnectionManager();

	public class ConnectionManager {

		private final Map<PhysicalNetworkLocation, ConnectionInfo> connections = new MapMaker().makeMap();

		public TrRemoteConnection getConnection(final RemoteNodeAddress address,
				final boolean unilateral, final String userLabel) {
			return getConnection(address, unilateral, userLabel, TrUtils.noopRunnable);
		}

		public TrRemoteConnection getConnection(final RemoteNodeAddress address,
				final boolean unilateral, final String userLabel, final Runnable disconnectCallback) {
			ConnectionInfo ci = connections.get(address.physicalLocation);
			if (ci == null) {
				//if (address.publicKey == null)
				//	throw new RuntimeException("We need the peer's public key unless we're already connected to it");
				ci = new ConnectionInfo();
				final ConnectionInfo finalCi = ci;
				final TrNetworkInterface netIface = interfacesByAddressType.get(address.physicalLocation.getClass());
				if (netIface == null)
					throw new RuntimeException("Unknown TrRemoteAddress type: " + address.physicalLocation.getClass());
				ci.remoteConnection = netIface.connect(address.physicalLocation, address.publicKey, new TrNetMessageListener(), null,
						new Runnable() {

					public void run() {
						connections.remove(address.physicalLocation);
						for (final Runnable r : finalCi.interests.values()) {
							r.run();
						}
					}

				}, unilateral);
				connections.put(address.physicalLocation, ci);
			}
			ci.interests.put(userLabel, disconnectCallback);
			return ci.remoteConnection;
		}

		public void noLongerNeeded(final PhysicalNetworkLocation physicalLocation, final String userLabel) {
            logger.debug("Connection to {} is no longer needed by {}", physicalLocation, userLabel);
			final ConnectionInfo ci = connections.get(physicalLocation);
			ci.interests.remove(userLabel);
			if (ci.interests.isEmpty()) {
				connections.remove(physicalLocation);
				ci.remoteConnection.disconnect();
			}
		}

	}

	private static class ConnectionInfo {
		Map<String, Runnable> interests = new MapMaker().makeMap();
		TrRemoteConnection remoteConnection;
	}

	public <T extends TrSessionImpl> T getOrCreateLocalSession(final Class<T> cls) {
		return this.getOrCreateLocalSession(cls, TrUtils.rand.nextInt());
	}

	public static final Function<Object, String> toStringer = new Function<Object, String>() {

		@Override
		public String apply(final Object input) {
			if (input instanceof RSAPublicKey)
				return "RSAPublicKey["+input.hashCode()+"]";
			else if (input instanceof UdpNetworkLocation)
				return "UDP:"+((UdpNetworkLocation) input).port;
			else
				return input.toString();
		}};
}
