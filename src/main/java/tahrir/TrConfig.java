package tahrir;

import tahrir.io.net.udpV1.UdpNetworkInterface.Config;
import tahrir.peerManager.*;
import tahrir.peerManager.TrPeerManager.Capabilities;
import tahrir.tools.TrUtils;

public class TrConfig {
	public String privateNodeId = "myprivnodeid.dat";
	public String publicNodeId = "mypubnodeid.dat";
	public String publicNodeIdsDir = "publicPeers";
	TrPeerManager.Config peers = new TrPeerManager.Config();
	public int udpListenPort = TrUtils.rand.nextInt(50000) + 10000;
	public String localHostName = null;
	public Capabilities capabilities = new Capabilities();
	public Config udp = new Config();
}
