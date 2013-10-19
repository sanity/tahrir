package tahrir.io.net.sessions;

import org.testng.annotations.Test;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.*;
import tahrir.io.net.udpV1.UdpNetworkLocation;
import tahrir.tools.TrUtils;

import java.net.InetAddress;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import static org.mockito.Mockito.*;

public class TopologyMaintenanceSessionImplTest {

    private RemoteNodeAddress buildRemoteNodeAddress() throws Exception {
        final RSAPublicKey pubKey = TrCrypto.createRsaKeyPair().a;
        final PhysicalNetworkLocation location = new UdpNetworkLocation(InetAddress.getByName("127.0.0.1"), new Random().nextInt(1000));
        return new RemoteNodeAddress(location, pubKey);
    }

    @Test
    public void initiating_topology_maintenance_should_probe_the_closest_peer_for_the_location() throws Exception {
        // Given
        final Integer sessionId = 0;

        final TrPeerManager peerManager = mock(TrPeerManager.class);

        final RemoteNodeAddress closestPeerAddress = buildRemoteNodeAddress();
        when(peerManager.getClosestPeer(anyInt())).thenReturn(closestPeerAddress);

        final TrNode node = mock(TrNode.class);

        when(node.getPeerManager()).thenReturn(peerManager);

        final RemoteNodeAddress myAddress = buildRemoteNodeAddress();
        when(node.getRemoteNodeAddress()).thenReturn(myAddress);

        final TrSessionManager sessionManager = mock(TrSessionManager.class);
        sessionManager.connectionManager = mock(TrSessionManager.ConnectionManager.class);

        final TrRemoteConnection closestPeerConnection = mock(TrRemoteConnection.class);
        when(sessionManager.connectionManager.getConnection(eq(closestPeerAddress), anyBoolean(), anyString())).thenReturn(closestPeerConnection);

        final TopologyMaintenanceSession closestPeerSession = mock(TopologyMaintenanceSession.class);
        when(sessionManager.getOrCreateRemoteSession(any(Class.class), any(TrRemoteConnection.class), anyInt())).thenReturn(closestPeerSession);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(sessionId, node, sessionManager);

        final Integer locationToFind = 0;

        // When
        maintenanceSession.startTopologyMaintenance(locationToFind);

        // Then
        verify(closestPeerSession).probeForLocation(
            locationToFind,
            TrConstants.MAINTENANCE_HOPS_TO_LIVE,
            new LinkedList<RemoteNodeAddress>() {{ add(myAddress); }}
        );
    }
}
