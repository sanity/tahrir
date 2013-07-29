package tahrir.ui;

import tahrir.io.net.microblogging.microblogs.ParsedBroadcastMessage;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 22/07/13
 */
public class BroadcastMessageModifiedEvent {
    public ParsedBroadcastMessage parsedMb;
    public enum ModificationType{
        RECEIVED, REMOVE;
    }
    ModificationType type;
    public BroadcastMessageModifiedEvent(ParsedBroadcastMessage parsedMb, ModificationType type){
        this.parsedMb = parsedMb;
        this.type = type;
    }
}
