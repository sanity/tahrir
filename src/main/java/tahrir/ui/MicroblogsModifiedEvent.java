package tahrir.ui;

import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 22/07/13
 */
public class MicroblogsModifiedEvent {
    public ParsedMicroblog parsedMb;
    public enum ModificationType{
        ADD, REMOVE;
    }
    ModificationType type;
    public MicroblogsModifiedEvent(ParsedMicroblog parsedMb, ModificationType type){
        this.parsedMb = parsedMb;
        this.type = type;
    }
}
