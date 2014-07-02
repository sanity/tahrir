package tahrir.network.broadcasts.broadcastMessages;

import tahrir.util.crypto.TrCrypto;
import tahrir.util.crypto.TrSignature;
import tahrir.identites.UserIdentity;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 01/08/13
 */
public class SignedBroadcastMessage {
    public ParsedBroadcastMessage parsedBroadcastMessage;
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

    public TrSignature getSignature(){
        return signature;
    }

    public UserIdentity getAuthor(){
        return author;
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
