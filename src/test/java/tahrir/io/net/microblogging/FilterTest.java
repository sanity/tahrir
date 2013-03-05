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

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.SortedSet;

public class FilterTest {
	private static final Logger logger = LoggerFactory.getLogger(FilterTest.class);

	private ParsedMicroblog mbForUnfilteredOnly;
	private ParsedMicroblog mbFromUserAMentionsB;
	private ParsedMicroblog mbFromUserB;
	private SortedSet<ParsedMicroblog> microblogs;

	private Tuple2<RSAPublicKey, String> userA;
	private Tuple2<RSAPublicKey, String> userB;

	@BeforeClass
	public void setup() {
		userA = new Tuple2<RSAPublicKey, String>(TrCrypto.createRsaKeyPair().a, "UserA");
		userB = new Tuple2<RSAPublicKey, String>(TrCrypto.createRsaKeyPair().a, "UserB");

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
		// in this test we add both user A and B to contacts
		final ContactBook cb = new ContactBook(null);
		cb.addContact(userA.b, userA.a);
		cb.addContact(userB.b, userB.a);

		final ContactsFilter filter = new ContactsFilter(microblogs, cb);
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserAMentionsB));
		Assert.assertTrue(filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}

	@Test
	public void authorFilterTest() {
		// in this test we test to see if we can filter microblogs by user A only
		final AuthorFilter filter = new AuthorFilter(microblogs, userA.a);
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserAMentionsB));
		Assert.assertTrue(!filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}

	@Test
	public void mentionFilterTest() {
		// in this test we see if we can filter messages that mention user B only
		final MentionFilter filter = new MentionFilter(microblogs, userB.a);
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserAMentionsB));
		Assert.assertTrue(!filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}
}
