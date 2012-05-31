package tahrir;

public class TrConstants {
	public static final int MAX_UDP_PACKET_SIZE = 1400; // old value was 1450

	public static final int UDP_CONN_INIT_INTERVAL_SECONDS = 2;

	public static final String version = "0.1";

	public static final int DEFAULT_BAOS_SIZE = 2048;

	public static final int DEFAULT_UDP_ACK_TIMEOUT_MS = 1000;

	public static final int UDP_SHORT_MESSAGE_RETRY_ATTEMPTS = 3; // old value was 5

	public static final int UDP_KEEP_ALIVE_DURATION = 7;

	public static final int PUB_PEER_CONCURRENT_ASSIMILATE = 3;

	public static final int MAINTENANCE_HOPS_TO_LIVE = 8;

	public static final int TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE = 3;

	public static final int HOPS_TO_LIVE_RESET = 4;

	public static final int FORWARD_AGAIN_WAIT_SEC = 30;
}
