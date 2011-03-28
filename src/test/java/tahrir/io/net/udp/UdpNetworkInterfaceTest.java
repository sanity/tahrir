package tahrir.io.net.udp;

import java.net.InetAddress;

import org.testng.annotations.Test;

import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.udp.UdpNetworkInterface.Config;

public class UdpNetworkInterfaceTest {
	@Test
	public void simpleSendReceiveTest() throws Exception {
		final Config conf1 = new Config();
		conf1.listenPort = 3956;
		conf1.maxUpstreamBytesPerSecond = 1024;
		final UdpNetworkInterface i1 = new UdpNetworkInterface(conf1);
		final UdpRemoteAddress ra1 = new UdpRemoteAddress(InetAddress.getLocalHost(), 3956);


		final Config conf2 = new Config();
		conf2.listenPort = 3957;
		conf2.maxUpstreamBytesPerSecond = 1024;
		final UdpNetworkInterface i2 = new UdpNetworkInterface(conf2);
		final UdpRemoteAddress ra2 = new UdpRemoteAddress(InetAddress.getLocalHost(), 3957);

		final byte[] msg = new byte[900];
		for (int x = 0; x < msg.length; x++) {
			msg[x] = 22;
		}

		i2.registerListener(new TrMessageListener<UdpRemoteAddress>() {

			public void received(final TrNetworkInterface<UdpRemoteAddress> iFace, final UdpRemoteAddress sender,
					final byte[] message, final int length) {
				System.out.println("Received from " + sender + " message " + msg);
			}
		});

		i1.sendTo(ra2, msg, 0);
	}
}
