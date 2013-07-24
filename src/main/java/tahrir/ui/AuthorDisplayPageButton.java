package tahrir.ui;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import tahrir.io.net.microblogging.UserIdentity;
import tahrir.io.net.microblogging.filters.AuthorFilter;

import java.awt.event.ActionEvent;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

/**
 * Represents a button that, when clicked, will create a tab which will display a BroadcastMessageDisplayPage with an author's
 * microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
@SuppressWarnings("serial")
public class AuthorDisplayPageButton extends TabCreateButton {
	private final RSAPublicKey authorKey;
    private final Optional<UserIdentity> authorIdentity;
	private final TrMainWindow mainWindow;

	public AuthorDisplayPageButton(final TrMainWindow mainWindow, RSAPublicKey authorKey, String text) {
		super(mainWindow, text);
		this.authorKey = authorKey;
		this.mainWindow = mainWindow;
        this.authorIdentity = mainWindow.node.mbClasses.identityStore.getIdentityWithPubKey(authorKey);

        addActionListener(this);
		makeTransparent();
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
        final Set<UserIdentity> authors = Sets.newHashSet();
        if(authorIdentity.isPresent())
        authors.add(authorIdentity.get());
		final BroadcastMessageDisplayPage mbDisplayPage = new BroadcastMessageDisplayPage(new AuthorFilter(mainWindow.node.mbClasses.identityStore), mainWindow);
		setContents(mbDisplayPage.getContent());
		super.actionPerformed(arg0);
	}
}