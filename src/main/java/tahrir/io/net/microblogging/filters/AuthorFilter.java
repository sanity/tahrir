package tahrir.io.net.microblogging.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.IdentityStore;
import tahrir.io.net.microblogging.microblogs.ParsedBroadcastMessage;

/**
 * Created with IntelliJ IDEA.
 * User: ian
 * Date: 7/22/13
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthorFilter implements Predicate<ParsedBroadcastMessage> {
    private final IdentityStore identityStore;

    public AuthorFilter(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public boolean apply(@Nullable final ParsedBroadcastMessage parsedBroadcastMessage) {
        return identityStore.getIdentitiesWithLabel(TrConstants.OWN).contains(parsedBroadcastMessage.getMbData().getAuthor());
    }
}
