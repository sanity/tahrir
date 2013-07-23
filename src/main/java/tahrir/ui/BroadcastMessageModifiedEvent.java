package tahrir.ui;

import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 22/07/13
 */
public class BroadcastMessageModifiedEvent {
    public ParsedMicroblog parsedMb;
    public enum ModificationType{
        RECIEVED, REMOVE;
    }
    ModificationType type;
    public BroadcastMessageModifiedEvent(ParsedMicroblog parsedMb, ModificationType type){
        this.parsedMb = parsedMb;
        this.type = type;
    }
}
