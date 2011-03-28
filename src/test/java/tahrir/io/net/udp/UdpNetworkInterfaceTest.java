package tahrir.io.net.udp;

import java.net.InetAddress;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentListener;
import tahrir.io.net.udp.UdpNetworkInterface.Config;

public class UdpNetworkInterfaceTest {
	@Test
	public void simpleSendReceiveTest() throws Exception {
		final Config conf1 = new Config();
		conf1.listenPort = 3956;
		conf1.maxUpstreamBytesPerSecond = 1024;
		final UdpNetworkInterface i1 = new UdpNetworkInterface(conf1);

		final Config conf2 = new Config();
		conf2.listenPort = 3957;
		conf2.maxUpstreamBytesPerSecond = 1024;
		final UdpNetworkInterface i2 = new UdpNetworkInterface(conf2);
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
	}
}
