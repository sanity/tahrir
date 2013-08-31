package tahrir.io.net.broadcasts.filters;

import com.google.common.base.Predicate;
import tahrir.TrConstants;
import tahrir.io.net.broadcasts.IdentityStore;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 23/07/13
 */
public class FollowingFilter  implements Predicate<BroadcastMessage> {

    private final IdentityStore identityStore;

    public FollowingFilter(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public boolean apply(final BroadcastMessage broadcastMessage) {
        return identityStore.getIdentitiesWithLabel(TrConstants.FOLLOWING).contains(broadcastMessage.signedBroadcastMessage.getAuthor());
    }
}
