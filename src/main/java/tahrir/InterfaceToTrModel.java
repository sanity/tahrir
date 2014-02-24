package tahrir;

import tahrir.io.net.broadcasts.UserIdentity;

/**
 * Created by oliverl3 on 2/23/14.
 */
public interface InterfaceToTrModel {


   public void broadcastMessage(UserIdentity sender, String message, BroadcastMessageListener broadcastMessageListener);

    public UserIdentity createUserIdentity(String preferredNickname);

    public void addMessageReceivedListener(MessageReceivedListener messageReceivedListener);
}
