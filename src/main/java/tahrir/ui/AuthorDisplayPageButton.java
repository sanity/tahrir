package tahrir.ui;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import tahrir.io.net.microblogging.UserIdentity;
import tahrir.io.net.microblogging.filters.AuthorFilter;

import java.awt.event.ActionEvent;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

/**
 * Represents a button that, when clicked, will create a tab which will display a MicroblogDisplayPage with an author's
 * microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
@SuppressWarnings("serial")
public class AuthorDisplayPageButton extends TabCreateButton {
	private final RSAPublicKey authorKey;
    private final UserIdentity authorIdentity;
	private final TrMainWindow mainWindow;

	public AuthorDisplayPageButton(final TrMainWindow mainWindow, RSAPublicKey authorKey, String text) {
		super(mainWindow, text);
		this.authorKey = authorKey;
		this.mainWindow = mainWindow;
        this.authorIdentity = mainWindow.node.identityStore.getIdentityWithPubKey(authorKey).get();
		addActionListener(this);
		makeTransparent();
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
        final Set<UserIdentity> authors = Sets.newHashSet();
        authors.add(authorIdentity);
		final MicroblogDisplayPage mbDisplayPage = new MicroblogDisplayPage(new AuthorFilter(authors), mainWindow);
		setContents(mbDisplayPage.getContent());
		super.actionPerformed(arg0);
	}
}