package tahrir.ui;

import java.awt.*;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.text.*;

import net.miginfocom.swing.MigLayout;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.filters.UserFilter;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

public class MicroblogPost {
	private final JPanel contentPanel;

	public MicroblogPost(final MicroblogForBroadcast mb, final TrMainWindow mainWindow) {
		contentPanel = new JPanel(new MigLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(BorderFactory.createRaisedSoftBevelBorder());

		// create a button which bring up tab with author's microblogs when clicked
		final MicroblogViewingPage authorViewingPage = new MicroblogViewingPage(null, new UserFilter(mb.publicKey), mainWindow);
		final TabCreateButton authorNick = new TabCreateButton(mainWindow, mb.authorNick, authorViewingPage.getContentPane());
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
		contentPanel.add(authorNick, "align left");

		final JLabel postTime = new JLabel(DateParser.parseTime(mb.timeCreated));
		postTime.setForeground(Color.GRAY);
		postTime.setFont(new Font("time", Font.PLAIN, postTime.getFont().getSize() - 2));
		contentPanel.add(postTime, "wrap, align right, span");

		final JTextPane messageTextPane = new JTextPane();
		messageTextPane.setBackground(Color.WHITE);
		messageTextPane.setEditable(false);

		// insert text into text pane
		final StyledDocument doc = messageTextPane.getStyledDocument();
		try {
			doc.insertString(0, mb.message, null);
		} catch (final BadLocationException e) {
			throw new RuntimeException(e);
		}
		contentPanel.setPreferredSize(new Dimension(TrConstants.GUI_WIDTH_PX - 50, messageTextPane.getHeight()));
		messageTextPane.setPreferredSize(contentPanel.getPreferredSize());
		contentPanel.add(messageTextPane, "wrap, span");

		final JButton upvoteButton = new TransparentButton(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "upvote.png"), "upvote");
		contentPanel.add(upvoteButton, "split 2, span, align right");
		final JButton downvoteButton = new TransparentButton(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "downvote.png"), "downvote");
		contentPanel.add(downvoteButton);
	}

	public JPanel getContentPanel() {
		return contentPanel;
	}

	private static class DateParser {
		private static DateFormat dateFormater = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

		public static String parseTime(final long time) {
			final Date date = new Date(time);
			return dateFormater.format(date);
		}
	}
}
