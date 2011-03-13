package tahrir.io.net;

import java.lang.reflect.*;

import tahrir.tools.TrMath;


public class TrNet {
	public class IH implements InvocationHandler {

		public IH(final Class<?> c, final TrRemoteConnection connection, final int sessionId) {
			// TODO Auto-generated constructor stub
		}

		public Object invoke(final Object arg0, final Method arg1, final Object[] arg2) throws Throwable {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public <T extends TrSession> T getRemoteSession(final Class<T> c, final TrRemoteConnection connection) {
		return getRemoteSession(c, connection, TrMath.rand.nextInt());
	}

	@SuppressWarnings("unchecked")
	public <T extends TrSession> T getRemoteSession(final Class<T> c, final TrRemoteConnection connection,
			final int sessionId) {
		return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, new IH(c, connection, sessionId));
	}
}
