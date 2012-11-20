package tahrir.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.SortedSet;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.microblogging.filters.*;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

public class TrMainWindow {
	public static Logger logger = LoggerFactory.getLogger(TrMainWindow.class.getName());

	public TrNode node;

	private final JFrame frame;
	private final JPanel contentPanel;
	private final JTabbedPane tabbedPane;

	private static int TAB_NOT_FOUND = -1;

	public TrMainWindow(final TrNode node) {
		this.node = node;

		contentPanel = new JPanel(new MigLayout());
		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(TrConstants.GUI_WIDTH_PX, TrConstants.GUI_HEIGHT_PX - 120));
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		addTabs(node.mbClasses.mbsForViewing.getMicroblogSet());
		contentPanel.add(tabbedPane, "wrap");

		final JTextPane newPostPane = new JTextPane();
		newPostPane.setBackground(Color.WHITE);
		newPostPane.setBorder(BorderFactory.createLineBorder(TrConstants.SEAGLASS_BLUE));
		newPostPane.setPreferredSize(new Dimension(TrConstants.GUI_WIDTH_PX - 50, 110));
		// the text pane and the button will go into the same cell
		contentPanel.add(newPostPane, "split 2");

		final JButton newPostButton = new JButton("Post");
		contentPanel.add(newPostButton, "align center");

		frame = new JFrame();
		frame.setTitle("Tahrir");
		frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
		frame.setSize(TrConstants.GUI_WIDTH_PX, TrConstants.GUI_HEIGHT_PX);
		frame.setResizable(false);
		frame.setVisible(true);
	}

	public void createClosableTab(final String tabName, final Component tabContents) {
		tabbedPane.insertTab(tabName, null, tabContents, null, tabbedPane.getTabCount());
		final int newTabIndex = tabbedPane.getTabCount() - 1;
		tabbedPane.setSelectedIndex(newTabIndex);
		tabbedPane.setTabComponentAt(newTabIndex, new ClosableTabComponent(tabName));
	}

	private void addTabs(final SortedSet<ParsedMicroblog> sourceForFilters) {
		final MicroblogDisplayPage unfilteredPostPage = new MicroblogDisplayPage(new Unfiltered(sourceForFilters), this);
		final MicroblogDisplayPage followingPostPage = new MicroblogDisplayPage(new ContactsFilter(sourceForFilters, node.mbClasses.contactBook), this);
		final JPanel mentions = new JPanel();
		final MicroblogDisplayPage myPostsPage = new MicroblogDisplayPage(new AuthorFilter(sourceForFilters, node.getRemoteNodeAddress().publicKey), this);
		final JPanel contactBook = new JPanel();
		final JPanel settings = new JPanel();

		mentions.add(new JLabel("This is the mentions page"));
		contactBook.add(new JLabel("This is the contact book page"));
		settings.add(new JLabel("This is the settings page"));

		tabbedPane.addTab(null, createTabIcon("unfiltered.png"), unfilteredPostPage.getContent(), "All posts");
		tabbedPane.addTab(null, createTabIcon("following.png"), followingPostPage.getContent(), "Following posts");
		tabbedPane.addTab(null, createTabIcon("mentions.png"), mentions, "Mentions");
		tabbedPane.addTab(null, createTabIcon("my-posts.png"), myPostsPage.getContent(), "My posts");
		tabbedPane.addTab(null, createTabIcon("contact-book.png"), contactBook, "Contact book");
		tabbedPane.addTab(null, createTabIcon("settings.png"), settings, "Settings");
	}

	private Icon createTabIcon(final String name) {
		final ImageIcon icon = new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + name);
		return icon;
	}

	private class ClosableTabComponent extends JPanel {
		public ClosableTabComponent(final String tabName) {
			super(new MigLayout());

			final JLabel label = new JLabel(tabName);
			label.setFont(new Font("tab", Font.PLAIN, label.getFont().getSize() - 1));
			setForeground(Color.DARK_GRAY);
			add(label);

			final JButton button = new CloseTabButton(this);
			add(button);
		}
	}

	private class CloseTabButton extends JButton implements ActionListener {
		ClosableTabComponent parent;

		public CloseTabButton(final ClosableTabComponent parent) {
			super(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "close-tab.png"));
			this.parent = parent;
			//setRolloverEnabled(true);
			//setRolloverIcon(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "close-tab-hover.png"));
			setFocusable(false);
			setContentAreaFilled(false);
			setToolTipText("Close this tab");

			addActionListener(this);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int tabIndex = tabbedPane.indexOfTabComponent(parent);
			if (tabIndex != TAB_NOT_FOUND) {
				tabbedPane.remove(tabIndex);
			}
		}
	}
}
