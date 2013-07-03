package tahrir.io.net.microblogging;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing.ParsedMicroblogTimeComparator;
import tahrir.io.net.microblogging.filters.AuthorFilter;
import tahrir.io.net.microblogging.filters.ContactsFilter;
import tahrir.io.net.microblogging.filters.MentionFilter;
import tahrir.io.net.microblogging.filters.Unfiltered;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;
import tahrir.tools.TrUtils;
import tahrir.tools.Tuple2;

import java.io.File;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.SortedSet;

public class FilterTest {
	private static final Logger logger = LoggerFactory.getLogger(FilterTest.class);

	private ParsedMicroblog mbForUnfilteredOnly;
	private ParsedMicroblog mbFromUserAMentionsB;
	private ParsedMicroblog mbFromUserB;
	private SortedSet<ParsedMicroblog> microblogs;

    private UserIdentity userA;
    private UserIdentity userB;


	@BeforeClass
	public void setup() {
        userA = new UserIdentity( "UserA", TrCrypto.createRsaKeyPair().a);
        userB = new UserIdentity( "UserB", TrCrypto.createRsaKeyPair().a);


		mbForUnfilteredOnly = TrUtils.TestUtils.getParsedMicroblog();
		mbFromUserAMentionsB = TrUtils.TestUtils.getParsedMicroblog(userA, userB);
		mbFromUserB = TrUtils.TestUtils.getParsedMicroblog(userB);

		microblogs = Sets.newTreeSet(new ParsedMicroblogTimeComparator());
		microblogs.add(mbForUnfilteredOnly);
		microblogs.add(mbFromUserAMentionsB);
		microblogs.add(mbFromUserB);
	}

	@Test
	public void unfilteredTest() {
		final Unfiltered filter = new Unfiltered(microblogs);
		Assert.assertTrue(filter.getMicroblogs().containsAll(microblogs));
	}

	@Test
	public void contactsFilterTest() {
		File testFile = null;
		try {
			testFile = TrUtils.TestUtils.createTempDirectory();
		} catch (IOException e) {
			throw new RuntimeException("Coudn't create temp file", e);
		}
		// in this test we add both user A and B to contacts
        final IdentityStore identityStore= new IdentityStore(testFile);
		identityStore.addIdentity("Following", userA);
        identityStore.addIdentity("Following", userB);

		final ContactsFilter filter = new ContactsFilter(microblogs, identityStore);
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserAMentionsB));
		Assert.assertTrue(filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}

	@Test
	public void authorFilterTest() {
		// in this test we test to see if we can filter microblogs by user A only
		final AuthorFilter filter = new AuthorFilter(microblogs, userA.getPubKey());
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserAMentionsB));
		Assert.assertTrue(!filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}

	@Test
	public void mentionFilterTest() {
		// in this test we see if we can filter messages that mention user B only
		final MentionFilter filter = new MentionFilter(microblogs, userB.getPubKey());
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserAMentionsB));
		Assert.assertTrue(!filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}
}
