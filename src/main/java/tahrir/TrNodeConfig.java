package tahrir;

import com.google.common.base.Optional;
import tahrir.network.TrPeerManager;
import tahrir.util.crypto.TrCrypto;
import tahrir.network.TrPeerManager.Capabilities;
import tahrir.network.broadcasts.UserIdentity;
import tahrir.transport.messaging.udpV1.UdpNetworkInterface.UNIConfig;

public class TrNodeConfig {
	public String privateNodeId = "myprivnodeid.dat";
	public String publicNodeId = "mypubnodeid.dat";
	public String publicNodeIdsDir = "publicPeers";
	public String contactBookDir = "contact_book";
	public String publicKeyChars = "public_key_chars.dat";
	public String contacts = "contacts.dat";
	public TrPeerManager.Config peers = new TrPeerManager.Config();
	// public int udpListenPort = TrUtils.rand.nextInt(50000) + 10000;
	public String localHostName = null;
	public Capabilities capabilities = new Capabilities();
	public UNIConfig udp = new UNIConfig();
    public UserIdentity currentUserIdentity = new UserIdentity("Default", TrCrypto.createRsaKeyPair().a, Optional.of(TrCrypto.createRsaKeyPair().b));
}
