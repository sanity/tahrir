package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.net.microblogging.UserIdentity;
import tahrir.io.net.microblogging.filters.AuthorFilter;
import tahrir.io.net.microblogging.filters.FollowingFilter;
import tahrir.io.net.microblogging.filters.Unfiltered;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrMainWindow {
	public static Logger logger = LoggerFactory.getLogger(TrMainWindow.class.getName());

	public TrNode node;
    public UserIdentity currentIdentity;

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
		addTabs();
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

    public void setCurrentIdentity(String nick){
        for(UserIdentity identity :node.mbClasses.identityStore.getIdentitiesWithNick(nick)){
            if(identity.hasPvtKey() && identity.getNick().equals(nick)){
                currentIdentity = identity;
            }
        }
    }

	private void addTabs() {
		final BroadcastMessageDisplayPage unfilteredPostPage = new BroadcastMessageDisplayPage(
				new Unfiltered(), this);
		final BroadcastMessageDisplayPage followingPostPage = new BroadcastMessageDisplayPage(
				new FollowingFilter(this.node.mbClasses.identityStore.getIdentitiesWithLabel(TrConstants.FOLLOWING)),this);
		final JPanel mentions = new JPanel();
		final BroadcastMessageDisplayPage myPostsPage = new BroadcastMessageDisplayPage(
				new AuthorFilter(this.node.mbClasses.identityStore.getIdentitiesWithLabel(TrConstants.OWN)), this);
        final ContactBookDisplayPage contactBookDisplayPage = new ContactBookDisplayPage(this);
        final SettingsDisplayPage settingsDisplayPage = new SettingsDisplayPage(this);

		mentions.add(new JLabel("This is the mentions page"));

        tabbedPane.addTab("All", unfilteredPostPage.getContent());
        tabbedPane.addTab("Following", followingPostPage.getContent());
        tabbedPane.addTab("Mentions", mentions);
        tabbedPane.addTab("My posts", myPostsPage.getContent());
        tabbedPane.addTab("Contacts", contactBookDisplayPage.getContent());
        tabbedPane.addTab("Settings", settingsDisplayPage.getContent());
		/*
		TODO: Decide icon or text
		tabbedPane.addTab(null, createTabIcon("unfiltered.png"), unfilteredPostPage.getContent(), "All posts");
		tabbedPane.addTab(null, createTabIcon("following.png"), followingPostPage.getContent(), "Following posts");
		tabbedPane.addTab(null, createTabIcon("mentions.png"), mentions, "Mentions");
		tabbedPane.addTab(null, createTabIcon("my-posts.png"), myPostsPage.getContent(), "My posts");
		tabbedPane.addTab(null, createTabIcon("contact-book.png"), contactBook, "Contact book");
		tabbedPane.addTab(null, createTabIcon("settings.png"), settings, "Settings");
		*/
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
