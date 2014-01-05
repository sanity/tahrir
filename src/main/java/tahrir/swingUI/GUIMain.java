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
import tahrir.ui.LoginWindow;
import tahrir.ui.RegisterWindow;
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

                final TrMainWindow mainWindow = new TrMainWindow(testNode, "Default");
                mainWindow.getContent().revalidate();

                /*if(testNode.mbClasses.identityStore.labelsOfUser.keySet().isEmpty()){
                    final RegisterWindow registerWindow = new RegisterWindow(testNode);
                }
                else{
                    final LoginWindow loginWindow = new LoginWindow(testNode);
                }
                */
                //UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
                GUIMain.addTestInformationToNode(testNode);

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        public static void addTestInformationToNode(final TrNode node) {

            UserIdentity user1 = new UserIdentity("user1", TrCrypto.createRsaKeyPair().a, Optional.of(TrCrypto.createRsaKeyPair().b));
            UserIdentity user2 = new UserIdentity("user2", TrCrypto.createRsaKeyPair().a, Optional.of(TrCrypto.createRsaKeyPair().b));
            UserIdentity user3 = new UserIdentity("User3", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            UserIdentity user4 = new UserIdentity("User4", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            UserIdentity user5 = new UserIdentity("User5", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            UserIdentity user6 = new UserIdentity("User6", node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
            UserIdentity user7 = new UserIdentity("Guest", TrCrypto.createRsaKeyPair().a, Optional.<RSAPrivateKey>absent());
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.FOLLOWING, user1);
            node.mbClasses.identityStore.addIdentity(user2);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.FOLLOWING, user7);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user3);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user4);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user5);
            node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN, user6);


            BroadcastMessage fromRand = TrUtils.TestUtils.getBroadcastMessage(node);
            BroadcastMessage fromUser1 = TrUtils.TestUtils.getBroadcastMessageFrom(node, user1);
            BroadcastMessage fromUser2 = TrUtils.TestUtils.getBroadcastMessage(user2, user3, node);
            BroadcastMessage fromUser3 = TrUtils.TestUtils.getBroadcastMessageFrom(node, user3);
            SortedSet<BroadcastMessage> broadcastMessages = Sets.newTreeSet(new BroadcastMessageInbox.BroadcastMessageTimeComparator());
            broadcastMessages.add(fromRand);
            broadcastMessages.add(fromUser1);
            broadcastMessages.add(fromUser2);
            broadcastMessages.add(fromUser3);
            for (BroadcastMessage broadcastMessage : broadcastMessages) {
                node.mbClasses.incomingMbHandler.handleInsertion(broadcastMessage);
            }


        }
}
