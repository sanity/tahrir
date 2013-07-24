package tahrir.io.net.microblogging.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.IdentityStore;
import tahrir.io.net.microblogging.UserIdentity;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ian
 * Date: 7/22/13
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthorFilter implements Predicate<ParsedMicroblog> {
    private final IdentityStore identityStore;

    public AuthorFilter(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public boolean apply(@Nullable final tahrir.io.net.microblogging.microblogs.ParsedMicroblog parsedMicroblog) {
        return identityStore.getIdentitiesWithLabel(TrConstants.OWN).contains(parsedMicroblog.getMbData().getAuthor());
    }
}
