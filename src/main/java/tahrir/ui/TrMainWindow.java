package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.net.microblogging.filters.AuthorFilter;
import tahrir.io.net.microblogging.filters.ContactsFilter;
import tahrir.io.net.microblogging.filters.Unfiltered;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedSet;

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
		newPostPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
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
		final MicroblogDisplayPage unfilteredPostPage = new MicroblogDisplayPage(
				new Unfiltered(sourceForFilters), this);
		final MicroblogDisplayPage followingPostPage = new MicroblogDisplayPage(
				new ContactsFilter(sourceForFilters, node.mbClasses.contactBook), this);
		final JPanel mentions = new JPanel();
		final MicroblogDisplayPage myPostsPage = new MicroblogDisplayPage(
				new AuthorFilter(sourceForFilters, node.getRemoteNodeAddress().publicKey), this);
		final JPanel contactBook = new JPanel();
		final JPanel settings = new JPanel();

		mentions.add(new JLabel("This is the mentions page"));
		contactBook.add(new JLabel("This is the contact book page"));
		settings.add(new JLabel("This is the settings page"));

        tabbedPane.addTab("All", unfilteredPostPage.getContent());
        tabbedPane.addTab("Following", followingPostPage.getContent());
        tabbedPane.addTab("Mentions", mentions);
        tabbedPane.addTab("My posts", myPostsPage.getContent());
        tabbedPane.addTab("Contacts", contactBook);
        tabbedPane.addTab("Settings", settings);
	}

	public JPanel getContent() {
		return contentPanel;
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
			setForeground(Color.LIGHT_GRAY);
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
