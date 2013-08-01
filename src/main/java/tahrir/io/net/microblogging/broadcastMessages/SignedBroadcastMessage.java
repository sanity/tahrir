package tahrir.io.net.microblogging.broadcastMessages;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.crypto.TrSignature;
import tahrir.io.net.microblogging.UserIdentity;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 01/08/13
 */
public class SignedBroadcastMessage {
    private ParsedBroadcastMessage parsedBroadcastMessage;
    private UserIdentity author;
    private TrSignature signature;

    public SignedBroadcastMessage(){
        //for serialisation
    }


    public SignedBroadcastMessage(ParsedBroadcastMessage broadcastMessage, UserIdentity author){
        this.parsedBroadcastMessage = broadcastMessage;
        this.author = author;
        try {
            signature = TrCrypto.sign(parsedBroadcastMessage, author.getPvtKey());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SignedBroadcastMessage that = (SignedBroadcastMessage) o;

        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (parsedBroadcastMessage != null ? !parsedBroadcastMessage.equals(that.parsedBroadcastMessage) : that.parsedBroadcastMessage != null)
            return false;
        if (signature != null ? !signature.equals(that.signature) : that.signature != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parsedBroadcastMessage != null ? parsedBroadcastMessage.hashCode() : 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (signature != null ? signature.hashCode() : 0);
        return result;
    }

}
