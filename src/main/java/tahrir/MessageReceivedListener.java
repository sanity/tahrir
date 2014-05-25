package tahrir;

import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;

/**
 * Created by oliverl3 on 3/9/14.
 */
public interface MessageReceivedListener {

    public void messageReceived(BroadcastMessage message);

}
