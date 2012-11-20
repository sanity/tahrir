package tahrir.ui;

import java.awt.*;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.text.*;

import net.miginfocom.swing.MigLayout;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

/**
 * Represents a microblog for display in the GUI.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogPost extends JPanel {

	public MicroblogPost(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		super(new MigLayout());
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createRaisedSoftBevelBorder());

		addPostTime(mb, mainWindow);
		addAuthorButton(mb, mainWindow);
		addTextPane(mb);
		addVotingButtons();
	}

	private void addPostTime(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		final JLabel postTime = new JLabel(DateParser.parseTime(mb.sourceMb.timeCreated));
		postTime.setForeground(Color.GRAY);
		postTime.setFont(new Font("time", Font.PLAIN, postTime.getFont().getSize() - 2));
		add(postTime, "wrap, align right, span");
	}

	private void addAuthorButton(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		final CreateAuthorPageButton authorNick = new CreateAuthorPageButton(mainWindow, mb.sourceMb.publicKey, mb.sourceMb.authorNick);
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
		add(authorNick, "align left");
	}

	private void addTextPane(final ParsedMicroblog mb) {
		final JTextPane messageTextPane = new JTextPane();
		messageTextPane.setBackground(Color.WHITE);
		messageTextPane.setEditable(false);

		// insert text into text pane
		final StyledDocument doc = messageTextPane.getStyledDocument();
		try {
			doc.insertString(0, mb.sourceMb.message, null);
		} catch (final BadLocationException e) {
			throw new RuntimeException(e);
		}
		setPreferredSize(new Dimension(TrConstants.GUI_WIDTH_PX - 50, messageTextPane.getHeight()));
		messageTextPane.setPreferredSize(getPreferredSize());
		add(messageTextPane, "wrap, span");
	}

	private void addVotingButtons() {
		final JButton upvoteButton = new JButton(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "upvote.png"));
		setVotingButtonConfigs(upvoteButton, "upvote");
		add(upvoteButton, "split 2, span, align right");

		final JButton downvoteButton = new JButton(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "downvote.png"));
		setVotingButtonConfigs(downvoteButton, "downvote");
		add(downvoteButton);
	}

	private void setVotingButtonConfigs(final JButton button, final String tooltip) {
		button.setToolTipText(tooltip);
		button.setFocusable(false);
		button.setContentAreaFilled(false);
	}

	private static class DateParser {
		private static DateFormat dateFormater = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

		public static String parseTime(final long time) {
			final Date date = new Date(time);
			return dateFormater.format(date);
		}
	}
}
