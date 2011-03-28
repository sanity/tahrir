package tahrir.io.net.udp;

import java.net.InetAddress;
import java.security.interfaces.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentListener;
import tahrir.io.net.TrRemoteConnection.State;
import tahrir.io.net.udp.UdpNetworkInterface.Config;
import tahrir.tools.Tuple2;

public class UdpNetworkInterfaceTest {
	@Test
	public void simpleSendReceiveTest() throws Exception {
		final Config conf1 = new Config();
		conf1.listenPort = 3956;
		conf1.maxUpstreamBytesPerSecond = 1024;

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		final UdpNetworkInterface i1 = new UdpNetworkInterface(conf1, kp1.b);

		final Config conf2 = new Config();
		conf2.listenPort = 3957;
		conf2.maxUpstreamBytesPerSecond = 1024;
		final UdpNetworkInterface i2 = new UdpNetworkInterface(conf2, kp2.b);
		final UdpRemoteAddress ra2 = new UdpRemoteAddress(InetAddress.getLocalHost(), 3957);

		final byte[] msg = new byte[900];
		for (int x = 0; x < msg.length; x++) {
			msg[x] = (byte) (x % 256);
		}

		final boolean[] receivedFlag = new boolean[2];

		i2.registerListener(new TrMessageListener<UdpRemoteAddress>() {

			public void received(final TrNetworkInterface<UdpRemoteAddress> iFace, final UdpRemoteAddress sender,
					final byte[] message, final int length) {
				receivedFlag[0] = true;
				Assert.assertEquals(length, 900);
				for (int x = 0; x < length; x++) {
					Assert.assertEquals(message[x], msg[x]);
				}
			}
		});

		i1.sendTo(ra2, msg, new TrSentListener() {

			public void success() {
				receivedFlag[1] = true;
			}

			public void failure() {
			}
		}, 0);

		Thread.sleep(200);

		Assert.assertTrue(receivedFlag[0]);
		Assert.assertTrue(receivedFlag[1]);

		// i1.shutdown();
		// i2.shutdown();
	}

	@Test
	public void establishConnectionTest() throws Exception {
		final Config conf1 = new Config();
		conf1.listenPort = 3946;
		conf1.maxUpstreamBytesPerSecond = 1024;

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		final UdpNetworkInterface i1 = new UdpNetworkInterface(conf1, kp1.b);
		final UdpRemoteAddress ra1 = new UdpRemoteAddress(InetAddress.getLocalHost(), 3946);

		final Config conf2 = new Config();
		conf2.listenPort = 3947;
		conf2.maxUpstreamBytesPerSecond = 1024;
		final UdpNetworkInterface i2 = new UdpNetworkInterface(conf2, kp2.b);
		final UdpRemoteAddress ra2 = new UdpRemoteAddress(InetAddress.getLocalHost(), 3947);

		final UdpRemoteConnection one2two = i1.connectTo(ra2, kp2.a);

		final UdpRemoteConnection two2one = i2.connectTo(ra1, kp1.a);

		for (int x = 0; x < 100; x++) {
			if (one2two.getState().equals(State.CONNECTED)) {
				break;
			}
			Thread.sleep(100);
		}

		Assert.assertEquals(one2two.getState(), State.CONNECTED);
	}
}
