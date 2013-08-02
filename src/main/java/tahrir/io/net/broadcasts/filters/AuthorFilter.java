package tahrir.io.net.broadcasts.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import tahrir.TrConstants;
import tahrir.io.net.broadcasts.IdentityStore;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;

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
    public boolean apply(@Nullable final BroadcastMessage broadcastMessage) {
        return identityStore.getIdentitiesWithLabel(TrConstants.OWN).contains(broadcastMessage.signedBroadcastMessage.getAuthor());
    }
}
