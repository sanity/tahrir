package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

public class FilterTest {
	private static final Logger logger = LoggerFactory.getLogger(FilterTest.class);

	private ParsedMicroblog mbForUnfilteredOnly;
	private ParsedMicroblog mbFromUserA;
	private ParsedMicroblog mbFromUserB;
	private ParsedMicroblog anotherMbForUnfilteredOnly;
	private SortedSet<ParsedMicroblog> microblogs;

	private RSAPublicKey userAKey;
	private RSAPublicKey userBKey;

	@BeforeClass
	public void setup() {
		userAKey = TrCrypto.createRsaKeyPair().a;
		userBKey = TrCrypto.createRsaKeyPair().a;

		final String stringForUnfilteredOnlyMb = "<mb><txt>Aenean venenatis vulputate magna, a.</txt>" + createRandomMentionString() + "</mb>";
		final String stringForUserA = "<mb><txt>Praesent auctor dapibus ante</txt>" + mentionUserB() + "<txt>, id venenatis lacus posuere vel. Cras.</txt></mb>";
		final String stringForUserB = "<mb><txt>Suspendisse potenti. Aliquam erat volutpat. Aenean mollis hendrerit.</txt></mb>";
		final String stringForAnotherMbForUnfilteredOnly = "<mb><txt>Vivamus lacinia volutpat feugiat. Nulla iaculis.</txt>" + createRandomMentionString() + "</mb>";

		mbForUnfilteredOnly = createParsedMicroblog("random_user0", TrCrypto.createRsaKeyPair().a, stringForUnfilteredOnlyMb);
		mbFromUserA = createParsedMicroblog("userA", userAKey, stringForUserA);
		mbFromUserB = createParsedMicroblog("userB", userBKey, stringForUserB);
		anotherMbForUnfilteredOnly = createParsedMicroblog("random_user1", TrCrypto.createRsaKeyPair().a, stringForAnotherMbForUnfilteredOnly);

		microblogs = new TreeSet<ParsedMicroblog>(new ParsedMicroblogTimeComparator());
		microblogs.add(mbForUnfilteredOnly);
		microblogs.add(mbFromUserA);
		microblogs.add(mbFromUserB);
		microblogs.add(anotherMbForUnfilteredOnly);
	}

	@Test
	public void unfilteredTest() {
		final Unfiltered filter = new Unfiltered(microblogs);
		Assert.assertTrue(filter.getMicroblogs().containsAll(microblogs));
	}

	@Test
	public void contactsFilterTest() {
		// in this test we add both user A and B to contacts
		final ContactBook cb = new ContactBook();
		cb.addContact("userA", userAKey);
		cb.addContact("userB", userBKey);

		final ContactsFilter filter = new ContactsFilter(microblogs, cb);
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserA));
		Assert.assertTrue(filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}

	@Test
	public void authorFilterTest() {
		// in this test we test to see if we can filter microblogs by user A only
		final AuthorFilter filter = new AuthorFilter(microblogs, userAKey);
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserA));
		Assert.assertTrue(!filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(mbForUnfilteredOnly));
	}

	@Test
	public void mentionFilterTest() {
		// in this test we see if we can filter messages that mention user B only
		final MentionFilter filter = new MentionFilter(microblogs, userBKey);
		final List<ParsedMicroblog> filterMbs = filter.getMicroblogs();

		Assert.assertTrue(filterMbs.contains(mbFromUserA));
		Assert.assertTrue(!filterMbs.contains(mbFromUserB));
		Assert.assertTrue(!filterMbs.contains(anotherMbForUnfilteredOnly));
	}

	private String createRandomMentionString() {
		final RSAPublicKey pubKey = TrCrypto.createRsaKeyPair().a;
		return "<mention>" + ParsedMicroblog.convertToMentionBytesString(pubKey) + "</mention>";
	}

	private String mentionUserB() {
		return "<mention>" + ParsedMicroblog.convertToMentionBytesString(userBKey) + "</mention>";
	}

	private ParsedMicroblog createParsedMicroblog(final String authorNick, final RSAPublicKey pubKey, final String msg) {
		final BroadcastMicroblog sourceMb = new BroadcastMicroblog(0, authorNick, pubKey, msg, System.currentTimeMillis());
		try {
			return new ParsedMicroblog(sourceMb);
		} catch(final Exception e) {
			logger.error(e.toString());
		}
		return null;
	}
}
