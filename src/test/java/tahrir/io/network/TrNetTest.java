package tahrir.io.network;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tahrir.TrNodeConfig;
import tahrir.TrNode;
import tahrir.network.*;
import tahrir.util.crypto.TrCrypto;
import tahrir.network.sessions.Priority;
import tahrir.transport.messaging.udpV1.UdpNetworkInterface;
import tahrir.transport.messaging.udpV1.UdpNetworkInterface.UNIConfig;
import tahrir.transport.messaging.udpV1.UdpNetworkLocation;
import tahrir.util.tools.TrUtils.TestUtils;
import tahrir.util.tools.Tuple2;

import java.net.InetAddress;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.LinkedList;

public class TrNetTest {
	Logger logger = LoggerFactory.getLogger(TrNetTest.class);

	private TestSession remoteSession;

	private static volatile boolean testDone = false;

	@BeforeTest
	public void setUpNodes() throws Exception {
		final UNIConfig udpNetIfaceConf1 = new UNIConfig();
		udpNetIfaceConf1.listenPort = 3912;
		udpNetIfaceConf1.maxUpstreamBytesPerSecond = 1024;

		final UNIConfig udpNetIfaceConf2 = new UNIConfig();
		udpNetIfaceConf2.listenPort = 3913;
		udpNetIfaceConf2.maxUpstreamBytesPerSecond = 1024;

		logger.info("Generating public-private keys");

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		logger.info("Done");

		final TrNetworkInterface iface1 = new UdpNetworkInterface(udpNetIfaceConf1, kp1);

		final TrNetworkInterface iface2 = new UdpNetworkInterface(udpNetIfaceConf2, kp2);

		final TrNodeConfig trCfg1 = new TrNodeConfig();
		setTrConfig(trCfg1);

		final TrNodeConfig trCfg2 = new TrNodeConfig();
		setTrConfig(trCfg2);

		final TrNode node1 = new TrNode(TestUtils.createTempDirectory(), trCfg1);
		final TrSessionManager sessionMgr1 = new TrSessionManager(node1, iface1, false);

		sessionMgr1.registerSessionClass(TestSession.class, TestSessionImpl.class);

		final TrNode node2 = new TrNode(TestUtils.createTempDirectory(), trCfg2);
		final TrSessionManager sessionMgr2 = new TrSessionManager(node2, iface2, false);

		sessionMgr2.registerSessionClass(TestSession.class, TestSessionImpl.class);

		final TrRemoteConnection one2two = sessionMgr1.connectionManager.getConnection(
				new RemoteNodeAddress(new UdpNetworkLocation(InetAddress.getByName("127.0.0.1"), udpNetIfaceConf2.listenPort), kp2.a), false,
				"sessionMgr1");
		final TrRemoteConnection two2one = sessionMgr2.connectionManager.getConnection(
				new RemoteNodeAddress(new UdpNetworkLocation(InetAddress.getByName("127.0.0.1"), udpNetIfaceConf1.listenPort), kp1.a), false,
				"sessionMgr2");

		remoteSession = sessionMgr1.getOrCreateRemoteSession(TestSession.class, one2two, 1234);
	}

	@AfterMethod
	public void purgeTestDone() {
		testDone = false;
	}

	@Test
	public void simpleTest() throws Exception {
		remoteSession.simpleMethod(0);

		for (int x = 0; x < 100; x++) {
			Thread.sleep(100);
			if (testDone) {
				break;
			}
		}

		Assert.assertTrue(testDone);
	}

	@Test
	public void parameterisedTypeTest() throws Exception {
		final LinkedList<Integer> ll = Lists.newLinkedList();
		ll.add(1);
		remoteSession.methodWithparameterizedType(ll);

		for (int x = 0; x < 100; x++) {
			Thread.sleep(100);
			if (testDone) {
				break;
			}
		}

		Assert.assertTrue(testDone);
	}

	@Test
	public void noParameterTest() throws Exception {
		remoteSession.noParamMethod();

		for (int x = 0; x < 20; x++) {
			Thread.sleep(100);
			if (testDone) {
				break;
			}
		}

		Assert.assertTrue(testDone);
	}

	@Test
	public void multipleParameterTest() throws Exception {
		final LinkedList<Integer> ll = Lists.newLinkedList();
		ll.add(1);
		remoteSession.methodWithMultipleParams(0, ll);

		for (int x = 0; x < 100; x++) {
			Thread.sleep(100);
			if (testDone) {
				break;
			}
		}

		Assert.assertTrue(testDone);
	}

	private void setTrConfig(TrNodeConfig config) {
		// we are testing basic networking features, not higher level p2p stuff
		config.peers.assimilate = false;
		config.peers.runMaintainance = false;
		config.peers.runBroadcast = false;
	}

	public static interface TestSession extends TrSession {
		@Priority(TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY)
		public void simpleMethod(int param);

		@Priority(TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY)
		public void methodWithparameterizedType(LinkedList<Integer> list);

		@Priority(TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY)
		public void methodWithMultipleParams(int param1, LinkedList<Integer> param2);

		@Priority(TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY)
		public void noParamMethod();
	}

	public static class TestSessionImpl extends TrSessionImpl implements TestSession {

		public TestSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
			super(sessionId, node, sessionMgr);
		}

		public void simpleMethod(final int param) {
			if (param < 10) {
				final PhysicalNetworkLocation senderRemoteAddress = sender();
				final TrRemoteConnection connectionToSender = connection(senderRemoteAddress);
				final TestSession remoteSessionOnSender = remoteSession(TestSession.class, connectionToSender);
				remoteSessionOnSender.simpleMethod(param + 1);
			} else {
				testDone = true;
			}

		}

		public void methodWithparameterizedType(final LinkedList<Integer> list) {
			if (list.size() < 10) {
				int sum = 0;
				for (final int i : list) {
					sum += i;
				}
				list.add(sum);
				final PhysicalNetworkLocation senderRemoteAddress = sender();
				final TrRemoteConnection connectionToSender = connection(senderRemoteAddress);
				final TestSession remoteSessionOnSender = remoteSession(TestSession.class, connectionToSender);
				remoteSessionOnSender.methodWithparameterizedType(list);
			} else {
				testDone = true;
			}
		}

		public void methodWithMultipleParams(int param1, final LinkedList<Integer> param2) {
			if (param1 < 10 && param2.size() < 10) {
				param1++;
				int sum = 0;
				for (final int i : param2) {
					sum += i;
				}
				param2.add(sum);
				final PhysicalNetworkLocation senderRemoteAddress = sender();
				final TrRemoteConnection connectionToSender = connection(senderRemoteAddress);
				final TestSession remoteSessionOnSender = remoteSession(TestSession.class, connectionToSender);
				remoteSessionOnSender.methodWithMultipleParams(param1, param2);
			} else {
				testDone = true;
			}
		}

		public void noParamMethod() {
			testDone = true;
		}
	}
}
