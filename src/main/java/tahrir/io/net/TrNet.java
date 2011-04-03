package tahrir.io.net;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import tahrir.io.net.TrSession.Priority;
import tahrir.io.serialization.TrSerializer;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

import com.beust.jcommander.internal.Maps;


public class TrNet {

	private final TrNetworkInterface<?> iface;

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

	public TrNet(final TrNetworkInterface<?> iface) {
		this.iface = iface;
	}

	public class IH implements InvocationHandler {

		private final Class<?> c;
		private final TrRemoteConnection<?> connection;
		private final int sessionId;

		public IH(final Class<?> c, final TrRemoteConnection<?> connection, final int sessionId) {
			this.c = c;
			this.connection = connection;
			this.sessionId = sessionId;
		}

		public Object invoke(final Object object, final Method method, final Object[] arguments) throws Throwable {
			// We have to include the parameter types because for some dumb
			// reason Method.hashCode() ignores these.
			final int methodId = TrNet.hashCode(method);
			final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
			builder.writeInt(sessionId);
			MessageType.METHOD_CALL.write(builder);
			builder.writeInt(methodId);
			for (final Object argument : arguments) {
				TrSerializer.serializeTo(argument, builder);
			}
			final Priority priorityAnnotation = method.getAnnotation(TrSession.Priority.class);
			if (priorityAnnotation == null)
				throw new RuntimeException("Method " + method + " does not have a @Priority annotation");
			connection.send(builder.build(), priorityAnnotation.value(), TrNetworkInterface.nullSentListener);
			return null;
		}

	}

	private static final int hashCode(final Method method) {
		return method.hashCode() ^ Arrays.deepHashCode(method.getGenericParameterTypes());
	}

	private final Map<Integer, Method> methodsById = Maps.newHashMap();
	private final Map<Class<? extends TrSession>, Class<? extends TrSession>> classesByInterface = Maps.newHashMap();

	public void registerSessionClass(final Class<? extends TrSession> cls, final Class<? extends TrSession> iface) {
		classesByInterface.put(iface, cls);
		for (final Method m : iface.getMethods()) {
			try {
				final Method classMethod = cls.getMethod(m.getName(), m.getParameterTypes());
				final Method replacedMethod = methodsById.put(hashCode(classMethod), classMethod);
				if (replacedMethod != null)
					throw new RuntimeException("Method " + classMethod + " and method " + replacedMethod
							+ " hash to the same value (" + hashCode(classMethod) + "), one of them must be renamed");
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public <T extends TrSession> T getRemoteSession(final Class<T> c, final TrRemoteConnection<?> connection) {
		return getRemoteSession(c, connection, TrUtils.rand.nextInt());
	}

	@SuppressWarnings("unchecked")
	public <T extends TrSession> T getRemoteSession(final Class<T> c, final TrRemoteConnection<?> connection,
			final int sessionId) {
		return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, new IH(c, connection, sessionId));
	}
}
