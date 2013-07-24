package tahrir.io.net.microblogging.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.IdentityStore;
import tahrir.io.net.microblogging.UserIdentity;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.util.Set;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 23/07/13
 */
public class FollowingFilter  implements Predicate<ParsedMicroblog> {

    private final IdentityStore identityStore;

    public FollowingFilter(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public boolean apply(@Nullable final tahrir.io.net.microblogging.microblogs.ParsedMicroblog parsedMicroblog) {
        return identityStore.getIdentitiesWithLabel(TrConstants.FOLLOWING).contains(parsedMicroblog.getMbData().getAuthor());
    }
}
