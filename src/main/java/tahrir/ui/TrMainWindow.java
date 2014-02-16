package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.TrMain;
import tahrir.TrNode;
import tahrir.TrUI;
import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.SignedBroadcastMessage;
import tahrir.io.net.broadcasts.filters.AuthorFilter;
import tahrir.io.net.broadcasts.filters.FollowingFilter;
import tahrir.io.net.broadcasts.filters.MentionsFilter;
import tahrir.io.net.broadcasts.filters.Unfiltered;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrMainWindow implements TrUI{
	public static Logger logger = LoggerFactory.getLogger(TrMainWindow.class.getName());

	private TrNode node;

	private final JFrame frame;
	private final JPanel contentPanel;
	private final JTabbedPane tabbedPane;

	private static int TAB_NOT_FOUND = -1;


	public TrMainWindow(final TrNode node, String currentUsername){
		this.node = node;
        node.setCurrentIdentity(currentUsername);

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
        newPostButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(node.getConfig().currentUserIdentity.getNick().equals("Default")){
                    if(node.mbClasses.identityStore.labelsOfUser.keySet().isEmpty()){
                        final RegisterWindow registerWindow = new RegisterWindow(node);
                    }
                    else{
                        final LoginWindow loginWindow = new LoginWindow(node);
                    }
                }
                else{
                    String message = newPostPane.getText();
                    //TODO: get the language from config or settings page.
                    ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext(message, "en", node.mbClasses.identityStore, System.currentTimeMillis());
                    SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, node.getConfig().currentUserIdentity);
                    final BroadcastMessage broadcastMessage = new BroadcastMessage(signedBroadcastMessage);
                    node.mbClasses.incomingMbHandler.handleInsertion(broadcastMessage);
                    newPostPane.setText("");
                }
            }
        });
		contentPanel.add(newPostButton, "align center");

		frame = new JFrame();
		frame.setTitle("Tahrir");
		frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
		frame.setSize(TrConstants.GUI_WIDTH_PX, TrConstants.GUI_HEIGHT_PX);
		frame.setResizable(false);
		frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void createClosableTab(final String tabName, final Component tabContents) {
		tabbedPane.insertTab(tabName, null, tabContents, null, tabbedPane.getTabCount());
		final int newTabIndex = tabbedPane.getTabCount() - 1;
		tabbedPane.setSelectedIndex(newTabIndex);
		tabbedPane.setTabComponentAt(newTabIndex, new ClosableTabComponent(tabName));
	}

	private void addTabs() {
		final BroadcastMessageDisplayPage unfilteredPostPage = new BroadcastMessageDisplayPage(
				new Unfiltered(), this);
		final BroadcastMessageDisplayPage followingPostPage = new BroadcastMessageDisplayPage(
				new FollowingFilter(this.node.mbClasses.identityStore),this);
		final BroadcastMessageDisplayPage mentionsPostPage = new BroadcastMessageDisplayPage(
                new MentionsFilter(node.mbClasses.identityStore), this);
		final BroadcastMessageDisplayPage myPostsPage = new BroadcastMessageDisplayPage(
				new AuthorFilter(this.node.mbClasses.identityStore), this);
        final ContactBookDisplayPage contactBookDisplayPage = new ContactBookDisplayPage(this);
        final SettingsDisplayPage settingsDisplayPage = new SettingsDisplayPage(this);

	    tabbedPane.addTab("All", unfilteredPostPage.getContent());
        tabbedPane.addTab("Following", followingPostPage.getContent());
        tabbedPane.addTab("Mentions", mentionsPostPage.getContent());
        tabbedPane.addTab("My posts", myPostsPage.getContent());
        tabbedPane.addTab("Contacts", contactBookDisplayPage.getContent());
        tabbedPane.addTab("Settings", settingsDisplayPage.getContent());
		/*
		TODO: Decide icon or text
		tabbedPane.addTab(null, createTabIcon("unfiltered.png"), unfilteredPostPage.getContentPanel(), "All posts");
		tabbedPane.addTab(null, createTabIcon("following.png"), followingPostPage.getContentPanel(), "Following posts");
		tabbedPane.addTab(null, createTabIcon("mentions.png"), mentions, "Mentions");
		tabbedPane.addTab(null, createTabIcon("my-posts.png"), myPostsPage.getContentPanel(), "My posts");
		tabbedPane.addTab(null, createTabIcon("contact-book.png"), contactBook, "Contact book");
		tabbedPane.addTab(null, createTabIcon("settings.png"), settings, "Settings");
		*/
	}

	public JPanel getContentPanel() {
		return contentPanel;
	}

	private Icon createTabIcon(final String name) {
		final ImageIcon icon = new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + name);
		return icon;
	}

    @Override
    public TrNode getNode() {
        return node;
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
