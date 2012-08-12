package tahrir.ui;

import java.awt.*;

import javax.swing.*;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.microblogging.filters.*;

public class TrMainWindow {
	public static Logger logger = LoggerFactory.getLogger(TrMainWindow.class.getName());

	public TrNode node;

	private final JFrame frame;
	private final JPanel contentPanel;
	private final JTabbedPane tabbedPane;

	public TrMainWindow(final TrNode node) {
		this.node = node;

		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(TrConstants.GUI_WIDTH_PX, TrConstants.GUI_HEIGHT_PX));
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		addTabs();

		contentPanel = new JPanel();
		contentPanel.add(tabbedPane);

		frame = new JFrame();
		frame.setTitle("Tahrir");
		frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
		frame.setSize(TrConstants.GUI_WIDTH_PX, TrConstants.GUI_HEIGHT_PX);
		frame.setVisible(true);
	}

	private void addTabs() {
		final MicroblogViewingPage unfilteredPostPage = new MicroblogViewingPage(node, new Unfiltered());
		final MicroblogViewingPage followingPostPage = new MicroblogViewingPage(node, new ContactsFilter(node.mbManager.contactBook));
		final JPanel mentions = new JPanel();
		final MicroblogViewingPage myPostsPage = new MicroblogViewingPage(node, new UserFilter(node.getRemoteNodeAddress().publicKey));
		final JPanel contactBook = new JPanel();
		final JPanel settings = new JPanel();

		mentions.add(new JLabel("This is the mentions page"));
		contactBook.add(new JLabel("This is the contact book page"));
		settings.add(new JLabel("This is the settings page"));

		tabbedPane.addTab(null, createTabIcon("unfiltered.png"), unfilteredPostPage.getContentPane(), "Unfiltered posts");
		tabbedPane.addTab(null, createTabIcon("following.png"), followingPostPage.getContentPane(), "Following posts");
		tabbedPane.addTab(null, createTabIcon("mentions.png"), mentions, "Mentions");
		tabbedPane.addTab(null, createTabIcon("my-posts.png"), myPostsPage.getContentPane(), "My posts");
		tabbedPane.addTab(null, createTabIcon("contact-book.png"), contactBook, "Contact book");
		tabbedPane.addTab(null, createTabIcon("settings.png"), settings, "Settings");
	}

	private Icon createTabIcon(final String name) {
		final ImageIcon icon = new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + name);
		/*
		try {
			icon = new ImageIcon(ImageIO.read(TrMainWindow.class.getResourceAsStream(TrConstants.MAIN_WINDOW_ARTWORK_PATH + name)));
		} catch (final IOException e) {
			e.printStackTrace();
			logger.error("Error loading main tab icon.");
		}
		 */
		return icon;
	}
}
