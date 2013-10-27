package tahrir.io.net.sessions;

import com.google.common.collect.MapMaker;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.TrNodeConfig;
import tahrir.io.net.*;

import java.util.LinkedList;

import static org.mockito.Mockito.*;
import static tahrir.tools.Generic.*;

public class TopologyMaintenanceSessionImplTest {

    // TODO: jacksingleton - refactor TopologyMaintenanceSessionImpl dependencies to simplify all this setup code

    private final TrNode node = mock(TrNode.class);
    private final TrSessionManager sessionManager = mock(TrSessionManager.class);
    private final TrPeerManager peerManager = mock(TrPeerManager.class);

    private void myNodesAddressIs(RemoteNodeAddress myAddress) {
        when(node.getRemoteNodeAddress()).thenReturn(myAddress);
    }

    private void theClosestPeerAddressIs(RemoteNodeAddress closestPeerAddress) {
        when(peerManager.getClosestPeer(anyInt())).thenReturn(closestPeerAddress);
    }

    private TopologyMaintenanceSession sessionManagerReturnsMaintenanceSessionFor(final RemoteNodeAddress address) {
        final TrRemoteConnection connection = mock(TrRemoteConnection.class);
        when(sessionManager.connectionManager.getConnection(eq(address), anyBoolean(), anyString())).thenReturn(connection);

        final TopologyMaintenanceSession maintenanceSession = mock(TopologyMaintenanceSession.class);
        when(sessionManager.getOrCreateRemoteSession(any(Class.class), eq(connection), anyInt())).thenReturn(maintenanceSession);

        return maintenanceSession;
    }

    private void theSenderLocationIs(RemoteNodeAddress senderAddress) {
        TrSessionImpl.sender.set(senderAddress.physicalLocation);
    }

    private void myNodeConfigIs(TrNodeConfig nodeConfig) {
        when(node.getConfig()).thenReturn(nodeConfig);
    }

    private void myTopologyLocationInfoIs(TrPeerManager.TopologyLocationInfo topologyLocationInfo) {
        when(peerManager.getLocInfo()).thenReturn(topologyLocationInfo);
    }

    private void myNodeHasNoConnections() {
        peerManager.peers = new MapMaker().makeMap();
    }

    @BeforeMethod
    public void setupMocks() {
        when(node.getPeerManager()).thenReturn(peerManager);
        sessionManager.connectionManager = mock(TrSessionManager.ConnectionManager.class);
    }

    @AfterMethod
    public void resetMocks() {
        reset(node, sessionManager, peerManager, sessionManager.connectionManager);
    }

    @Test
    public void initiating_topology_maintenance_should_probe_the_closest_peer_for_the_location() {
        // Given
        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);

        final TopologyMaintenanceSession closestPeerSession = sessionManagerReturnsMaintenanceSessionFor(closestPeerAddress);

        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        final Integer locationToFind = genericTopologyLocation();

        // When
        maintenanceSession.startTopologyMaintenance(locationToFind);

        // Then
        verify(closestPeerSession).probeForLocation(
            locationToFind,
            TrConstants.MAINTENANCE_HOPS_TO_LIVE,
            new LinkedList<RemoteNodeAddress>() {{ add(myAddress); }}
        );
    }

    @Test
    public void initiating_topology_maintenance_should_not_send_probe_when_current_node_is_closest_to_location() {
        // Given
        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);
        theClosestPeerAddressIs(myAddress);

        final TopologyMaintenanceSession closestPeerSession = sessionManagerReturnsMaintenanceSessionFor(myAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        final Integer locationToFind = genericTopologyLocation();

        // When
        maintenanceSession.startTopologyMaintenance(locationToFind);

        // Then
        verify(closestPeerSession, never()).probeForLocation(
            locationToFind,
            TrConstants.MAINTENANCE_HOPS_TO_LIVE,
            new LinkedList<RemoteNodeAddress>() {{ add(myAddress); }}
        );
    }

    @Test
    public void receiving_probe_should_send_accept_info_to_sender_when_current_node_is_closest_to_location() {
        // Given
        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);
        theClosestPeerAddressIs(myAddress);

        myNodeHasNoConnections();

        final TrPeerManager.TopologyLocationInfo topologyLocationInfo = genericTopologyLocationInfo();
        myTopologyLocationInfoIs(topologyLocationInfo);

        final TrNodeConfig nodeConfig = new TrNodeConfig();
        myNodeConfigIs(nodeConfig);

        final RemoteNodeAddress senderAddress = genericRemoteNodeAddress();
        final TopologyMaintenanceSession senderSession = sessionManagerReturnsMaintenanceSessionFor(senderAddress);

        theSenderLocationIs(senderAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        // When
        maintenanceSession.probeForLocation(genericTopologyLocation(), TrConstants.MAINTENANCE_HOPS_TO_LIVE,
                new LinkedList<RemoteNodeAddress>() {{ add(senderAddress); }});

        // Then
        verify(senderSession).sendAcceptInfo(myAddress, new LinkedList<RemoteNodeAddress>() {{
            add(senderAddress);
        }});
    }

    @Test
    public void receiving_probe_should_send_my_capabilities_are_to_forwarders_when_current_node_is_closest_to_location() {
        // Given
        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);
        theClosestPeerAddressIs(myAddress);

        myNodeHasNoConnections();

        final TrPeerManager.TopologyLocationInfo topologyLocationInfo = genericTopologyLocationInfo();
        myTopologyLocationInfoIs(topologyLocationInfo);

        final TrNodeConfig nodeConfig = new TrNodeConfig();
        myNodeConfigIs(nodeConfig);

        final RemoteNodeAddress senderAddress = genericRemoteNodeAddress();
        final TopologyMaintenanceSession senderSession = sessionManagerReturnsMaintenanceSessionFor(senderAddress);

        theSenderLocationIs(senderAddress);

        final RemoteNodeAddress forwarderAddressOne = genericRemoteNodeAddress();
        final TopologyMaintenanceSession forwarderSessionOne = sessionManagerReturnsMaintenanceSessionFor(forwarderAddressOne);

        final RemoteNodeAddress forwarderAddressTwo = genericRemoteNodeAddress();
        final TopologyMaintenanceSession forwarderSessionTwo = sessionManagerReturnsMaintenanceSessionFor(forwarderAddressTwo);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        final LinkedList<RemoteNodeAddress> forwarderAddresses = new LinkedList<RemoteNodeAddress>() {{
            add(senderAddress);
            add(forwarderAddressOne);
            add(forwarderAddressTwo);
        }};

        final LinkedList<TopologyMaintenanceSession> forwarderSessions = new LinkedList<TopologyMaintenanceSession>() {{
            add(senderSession);
            add(forwarderSessionOne);
            add(forwarderSessionTwo);
        }};

        // When
        maintenanceSession.probeForLocation(genericTopologyLocation(), TrConstants.MAINTENANCE_HOPS_TO_LIVE, forwarderAddresses);

        // Then
        for (final TopologyMaintenanceSession forwarder : forwarderSessions) {
            verify(forwarder).myCapabilitiesAre(nodeConfig.capabilities, topologyLocationInfo.getLocation());
        }
    }

    @Test
    public void send_accept_info_should_send_my_capabilities_are_to_the_acceptor_when_our_node_is_in_willConnectTo() {
        // Given
        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);

        final RemoteNodeAddress acceptorAddress = genericRemoteNodeAddress();
        final TopologyMaintenanceSession acceptorSession = sessionManagerReturnsMaintenanceSessionFor(acceptorAddress);

        final TrNodeConfig nodeConfig = new TrNodeConfig();
        myNodeConfigIs(nodeConfig);

        final TrPeerManager.TopologyLocationInfo topologyLocationInfo = genericTopologyLocationInfo();
        myTopologyLocationInfoIs(topologyLocationInfo);

        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);
        sessionManagerReturnsMaintenanceSessionFor(closestPeerAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        maintenanceSession.startTopologyMaintenance(genericTopologyLocation()); // we are the initiator

        // When
        maintenanceSession.sendAcceptInfo(acceptorAddress, new LinkedList<RemoteNodeAddress>() {{
            add(myAddress);
        }});

        // Then
        verify(acceptorSession).myCapabilitiesAre(nodeConfig.capabilities, topologyLocationInfo.getLocation());
    }

    @Test
    public void send_accept_info_should_send_accept_info_to_the_prober_when_our_node_is_not_the_initiator() {
        // Given
        final RemoteNodeAddress proberAddress = genericRemoteNodeAddress();
        theSenderLocationIs(proberAddress);
        TopologyMaintenanceSession proberSession = sessionManagerReturnsMaintenanceSessionFor(proberAddress);

        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);
        sessionManagerReturnsMaintenanceSessionFor(closestPeerAddress);

        final RemoteNodeAddress acceptorAddress = genericRemoteNodeAddress();
        final LinkedList<RemoteNodeAddress> willConnectTo = new LinkedList<RemoteNodeAddress>();

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        maintenanceSession.probeForLocation(genericTopologyLocation(), 0, new LinkedList<RemoteNodeAddress>());

        // When
        maintenanceSession.sendAcceptInfo(acceptorAddress, willConnectTo);

        // Then
        verify(proberSession).sendAcceptInfo(acceptorAddress, willConnectTo);
    }

    @Test
    public void my_capabilities_are_should_add_the_acceptor_as_a_peer_when_our_node_is_not_the_acceptor() {
        // Given
        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);

        final TrNodeConfig nodeConfig = new TrNodeConfig();
        myNodeConfigIs(nodeConfig);

        final TrPeerManager.TopologyLocationInfo topologyLocationInfo = genericTopologyLocationInfo();
        myTopologyLocationInfoIs(topologyLocationInfo);

        final RemoteNodeAddress acceptorAddress = genericRemoteNodeAddress();
        sessionManagerReturnsMaintenanceSessionFor(acceptorAddress);

        final RemoteNodeAddress proberAddress = genericRemoteNodeAddress();
        theSenderLocationIs(proberAddress);
        sessionManagerReturnsMaintenanceSessionFor(proberAddress);

        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);
        sessionManagerReturnsMaintenanceSessionFor(closestPeerAddress);

        final TrPeerManager.Capabilities capabilities = new TrNodeConfig().capabilities;
        final Integer topologyLocation = genericTopologyLocation();

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        maintenanceSession.probeForLocation(genericTopologyLocation(), 0, new LinkedList<RemoteNodeAddress>());
        maintenanceSession.sendAcceptInfo(acceptorAddress, new LinkedList<RemoteNodeAddress>() {{ add(myAddress); }});

        // When
        maintenanceSession.myCapabilitiesAre(capabilities, topologyLocation);

        // Then
        verify(peerManager).addByReplacement(acceptorAddress, capabilities, topologyLocation);
    }

    @Test
    public void my_capabilities_are_should_add_the_sender_as_a_peer_when_our_node_is_the_acceptor() {
        // Given
        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);

        final TrNodeConfig nodeConfig = new TrNodeConfig();
        myNodeConfigIs(nodeConfig);

        final TrPeerManager.TopologyLocationInfo topologyLocationInfo = genericTopologyLocationInfo();
        myTopologyLocationInfoIs(topologyLocationInfo);

        final RemoteNodeAddress proberAddress = genericRemoteNodeAddress();
        theSenderLocationIs(proberAddress);
        sessionManagerReturnsMaintenanceSessionFor(proberAddress);

        theClosestPeerAddressIs(myAddress);
        sessionManagerReturnsMaintenanceSessionFor(myAddress);

        myNodeHasNoConnections();

        final TrPeerManager.Capabilities capabilities = new TrNodeConfig().capabilities;
        final Integer topologyLocation = genericTopologyLocation();

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, sessionManager);

        maintenanceSession.probeForLocation(genericTopologyLocation(), 0, new LinkedList<RemoteNodeAddress>() {{ add(proberAddress); }});

        // When
        maintenanceSession.myCapabilitiesAre(capabilities, topologyLocation);

        // Then
        verify(peerManager).addByReplacement(proberAddress, capabilities, topologyLocation);
    }

}
