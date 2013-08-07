package tahrir.io.net.broadcasts.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import nu.xom.Document;
import tahrir.TrConstants;
import tahrir.io.net.broadcasts.IdentityStore;
import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 07/08/13
 */
public class MentionsFilter implements Predicate<BroadcastMessage> {

    private final IdentityStore identityStore;

    public MentionsFilter(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public boolean apply(@Nullable final BroadcastMessage broadcastMessage) {
        Document doc = broadcastMessage.signedBroadcastMessage.parsedBroadcastMessage.broadcastMessageDocument;
        for (UserIdentity identity : identityStore.getIdentitiesWithLabel(TrConstants.OWN)){
            for(int docCount = 0; docCount<doc.query("//"+TrConstants.FormatInfo.MENTION).size(); docCount++){
                if(identity.getNick().equals(doc.query("//"+TrConstants.FormatInfo.MENTION).get(docCount).getValue())){
                    return true;
                }
            }
        }
        return false;
    }
}

