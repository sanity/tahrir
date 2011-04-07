package tahrir.io.net;

import java.io.*;
import java.lang.reflect.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import tahrir.TrNode;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.serialization.TrSerializer;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.MapMaker;


public class TrNet<RA extends TrRemoteAddress> {

	private final TrNode<RA> trNode;

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

	public TrNet(final TrNode<RA> trNode) {
		this.trNode = trNode;
	}

	public class IH implements InvocationHandler {

		private final Class<?> c;
		private final TrRemoteConnection<?> connection;
		private final int sessionId;
		private final double priority;

		public IH(final Class<?> c, final TrRemoteConnection<?> connection, final int sessionId, final double priority) {
			this.c = c;
			this.connection = connection;
			this.sessionId = sessionId;
			this.priority = priority;
		}

		public Object invoke(final Object object, final Method method, final Object[] arguments) throws Throwable {
			// We have to include the parameter types because for some dumb
			// reason Method.hashCode() ignores these.
			final int methodId = TrNet.hashCode(method);
			final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
			MessageType.METHOD_CALL.write(builder);
			builder.writeInt(sessionId);
			builder.writeInt(methodId);
			for (int x = 0; x < method.getParameterTypes().length; x++) {
				final Class<?> type = method.getParameterTypes()[x];
				final Object argument = arguments[x];
				if (!(TrRemoteConnection.class.isAssignableFrom(type))) {
					if (argument == null)
						throw new RuntimeException("Null argument with type " + type + " in method " + method
								+ ", but null arguments are not supported at this time");
					TrSerializer.serializeTo(argument, builder);
				} else {
					if (argument != null)
						throw new RuntimeException("Sender placeholder TrRemoteConnection field must be called with 'null'");
				}
			}

			connection.send(builder.build(), priority, TrNetworkInterface.nullSentListener);
			return null;
		}

	}

	public TrRemoteConnection<RA> connectTo(final RA address, final RSAPublicKey remotePubkey) {
		return trNode.networkInterface.connectTo(address, remotePubkey, new TrMessageListener<RA>() {

			public void received(final TrNetworkInterface<RA> iFace, final RA sender, final ByteArraySegment message) {
				final DataInputStream dis = message.toDataInputStream();
				try {
					final MessageType messageType = MessageType.forBytes.get(dis.readByte());
					switch (messageType) {
					case METHOD_CALL:
						final int sessionId = dis.readInt();
						final int methodId = dis.readInt();
						final MethodPair methodPair = methodsById.get(methodId);
						if (methodPair == null)
							throw new RuntimeException(
									"Unrecognized methodId "
									+ methodId
									+ ", is the method you are calling public, non-static and annotated with @Remote?");
						TrSessionImpl session = sessions.get(Tuple2.of(methodPair.cls.getDeclaringClass().getName(),
								sessionId));
						if (session == null) {
							final Constructor<?> constructor = methodPair.cls.getDeclaringClass().getConstructor(
									Integer.class,
									TrNode.class);
							session = (TrSessionImpl) constructor.newInstance(sessionId, trNode);
						}
						// We put regardless of whether it is new or not to
						// reset cache expiry time
						sessions.put(Tuple2.of(methodPair.cls.getDeclaringClass().getName(), sessionId), session);

						final ArrayList<Object> args = new ArrayList<Object>(methodPair.cls.getParameterTypes().length);
						for (final Class<?> t : methodPair.cls.getParameterTypes()) {
							if (TrRemoteConnection.class.isAssignableFrom(t)) {
								args.add(trNode.networkInterface.getConnectionForAddress(sender));
							} else {
								args.add(TrSerializer.deserializeFrom(t, dis));
							}
						}

						methodPair.cls.invoke(session, args.toArray());
						break;
					}
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private static final int hashCode(final Method method) {
		return method.hashCode() ^ Arrays.deepHashCode(method.getGenericParameterTypes());
	}

	private final Map<Tuple2<String, Integer>, TrSessionImpl> sessions = new MapMaker()
	.expiration(30,
			TimeUnit.MINUTES).makeMap();

	private final Map<Integer, MethodPair> methodsById = Maps.newHashMap();
	private final Map<Class<? extends TrSession>, Class<? extends TrSessionImpl>> classesByInterface = Maps
	.newHashMap();

	public void registerSessionClass(final Class<? extends TrSession> iface,
 final Class<? extends TrSessionImpl> cls,
			final Object... additionalConstructorParameters) {
		if (!iface.isInterface())
			throw new RuntimeException(iface + " is not an interface");
		if (cls.isInterface())
			throw new RuntimeException(cls+" is an interface, not a class");
		if (!TrSessionImpl.class.isAssignableFrom(cls))
			throw new RuntimeException(cls+" isn't a subclass of TrSessionImpl");
		if (!iface.isAssignableFrom(cls))
			throw new RuntimeException(cls+" is not an implementation of "+iface);
		try {
			final ArrayList<Class<?>> constructorParamTypes = new ArrayList<Class<?>>();
			constructorParamTypes.add(Integer.class);
			for (final Object acp : additionalConstructorParameters) {
				constructorParamTypes.add(acp.getClass());
			}
			final Class<?>[] cptA = new Class<?>[constructorParamTypes.size()];
			if (cls.getConstructor(constructorParamTypes.toArray(cptA)) == null)
				throw new RuntimeException(cls
						+ " must have a constructor that takes parameters (java.lang.Integer, tahrir.TrNode)");
		} catch (final Exception e1) {
			throw new RuntimeException(e1);
		}
		classesByInterface.put(iface, cls);
		for (final Method ifaceMethod : iface.getMethods()) {
			try {
				final MethodPair methodPair = new MethodPair(ifaceMethod, cls.getMethod(ifaceMethod.getName(),
						ifaceMethod.getParameterTypes()));
				final int modifiers = methodPair.cls.getModifiers();
				if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)
						|| !(methodPair.iface.isAnnotationPresent(TrSession.Remote.class) || methodPair.cls
								.isAnnotationPresent(TrSession.Remote.class))) {
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

	public <T extends TrSession> T getOrCreateRemoteSession(final Class<T> c, final TrRemoteConnection<?> connection,
			final double priority) {
		return getRemoteSession(c, connection, TrUtils.rand.nextInt(), priority);
	}

	@SuppressWarnings("unchecked")
	public <T extends TrSessionImpl> T getOrCreateLocalSession(final Class<T> c, final int sessionId,
			final Object... additionalConstructorParams) {
		try {
			T session = (T) this.sessions.get(Tuple2.of(c.getName(), sessionId));
			if (session == null) {
				final Constructor<?> constructor = c.getConstructor(Integer.class, TrNode.class);
				final ArrayList<Object> constructorParams = new ArrayList<Object>();
				constructorParams.add(sessionId);
				for (final Object ap : additionalConstructorParams) {
					constructorParams.add(ap);
				}
				session = (T) constructor.newInstance(constructorParams.toArray());
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

	@SuppressWarnings("unchecked")
	public <T extends TrSession> T getRemoteSession(final Class<T> c, final TrRemoteConnection<?> connection,
			final int sessionId, final double priority) {
		return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, new IH(c, connection, sessionId,
				priority));
	}

	public static final class MethodPair {
		public final Method iface, cls;

		public MethodPair(final Method iface, final Method cls) {
			this.iface = iface;
			this.cls = cls;
		}

	}
}
