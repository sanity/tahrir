package tahrir.ui;

import java.awt.Color;
import java.awt.Font;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

/**
 * Represents a microblog in a panel for rendering by the table renderer.
 */
public class MicroblogPostPanel {
	private final JPanel content;
	private final TrMainWindow mainWindow;

	public MicroblogPostPanel(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
		content = new JPanel(new MigLayout());

		addAuthorButton(mb, mainWindow);
		addPostTime(mb);
		addTextPane(mb);
		addVotingButtons();
	}

	private void addPostTime(final ParsedMicroblog mb) {
		final JLabel postTime = new JLabel(DateParser.parseTime(mb.sourceMb.data.timeCreated));
		postTime.setForeground(Color.GRAY);
		postTime.setFont(new Font("time", Font.PLAIN, postTime.getFont().getSize() - 2));
		content.add(postTime, "wrap, align right, span");
	}

	private void addAuthorButton(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		final CreateAuthorPageButton authorNick = new CreateAuthorPageButton(mainWindow,
				mb.sourceMb.data.authorPubKey, mb.sourceMb.data.authorNick);
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
		content.add(authorNick, "align left");
	}

	private void addTextPane(final ParsedMicroblog mb) {
		final JTextPane messageTextPane = new JTextPane();
		messageTextPane.setBackground(Color.WHITE);
		messageTextPane.setEditable(false);

		// insert text into text pane
		final StyledDocument doc = messageTextPane.getStyledDocument();
		try {

		} catch (final BadLocationException e) {
			throw new RuntimeException(e);
		}
		//content.setPreferredSize(new Dimension(TrConstants.GUI_WIDTH_PX - 50, messageTextPane.getHeight()));
		//messageTextPane.setPreferredSize(content.getPreferredSize());
		content.add(messageTextPane, "wrap, span");
	}

	// Sets up a style for a particular mention, settings its specific state such as the text on the
	// button etc. It seems weird the set up a style for every mention but I was stumped for another
	// way.
	private void setStyleForAMention(final Style theDefaultStyle, final StyledDocument doc, final RSAPublicKey authorKey,
			final String authorName) {
		// remove the previous mention
		doc.removeStyle("mention");
		// add the new style for mentions by creating the button and giving it to the doc
		final CreateAuthorPageButton button = new CreateAuthorPageButton(mainWindow, authorKey, "@" + authorName);
		final Style newMentionStyle = doc.addStyle(authorName, theDefaultStyle);
		StyleConstants.setComponent(newMentionStyle, button);
	}

	private void addVotingButtons() {
		//final JButton upvoteButton = new JButton(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "upvote.png"));
		final JButton upvoteButton = new JButton("up");
		setVotingButtonConfigs(upvoteButton, "upvote");
		content.add(upvoteButton, "split 2, span, align right");

		//final JButton downvoteButton = new JButton(new ImageIcon(TrConstants.MAIN_WINDOW_ARTWORK_PATH + "downvote.png"));
		final JButton downvoteButton = new JButton("down");
		setVotingButtonConfigs(downvoteButton, "downvote");
		content.add(downvoteButton);
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

	private interface TextPanelContentsPart {
		public String getText();

		public Style getStyle();
	}

	private class TextPart implements TextPanelContentsPart {
		private final String text;

		public TextPart(final String text) {
			this.text = text;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public Style getStyle() {
			return StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		}
	}

	private class MentionPart implements TextPanelContentsPart {

	}
}
