package tahrir.ui;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import tahrir.TrUI;
import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.filters.AuthorFilter;

import java.awt.event.ActionEvent;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

/**
 * Represents a button that, when clicked, will create a tab which will display a BroadcastMessageDisplayPage with an author's
 * broadcastMessages.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
@SuppressWarnings("serial")
public class AuthorDisplayPageButton extends TabCreateButton {
    private final Optional<UserIdentity> authorIdentity;
	private final TrUI mainWindow;

	public AuthorDisplayPageButton(final TrUI mainWindow, UserIdentity identity) {
		super(mainWindow, identity.getNick());
		this.mainWindow = mainWindow;
        this.authorIdentity = Optional.of(identity);

        addActionListener(this);
		makeTransparent();
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
        final Set<UserIdentity> authors = Sets.newHashSet();
        if(authorIdentity.isPresent())
        authors.add(authorIdentity.get());
		final BroadcastMessageDisplayPage mbDisplayPage = new BroadcastMessageDisplayPage(new AuthorFilter(mainWindow.getNode().mbClasses.identityStore), mainWindow);
		setContents(mbDisplayPage.getContent());
		super.actionPerformed(arg0);
	}
}