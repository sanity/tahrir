package tahrir.io.net.microblogging.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import tahrir.io.net.microblogging.UserIdentity;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.util.Set;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 23/07/13
 */
public class FollowingFilter  implements Predicate<ParsedMicroblog> {
    Set<UserIdentity> following;
    public FollowingFilter(Set<UserIdentity> following) {
        this.following = following;
    }

    @Override
    public boolean apply(@Nullable final tahrir.io.net.microblogging.microblogs.ParsedMicroblog parsedMicroblog) {
        return following.contains(parsedMicroblog.getMbData().getAuthor());
    }
}
