package tahrir.ui;

import tahrir.network.broadcasts.broadcastMessages.BroadcastMessage;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 22/07/13
 */
public class BroadcastMessageModifiedEvent {
    public BroadcastMessage broadcastMessage;
    public enum ModificationType{
        RECEIVED, REMOVE, BOOSTED;
    }
    ModificationType type;
    public BroadcastMessageModifiedEvent(BroadcastMessage broadcastMessage, ModificationType type){
        this.broadcastMessage = broadcastMessage;
        this.type = type;
    }
}
