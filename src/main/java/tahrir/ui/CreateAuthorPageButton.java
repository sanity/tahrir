package tahrir.ui;

import java.awt.event.ActionEvent;
import java.security.interfaces.RSAPublicKey;
import java.util.SortedSet;

import tahrir.io.net.microblogging.filters.AuthorFilter;
import tahrir.io.net.microblogging.filters.MicroblogFilter;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

/**
 * Represents a button that, when clicked, will create a tab which display a specified user's
 * microblogs.
 * 
 * Note: the fact the class exists is probably bad design, it works for now.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
@SuppressWarnings("serial")
public class CreateAuthorPageButton extends TabCreateButton {
	private final RSAPublicKey authorKey;
	private final TrMainWindow mainWindow;

	public CreateAuthorPageButton(final TrMainWindow mainWindow, final RSAPublicKey authorKey, final String authorName) {
		super(mainWindow, authorName);
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

	public void makeTransparent() {
		setOpaque(false);
		setFocusable(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
	}
}