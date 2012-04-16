package tahrir.io.net;

import java.net.InetAddress;
import java.security.interfaces.*;
import java.util.LinkedList;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

import com.google.common.collect.Lists;

import org.slf4j.*;
import org.testng.annotations.Test;

import tahrir.*;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.sessions.Priority;
import tahrir.io.net.udpV1.*;
import tahrir.io.net.udpV1.UdpNetworkInterface.Config;
import tahrir.tools.*;

public class TrNetTest {
	Logger logger = LoggerFactory.getLogger(TrNetTest.class);

	@Test
	public void simpleTest() throws Exception {
		final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);

		final Config udpNetIfaceConf1 = new Config();
		udpNetIfaceConf1.listenPort = 3912;
		udpNetIfaceConf1.maxUpstreamBytesPerSecond = 1024;

		final Config udpNetIfaceConf2 = new Config();
		udpNetIfaceConf2.listenPort = 3913;
		udpNetIfaceConf2.maxUpstreamBytesPerSecond = 1024;

		logger.info("Generating public-private keys");

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		logger.info("Done");

		final UdpNetworkInterface iface1 = new UdpNetworkInterface(udpNetIfaceConf1, kp1);

		final UdpNetworkInterface iface2 = new UdpNetworkInterface(udpNetIfaceConf2, kp2);

		final TrConfig trCfg1 = new TrConfig();
		trCfg1.peers.assimilate = false;

		final TrConfig trCfg2 = new TrConfig();
		trCfg2.peers.assimilate = false;
		final TrNode node1 = new TrNode(TrUtils.createTempDirectory(), trCfg1);
		final TrNet trn1 = new TrNet(node1, iface1, false);

		trn1.registerSessionClass(TestSession.class, TestSessionImpl.class);

		final TrNode node2 = new TrNode(TrUtils.createTempDirectory(), trCfg2);
		final TrNet trn2 = new TrNet(node2, iface2, false);

		trn2.registerSessionClass(TestSession.class, TestSessionImpl.class);

		final TrRemoteConnection one2two = trn1.connectionManager.getConnection(
				new UdpRemoteAddress(
				        InetAddress.getByName("127.0.0.1"), udpNetIfaceConf2.listenPort), kp2.a, false, "trn1");
		final TrRemoteConnection two2one = trn2.connectionManager.getConnection(
				new UdpRemoteAddress(
				        InetAddress.getByName("127.0.0.1"), udpNetIfaceConf1.listenPort), kp1.a, false, "trn2");

		final TestSession remoteSession = trn1.getOrCreateRemoteSession(TestSession.class, one2two, 1234);

		//remoteSession.testMethod(0);

		final LinkedList<Integer> ll = Lists.newLinkedList();
		ll.add(1);
		remoteSession.testMethod2(ll);

	}

	public static interface TestSession extends TrSession {
		@Priority(TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY)
		public void testMethod(int param);

		@Priority(TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY)
		public void testMethod2(LinkedList<Integer> list);
	}

	public static class TestSessionImpl extends TrSessionImpl implements TestSession {

		public TestSessionImpl(final Integer sessionId, final TrNode node, final TrNet trNet) {
			super(sessionId, node, trNet);
		}

		public void testMethod(final int param) {
			if (param < 10) {
				final TrRemoteAddress senderRemoteAddress = sender();
				final TrRemoteConnection connectionToSender = connection(senderRemoteAddress);
				final TestSession remoteSessionOnSender = remoteSession(TestSession.class, connectionToSender);
				remoteSessionOnSender.testMethod(param + 1);
			}
		}

		public void testMethod2(final LinkedList<Integer> list) {
			if (list.size() < 10) {
				int sum = 0;
				for (final int i : list) {
					sum += i;
				}
				list.add(sum);
				final TrRemoteAddress senderRemoteAddress = sender();
				final TrRemoteConnection connectionToSender = connection(senderRemoteAddress);
				final TestSession remoteSessionOnSender = remoteSession(TestSession.class, connectionToSender);
				remoteSessionOnSender.testMethod2(list);
			}
		}
	}
}
