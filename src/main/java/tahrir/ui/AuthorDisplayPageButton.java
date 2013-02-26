package tahrir.ui;

import tahrir.io.net.microblogging.filters.AuthorFilter;
import tahrir.io.net.microblogging.filters.MicroblogFilter;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.awt.event.ActionEvent;
import java.security.interfaces.RSAPublicKey;
import java.util.SortedSet;

/**
 * Represents a button that, when clicked, will create a tab which will display a MicroblogDisplayPage with an author's
 * microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
@SuppressWarnings("serial")
public class AuthorDisplayPageButton extends TabCreateButton {
	private final RSAPublicKey authorKey;
	private final TrMainWindow mainWindow;

	public AuthorDisplayPageButton(final TrMainWindow mainWindow, RSAPublicKey authorKey, String text) {
		super(mainWindow, text);
		this.authorKey = authorKey;
		this.mainWindow = mainWindow;
		addActionListener(this);
		makeTransparent();
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
		final SortedSet<ParsedMicroblog> mbSet = mainWindow.node.mbClasses.mbsForViewing.getMicroblogSet();
		// lazy creation of filter
		final MicroblogFilter userFilter = new AuthorFilter(mbSet, authorKey);
		final MicroblogDisplayPage mbDisplayPage = new MicroblogDisplayPage(userFilter, mainWindow);
		setContents(mbDisplayPage.getContent());
		super.actionPerformed(arg0);
	}
}