package tahrir.swingUI;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.containers.BroadcastMessageInbox;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.tools.TrUtils;
import tahrir.ui.TrMainWindow;

import java.security.interfaces.RSAPrivateKey;
import java.util.SortedSet;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 20/07/13
 */
public class GUIMain {

        public static void main(final String[] args) {
            try {
                final TrNode testNode = TrUtils.TestUtils.makeNode(9003, false, false, false, true, 0, 0);

                //UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

                final TrMainWindow mainWindow = new TrMainWindow(testNode);
                mainWindow.getContent().revalidate();
                GUIMain.addTestInformationToNode(testNode);

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        public static void addTestInformationToNode(final TrNode node) {
		/*
		  This is pretty silly: creating parsed broadcastMessages and then, using their information, turn them into
		  their unparsed form and later insert them as if they were from broadcast.
		 */

            UserIdentity user1=new UserIdentity("user1", TrCrypto.createRsaKeyPair().a, Optional.<RSAPrivateKey>absent());
            UserIdentity user2=new UserIdentity("user2", TrCrypto.createRsaKeyPair().a, Optional.<RSAPrivateKey>absent());
            UserIdentity user3 = new UserIdentity("User 3", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            UserIdentity user4 = new UserIdentity("User 4", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            UserIdentity user5 = new UserIdentity("User 5", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            UserIdentity user6 = new UserIdentity("User 6", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.FOLLOWING, user1);
            node.mbClasses.identityStore.addIdentity(user2);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user3);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user4);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user5);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user6);

            ParsedBroadcastMessage fromRand = TrUtils.TestUtils.getParsedMicroblog();
            ParsedBroadcastMessage fromUser1 = TrUtils.TestUtils.getParsedMicroblog(user1);
            ParsedBroadcastMessage fromUser2 = TrUtils.TestUtils.getParsedMicroblog(user2, user1);
            ParsedBroadcastMessage fromUser3 = TrUtils.TestUtils.getParsedMicroblog(user3);
            SortedSet<ParsedBroadcastMessage> parsedMbs = Sets.newTreeSet(new BroadcastMessageInbox.ParsedMicroblogTimeComparator());
            parsedMbs.add(fromRand);
            parsedMbs.add(fromUser1);
            parsedMbs.add(fromUser2);

            for (ParsedBroadcastMessage parsedBroadcastMessage : parsedMbs) {
                String xmlMessage = BroadcastMessageParser.getXML(parsedBroadcastMessage.getParsedParts());
                BroadcastMessage broadcastMessage = new BroadcastMessage(xmlMessage, parsedBroadcastMessage.getMbData());
                node.mbClasses.incomingMbHandler.handleInsertion(broadcastMessage);
            }

            //checking to see if eventBus is working
            String xmlMessage = BroadcastMessageParser.getXML(fromUser3.getParsedParts());
            BroadcastMessage broadcastMessage = new BroadcastMessage(xmlMessage, fromUser3.getMbData());
            node.mbClasses.incomingMbHandler.handleInsertion(broadcastMessage);

        }
}
