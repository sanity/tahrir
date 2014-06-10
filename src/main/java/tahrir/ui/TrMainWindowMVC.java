package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import tahrir.*;
import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.SignedBroadcastMessage;
import tahrir.io.net.broadcasts.filters.AuthorFilter;
import tahrir.io.net.broadcasts.filters.FollowingFilter;
import tahrir.io.net.broadcasts.filters.MentionsFilter;
import tahrir.io.net.broadcasts.filters.Unfiltered;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrMainWindowMVC implements InterfaceToTrModel{

    private TrNode node;

    private final JPanel contentPanel;
    private final JTabbedPane tabbedPane;


    public TrMainWindowMVC(final TrNode node){

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

                    //broadcastMessage(message,);
                }
            }
        });
        contentPanel.add(newPostButton, "align center");

        JFrame frame = new JFrame();
        frame.setTitle("Tahrir");
        frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
        frame.setSize(TrConstants.GUI_WIDTH_PX, TrConstants.GUI_HEIGHT_PX);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPanel.revalidate();

    }

    private void addTabs(){


        MessageReceivedListener messageReceivedListener=new MessageReceivedListener() {
            @Override
            public void messageReceived(BroadcastMessage message) {

            }
        };
        addMessageReceivedListener(messageReceivedListener);

        tabbedPane.addTab("All", unfilteredPostPage.getContent());


    /*
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

    @Override
    public void broadcastMessage(BroadcastMessage message, BroadcastMessageSentListener broadcastMessageListener) {

    }

    @Override
    public UserIdentity createUserIdentity(String preferredNickname) {
        return null;
    }

    @Override
    public void addMessageReceivedListener(MessageReceivedListener messageReceivedListener) {

    }
}
