package tahrir.network.broadcasts.filters;

import com.google.common.base.Predicate;
import tahrir.TrConstants;
import tahrir.identites.IdentityStore;
import tahrir.network.broadcasts.broadcastMessages.BroadcastMessage;

/**
 * Created with IntelliJ IDEA.
 * User: ian
 * Date: 7/22/13
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthorFilter implements Predicate<BroadcastMessage> {
    private final IdentityStore identityStore;

    public AuthorFilter(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public boolean apply(final BroadcastMessage broadcastMessage) {
        return identityStore.getIdentitiesWithLabel(TrConstants.OWN).contains(broadcastMessage.signedBroadcastMessage.getAuthor());
    }
}
