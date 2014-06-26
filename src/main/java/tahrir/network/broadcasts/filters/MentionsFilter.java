package tahrir.network.broadcasts.filters;

import com.google.common.base.Predicate;
import nu.xom.Document;
import tahrir.TrConstants;
import tahrir.network.broadcasts.IdentityStore;
import tahrir.network.broadcasts.UserIdentity;
import tahrir.network.broadcasts.broadcastMessages.BroadcastMessage;

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
    public boolean apply(final BroadcastMessage broadcastMessage) {
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

