package tahrir;

import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.vaadin.TahrirVaadinRequest;

/**
 * Created by oliverl3 on 2/23/14.
 */
public interface InterfaceToTrModel {


    public void broadcastMessage(BroadcastMessage message, BroadcastMessageSentListener broadcastMessageListener);

    public UserIdentity createUserIdentity(String preferredNickname);

    public void addMessageReceivedListener(MessageReceivedListener messageReceivedListener);
}
