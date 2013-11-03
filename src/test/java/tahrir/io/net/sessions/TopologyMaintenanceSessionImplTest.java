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
    private final TrSessionImpl session = mock(TrSessionImpl.class);
    private final TrPeerManager peerManager = mock(TrPeerManager.class);

    private void myNodesAddressIs(RemoteNodeAddress myAddress) {
        when(node.getRemoteNodeAddress()).thenReturn(myAddress);
    }

    private void theClosestPeerAddressIs(RemoteNodeAddress closestPeerAddress) {
        when(peerManager.getClosestPeer(anyInt())).thenReturn(closestPeerAddress);
    }

    private TopologyMaintenanceSession sessionReturnsMaintenanceSessionFor(final RemoteNodeAddress address) {
        final TopologyMaintenanceSession maintenanceSession = mock(TopologyMaintenanceSession.class);
        when(session.remoteSession(any(Class.class), eq(address.physicalLocation))).thenReturn(maintenanceSession);
        when(session.remoteSession(any(Class.class), eq(address))).thenReturn(maintenanceSession);
        return maintenanceSession;
    }

    private void theSenderLocationIs(RemoteNodeAddress senderAddress) {
        when(session.sender()).thenReturn(senderAddress.physicalLocation);
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
    }

    @Test
    public void initiating_topology_maintenance_should_probe_the_closest_peer_for_the_location() {
        // Given
        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);

        final TopologyMaintenanceSession closestPeerSession = sessionReturnsMaintenanceSessionFor(closestPeerAddress);

        final RemoteNodeAddress myAddress = genericRemoteNodeAddress();
        myNodesAddressIs(myAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

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

        final TopologyMaintenanceSession closestPeerSession = sessionReturnsMaintenanceSessionFor(myAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

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
        final TopologyMaintenanceSession senderSession = sessionReturnsMaintenanceSessionFor(senderAddress);

        theSenderLocationIs(senderAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

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
        final TopologyMaintenanceSession senderSession = sessionReturnsMaintenanceSessionFor(senderAddress);

        theSenderLocationIs(senderAddress);

        final RemoteNodeAddress forwarderAddressOne = genericRemoteNodeAddress();
        final TopologyMaintenanceSession forwarderSessionOne = sessionReturnsMaintenanceSessionFor(forwarderAddressOne);

        final RemoteNodeAddress forwarderAddressTwo = genericRemoteNodeAddress();
        final TopologyMaintenanceSession forwarderSessionTwo = sessionReturnsMaintenanceSessionFor(forwarderAddressTwo);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

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
        final TopologyMaintenanceSession acceptorSession = sessionReturnsMaintenanceSessionFor(acceptorAddress);

        final TrNodeConfig nodeConfig = new TrNodeConfig();
        myNodeConfigIs(nodeConfig);

        final TrPeerManager.TopologyLocationInfo topologyLocationInfo = genericTopologyLocationInfo();
        myTopologyLocationInfoIs(topologyLocationInfo);

        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);
        sessionReturnsMaintenanceSessionFor(closestPeerAddress);

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

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
        TopologyMaintenanceSession proberSession = sessionReturnsMaintenanceSessionFor(proberAddress);

        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);
        sessionReturnsMaintenanceSessionFor(closestPeerAddress);

        final RemoteNodeAddress acceptorAddress = genericRemoteNodeAddress();
        final LinkedList<RemoteNodeAddress> willConnectTo = new LinkedList<RemoteNodeAddress>();

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

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
        sessionReturnsMaintenanceSessionFor(acceptorAddress);

        final RemoteNodeAddress proberAddress = genericRemoteNodeAddress();
        theSenderLocationIs(proberAddress);
        sessionReturnsMaintenanceSessionFor(proberAddress);

        final RemoteNodeAddress closestPeerAddress = genericRemoteNodeAddress();
        theClosestPeerAddressIs(closestPeerAddress);
        sessionReturnsMaintenanceSessionFor(closestPeerAddress);

        final TrPeerManager.Capabilities capabilities = new TrNodeConfig().capabilities;
        final Integer topologyLocation = genericTopologyLocation();

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

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
        sessionReturnsMaintenanceSessionFor(proberAddress);

        theClosestPeerAddressIs(myAddress);
        sessionReturnsMaintenanceSessionFor(myAddress);

        myNodeHasNoConnections();

        final TrPeerManager.Capabilities capabilities = new TrNodeConfig().capabilities;
        final Integer topologyLocation = genericTopologyLocation();

        final TopologyMaintenanceSessionImpl maintenanceSession = new TopologyMaintenanceSessionImpl(genericSessionId(), node, session);

        maintenanceSession.probeForLocation(genericTopologyLocation(), 0, new LinkedList<RemoteNodeAddress>() {{ add(proberAddress); }});

        // When
        maintenanceSession.myCapabilitiesAre(capabilities, topologyLocation);

        // Then
        verify(peerManager).addByReplacement(proberAddress, capabilities, topologyLocation);
    }

}
