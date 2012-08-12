package tahrir.io.net.udpV1;

import java.net.InetAddress;
import java.security.interfaces.*;

import org.slf4j.*;
import org.testng.Assert;
import org.testng.annotations.*;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.io.net.udpV1.UdpNetworkInterface.UNIConfig;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

import com.google.common.base.Function;

public class UdpNetworkInterfaceTest {
	private static final Logger logger = LoggerFactory.getLogger(UdpNetworkInterfaceTest.class);

	private int port1 = 3156;
	private int port2 = 3157;

	private UdpNetworkInterface i1;
	private UdpNetworkInterface i2;

	private TrMessageListener listener;

	private TrRemoteConnection one2two;
	private TrRemoteConnection two2one;

	private ByteArraySegment sentMessage;

	private Called receivedSuccessfully;
	private Called ackReceived;

	@BeforeMethod
	public void setUp() throws Exception {
		final UNIConfig conf1 = new UNIConfig();
		conf1.listenPort = port1--;
		conf1.maxUpstreamBytesPerSecond = 1024;

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		i1 = new UdpNetworkInterface(conf1, kp1);

		final UNIConfig conf2 = new UNIConfig();
		conf2.listenPort = port2++;
		conf2.maxUpstreamBytesPerSecond = 1024;
		i2 = new UdpNetworkInterface(conf2, kp2);

		final UdpNetworkLocation ra1 = new UdpNetworkLocation(InetAddress.getByName("127.0.0.1"), conf1.listenPort);
		final UdpNetworkLocation ra2 = new UdpNetworkLocation(InetAddress.getByName("127.0.0.1"), conf2.listenPort);

		final TrMessageListener noopListener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final PhysicalNetworkLocation sender,
					final ByteArraySegment message) {
			}

		};

		listener = new TrMessageListener() {

			public void received(final TrNetworkInterface iFace, final PhysicalNetworkLocation sender,
					final ByteArraySegment message) {
				Assert.assertEquals(message, sentMessage);
				receivedSuccessfully.called = true;
			}
		};

		final Called connected1 = new Called();
		final Called disconnected1 = new Called();
		one2two = i1.connect(ra2, kp2.a, noopListener, connected1, disconnected1, false);

		final Called connected2 = new Called();
		final Called disconnected2 = new Called();
		two2one = i2.connect(ra1, kp1.a, listener, connected2, disconnected2, false);

		receivedSuccessfully = new Called();
		ackReceived = new Called();
	}

	@Test
	public void simpleReliableMessageSend() throws Exception {
		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 100; x++) {
			msgBuilder.writeByte(33);
		}

		sentMessage = msgBuilder.build();

		one2two.send(sentMessage, 1, new TrSentReceivedListenerBasicImpl());

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
		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 100; x++) {
			msgBuilder.writeByte(33);
		}

		sentMessage = msgBuilder.build();

		i2.allowUnsolicitedInbound(listener);

		one2two.send(sentMessage, 1, new TrSentReceivedListenerBasicImpl());

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
		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 2000; x++) {
			msgBuilder.writeByte(33);
		}

		sentMessage = msgBuilder.build();

		//		for (int x = 0; x < 100; x++) {
		//			if (connected1.called && connected2.called) {
		//				break;
		//			}
		//			Thread.sleep(100);
		//		}
		//
		//		Assert.assertTrue(connected1.called && connected2.called);

		one2two.send(sentMessage, 1, new TrSentReceivedListenerBasicImpl());

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
	public void unreliableSimpleMessageSend() throws Exception {
		i1.setSimPercentageLoss(.2);

		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 10; x++) {
			msgBuilder.writeByte(33);
		}

		sentMessage = msgBuilder.build();

		one2two.send(sentMessage, 1, new TrSentReceivedListenerBasicImpl());

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
	public void longUnreliableMessageSend() throws Exception {
		i1.setSimPercentageLoss(.2);

		final ByteArraySegmentBuilder msgBuilder = ByteArraySegment.builder();

		for (int x = 0; x < 2000; x++) {
			msgBuilder.writeByte(33);
		}

		sentMessage = msgBuilder.build();

		one2two.send(sentMessage, 1, new TrSentReceivedListenerBasicImpl());

		for (int x = 0; x < 1000; x++) {
			if (ackReceived.called && receivedSuccessfully.called) {
				break;
			}
			Thread.sleep(500);
		}

		Assert.assertTrue(ackReceived.called);
		Assert.assertTrue(receivedSuccessfully.called);
	}

	public class TrSentReceivedListenerBasicImpl implements TrSentReceivedListener {
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
