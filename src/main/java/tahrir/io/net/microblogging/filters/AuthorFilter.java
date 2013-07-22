package tahrir.io.net.microblogging.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
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
    private final Set<UserIdentity> authors;

    public AuthorFilter(Set<UserIdentity> authors) {

        this.authors = authors;
    }

    @Override
    public boolean apply(@Nullable final tahrir.io.net.microblogging.microblogs.ParsedMicroblog parsedMicroblog) {
        return authors.contains(parsedMicroblog.getMbData().getAuthor());
    }
}
