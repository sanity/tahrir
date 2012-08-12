package tahrir.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

import net.miginfocom.swing.MigLayout;

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

	public void createClosableTab(final String tabName) {
		tabbedPane.insertTab(null, null, null, null, tabbedPane.getTabCount());
		final int newTabIndex = tabbedPane.getTabCount() - 1;
		tabbedPane.setSelectedIndex(newTabIndex);
		tabbedPane.setTabComponentAt(newTabIndex, new ClosableTabComponent(tabName));
	}

	private void addTabs() {
		final MicroblogViewingPage unfilteredPostPage = new MicroblogViewingPage(node, new Unfiltered(), this);
		final MicroblogViewingPage followingPostPage = new MicroblogViewingPage(node, new ContactsFilter(node.mbManager.contactBook), this);
		final JPanel mentions = new JPanel();
		final MicroblogViewingPage myPostsPage = new MicroblogViewingPage(node, new UserFilter(node.getRemoteNodeAddress().publicKey), this);
		final JPanel contactBook = new JPanel();
		final JPanel settings = new JPanel();

		mentions.add(new JLabel("This is the mentions page"));
		contactBook.add(new JLabel("This is the contact book page"));
		settings.add(new JLabel("This is the settings page"));

		tabbedPane.addTab(null, createTabIcon("unfiltered.png"), unfilteredPostPage.getContentPane(), "All posts");
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

	private class ClosableTabComponent extends JPanel {
		public ClosableTabComponent(final String tabName) {
			super(new MigLayout());

			final JLabel label = new JLabel(tabName);

			add(label);

			final JButton button = new CloseTabButton();
			add(button);
		}
	}

	private class CloseTabButton extends JButton implements ActionListener {
		public CloseTabButton() {
			super(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "close-tab.png"));
			//setRolloverEnabled(true);
			//setRolloverIcon(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "close-tab-hover.png"));

			setToolTipText("close this tab");

			//Make the button looks the same for all Laf's
			setUI(new BasicButtonUI());
			//Make it transparent
			setContentAreaFilled(false);

			addActionListener(this);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int i = tabbedPane.indexOfTabComponent(this);
			if (i != -1) {
				tabbedPane.remove(i);
			}
		}
	}
}
