package tahrir.io.net.broadcasts.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import tahrir.TrConstants;
import tahrir.io.net.broadcasts.IdentityStore;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 23/07/13
 */
public class FollowingFilter  implements Predicate<ParsedBroadcastMessage> {

    private final IdentityStore identityStore;

    public FollowingFilter(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public boolean apply(@Nullable final ParsedBroadcastMessage parsedBroadcastMessage) {
        return identityStore.getIdentitiesWithLabel(TrConstants.FOLLOWING).contains(parsedBroadcastMessage.getMbData().getAuthor());
    }
}
