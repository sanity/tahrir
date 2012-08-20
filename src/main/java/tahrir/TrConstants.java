package tahrir;

import java.awt.Color;

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

	public static final int WAIT_FROM_FORWARDING_SEC = 30;

	public static final double TOPOLOGY_MAINTENANCE_PRIORITY = 5.0;

	public static final double MICROBLOG_BROADCAST_PRIORITY = 6.0;

	public static final int BROADCAST_INIT_PRIORITY = 0;

	public static final int MAINTENANCE_FREQUENCY_MIN = 1;

	public static final int MAX_MICROBLOGS_FOR_VIEWING = 300;

	public static final int GUI_WIDTH_PX = 600;

	public static final int GUI_HEIGHT_PX = 600;

	public static final String MAIN_WINDOW_ARTWORK_PATH = "artwork/";

	public static final Color SEAGLASS_BLUE = new Color(112, 154, 208);
}
