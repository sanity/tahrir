package tahrir.io.net;

import java.net.InetAddress;
import java.security.interfaces.*;

import org.slf4j.*;
import org.testng.annotations.Test;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.sessions.Priority;
import tahrir.io.net.udpV1.*;
import tahrir.io.net.udpV1.UdpNetworkInterface.Config;
import tahrir.tools.Tuple2;

public class TrNetTest {
	Logger logger = LoggerFactory.getLogger(TrNetTest.class);

	@Test
	public void simpleTest() throws Exception {
		final Config conf1 = new Config();
		conf1.listenPort = 3912;
		conf1.maxUpstreamBytesPerSecond = 1024;

		final Config conf2 = new Config();
		conf2.listenPort = 3913;
		conf2.maxUpstreamBytesPerSecond = 1024;

		logger.info("Generating public-private keys");

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		logger.info("Done");

		final UdpNetworkInterface iface1 = new UdpNetworkInterface(conf1, kp1);

		final UdpNetworkInterface iface2 = new UdpNetworkInterface(conf2, kp2);


		final TrNode node1 = new TrNode();
		final TrNet trn1 = new TrNet(node1, iface1, false);

		trn1.registerSessionClass(TestSession.class, TestSessionImpl.class);

		final TrNode node2 = new TrNode();
		final TrNet trn2 = new TrNet(node2, iface2, false);

		trn2.registerSessionClass(TestSession.class, TestSessionImpl.class);

		final TrRemoteConnection one2two = trn1.connectionManager.getConnection(
				new UdpRemoteAddress(
						InetAddress.getLocalHost(), conf2.listenPort), kp2.a, false, "trn1");
		final TrRemoteConnection two2one = trn2.connectionManager.getConnection(
				new UdpRemoteAddress(
						InetAddress.getLocalHost(), conf1.listenPort), kp1.a, false, "trn2");

		final TestSession remoteSession = trn1.getOrCreateRemoteSession(TestSession.class, one2two, 1234);

		remoteSession.testMethod(0);

		Thread.sleep(1000);
	}

	public static interface TestSession extends TrSession {
		@Priority(TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY)
		public void testMethod(int param);
	}

	public static class TestSessionImpl extends TrSessionImpl implements TestSession {

		public TestSessionImpl(final Integer sessionId, final TrNode node, final TrNet trNet) {
			super(sessionId, node, trNet);
		}

		public void testMethod(final int param) {
			if (param < 10) {
				final TestSession remote = remoteSession(TestSession.class, connection(sender()));
				remote.testMethod(param + 1);
			}
		}
	}
}
