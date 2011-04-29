package tahrir.io.net.sessions;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.*;

import tahrir.*;
import tahrir.TrNode.PublicNodeId;
import tahrir.io.net.*;
import tahrir.tools.Persistence.Modified;
import tahrir.tools.Persistence.ModifyBlock;

public class AssimilateSessionImpl extends TrSessionImpl implements AssimilateSession {

	public AssimilateSessionImpl(final Integer sessionId, final TrNode node, final TrNet trNet) {
		super(sessionId, node, trNet);
	}

	Set<File> failedPubIdFiles = new ConcurrentSkipListSet<File>();

	public void assimilateViaPubPeers(final int concurrency) {
		// Get a suitable public peer
		final Collection<File> pubIdFiles = node.getPublicNodeIdFiles();
		final ArrayList<File> shuffledPubIdFiles = new ArrayList<File>(pubIdFiles);
		Collections.shuffle(shuffledPubIdFiles);
		final ConcurrentLinkedQueue<File> pubIdFilesQueue = new ConcurrentLinkedQueue<File>();
		for (int x = 0; x < concurrency; x++) {
			assimilateViaPubPeer(pubIdFilesQueue);
		}
	}

	private void assimilateViaPubPeer(final ConcurrentLinkedQueue<File> pubIdFilesQueue) {

	}

	public void requestNewConnection(final RSAPublicKey requestorPubkey) {
		requestNewConnection(null, requestorPubkey);
	}

	public void requestNewConnection(TrRemoteAddress requestor_, final RSAPublicKey requestorPubkey) {
		final TrRemoteAddress sndr = sender();
		final AssimilateSession senderAS = this.remoteSession(AssimilateSession.class, connection(sndr));
		if (requestor_ == null) {
			senderAS.yourAddressIs(sndr);
			requestor_ = sndr;
		}
		final TrRemoteAddress requestor = requestor_;
		if (node.peerManager.peers.size() < node.peerManager.config.maxPeers) {

		}
	}

	public void yourAddressIs(final TrRemoteAddress address) {
		node.modifyPublicNodeId(new ModifyBlock<PublicNodeId>() {

			public void run(final PublicNodeId publicNodeId, final Modified modified) {
				// We don't want anyone to be able to have us change
				// what we think our remove address is, only the
				// first.
				if (publicNodeId.address != null) {
					modified.notModified();
					return;
				}
				publicNodeId.address = address;
			}
		});
	}
}
