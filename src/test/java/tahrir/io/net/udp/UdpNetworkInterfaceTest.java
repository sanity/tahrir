package tahrir.io.net.udp;

import java.net.InetAddress;
import java.security.interfaces.*;

import com.google.common.base.Function;

import org.slf4j.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.io.net.udpV1.*;
import tahrir.io.net.udpV1.UdpNetworkInterface.UNIConfig;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

public class UdpNetworkInterfaceTest {
	private static final Logger logger = LoggerFactory.getLogger(UdpNetworkInterfaceTest.class);

	@Test
	public void simpleReliableMessageSend() throws Exception {
		final UNIConfig conf1 = new UNIConfig();
		conf1.listenPort = 3156;
		conf1.maxUpstreamBytesPerSecond = 1024;

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		final TrNetworkInterface i1 = new UdpNetworkInterface(conf1, kp1);

		final UNIConfig conf2 = new UNIConfig();
		conf2.listenPort = 3157;
		conf2.maxUpstreamBytesPerSecond = 1024;
		final TrNetworkInterface i2 = new UdpNetworkInterface(conf2, kp2);

		final UdpRemoteAddress ra1 = new UdpRemoteAddress(InetAddress.getByName("127.0.0.1"), conf1.listenPort);
		final UdpRemoteAddress ra2 = new UdpRemoteAddress(InetAddress.getByName("127.0.0.1"), conf2.listenPort);

		final TrMessageListener noopListener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final TrRemoteAddress sender,
					final ByteArraySegment message) {
			}

		};

		final Called receivedSuccessfully = new Called();

		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 100; x++) {
			msgBuilder.writeByte(33);
		}

		final ByteArraySegment sentMessage = msgBuilder.build();

		final TrMessageListener listener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final TrRemoteAddress sender,
					final ByteArraySegment message) {
				Assert.assertEquals(message, sentMessage);
				receivedSuccessfully.called = true;
			}
		};
		final Called connected1 = new Called();
		final Called disconnected1 = new Called();
		final TrRemoteConnection one2two = i1.connect(ra2, kp2.a, noopListener, connected1,
				disconnected1, false);

		final Called connected2 = new Called();
		final Called disconnected2 = new Called();
		final TrRemoteConnection two2one = i2.connect(ra1, kp1.a, listener, connected2,
				disconnected2, false);

		final Called ackReceived = new Called();

		one2two.send(sentMessage, 1, new TrSentReceivedListener() {

			public void sent() {
				System.out.println("Sent successfully");
			}

			public void failure() {
				Assert.fail();
			}

			public void received() {
				ackReceived.called = true;
				System.out.println("Received successfully");
			}
		});

		for (int x = 0; x < 10; x++) {
			if (ackReceived.called && receivedSuccessfully.called) {
				break;
			}
			Thread.sleep(500);
		}

		Assert.assertTrue(ackReceived.called);
		Assert.assertTrue(receivedSuccessfully.called);
	}

	@Test
	public void simpleReliableUnilateralMessageSend() throws Exception {
		final UNIConfig conf1 = new UNIConfig();
		conf1.listenPort = 4642;
		conf1.maxUpstreamBytesPerSecond = 1024;

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		final TrNetworkInterface i1 = new UdpNetworkInterface(conf1, kp1);

		final UNIConfig conf2 = new UNIConfig();
		conf2.listenPort = 4643;
		conf2.maxUpstreamBytesPerSecond = 1024;
		final TrNetworkInterface i2 = new UdpNetworkInterface(conf2, kp2);

		final Called receivedSuccessfully = new Called();

		final UdpRemoteAddress ra1 = new UdpRemoteAddress(InetAddress.getByName("127.0.0.1"), conf1.listenPort);
		final UdpRemoteAddress ra2 = new UdpRemoteAddress(InetAddress.getByName("127.0.0.1"), conf2.listenPort);

		final TrMessageListener noopListener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final TrRemoteAddress sender,
					final ByteArraySegment message) {
			}

		};

		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 100; x++) {
			msgBuilder.writeByte(33);
		}

		final ByteArraySegment sentMessage = msgBuilder.build();

		final TrMessageListener listener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final TrRemoteAddress sender,
					final ByteArraySegment message) {
				Assert.assertEquals(message, sentMessage);
				receivedSuccessfully.called = true;
			}
		};

		i2.allowUnsolicitedInbound(listener);

		final Called connected1 = new Called();
		final Called disconnected1 = new Called();
		final TrRemoteConnection one2two = i1.connect(ra2, kp2.a, noopListener, connected1,
				disconnected1, true);

		final Called ackReceived = new Called();

		one2two.send(sentMessage, 1, new TrSentReceivedListener() {

			public void sent() {
				logger.debug("Sent successfully");
			}

			public void failure() {
				Assert.fail();
			}

			public void received() {
				ackReceived.called = true;
				System.out.println("Received successfully");
			}
		});

		for (int x = 0; x < 10; x++) {
			if (ackReceived.called && receivedSuccessfully.called) {
				break;
			}
			Thread.sleep(500);
		}

		Assert.assertTrue(ackReceived.called);
		Assert.assertTrue(receivedSuccessfully.called);
	}

	@Test
	public void longReliableMessageSend() throws Exception {
		final UNIConfig conf1 = new UNIConfig();
		conf1.listenPort = 3286;
		conf1.maxUpstreamBytesPerSecond = 1024;

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		final TrNetworkInterface i1 = new UdpNetworkInterface(conf1, kp1);

		final UNIConfig conf2 = new UNIConfig();
		conf2.listenPort = 3287;
		conf2.maxUpstreamBytesPerSecond = 1024;
		final TrNetworkInterface i2 = new UdpNetworkInterface(conf2, kp2);

		final UdpRemoteAddress ra2 = new UdpRemoteAddress(InetAddress.getByName("127.0.0.1"), conf2.listenPort);
		final UdpRemoteAddress ra1 = new UdpRemoteAddress(InetAddress.getByName("127.0.0.1"), conf1.listenPort);

		final TrMessageListener noopListener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final TrRemoteAddress sender,
					final ByteArraySegment message) {
			}

		};

		final Called receivedSuccessfully = new Called();

		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 2000; x++) {
			msgBuilder.writeByte(33);
		}

		final ByteArraySegment sentMessage = msgBuilder.build();

		final TrMessageListener listener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final TrRemoteAddress sender,
					final ByteArraySegment message) {
				Assert.assertEquals(message, sentMessage);
				receivedSuccessfully.called = true;
			}
		};
		final Called connected1 = new Called();
		final Called disconnected1 = new Called();
		final TrRemoteConnection one2two = i1.connect(ra2, kp2.a, noopListener, connected1,
				disconnected1, false);

		final Called connected2 = new Called();
		final Called disconnected2 = new Called();
		final TrRemoteConnection two2one = i2.connect(ra1, kp1.a, listener, connected2,
				disconnected2, false);

		//		for (int x = 0; x < 100; x++) {
		//			if (connected1.called && connected2.called) {
		//				break;
		//			}
		//			Thread.sleep(100);
		//		}
		//
		//		Assert.assertTrue(connected1.called && connected2.called);

		final Called ackReceived = new Called();

		one2two.send(sentMessage, 1, new TrSentReceivedListener() {

			public void sent() {
				System.out.println("Sent successfully");
			}

			public void failure() {
				Assert.fail();
			}

			public void received() {
				ackReceived.called = true;
				System.out.println("Received successfully");
			}
		});

		for (int x = 0; x < 10; x++) {
			if (ackReceived.called && receivedSuccessfully.called) {
				break;
			}
			Thread.sleep(500);
		}

		Assert.assertTrue(ackReceived.called);
		Assert.assertTrue(receivedSuccessfully.called);
	}

	public static class Called implements Runnable, Function<TrRemoteConnection, Void> {
		public volatile boolean called = false;

		public void run() {
			called = true;
		}

		public Void apply(final TrRemoteConnection arg0) {
			called = true;
			return null;
		}

	}
}
