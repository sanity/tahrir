package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;
import tahrir.tools.Tuple2;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Represents a microblog in a panel for rendering by the table renderer.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogPostPanel {
	private static final Logger logger = LoggerFactory.getLogger(MicroblogPostPanel.class);

	private final JPanel content;
	private final TrMainWindow mainWindow;

	public MicroblogPostPanel(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
		content = new JPanel(new MigLayout());

		addAuthorButton(mb, mainWindow);
		addPostTime(mb);
		addTextPane(mb, mainWindow);
		addVotingButtons();
	}

	public JComponent getContent() {
		return content;
	}

	private void addPostTime(final ParsedMicroblog mb) {
		final JLabel postTime = new JLabel(DateParser.parseTime(mb.mbData.timeCreated));
		postTime.setForeground(Color.GRAY);
		postTime.setFont(new Font("time", Font.PLAIN, postTime.getFont().getSize() - 2));
		content.add(postTime, "wrap, align right, span");
	}

	private void addAuthorButton(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		final AuthorDisplayPageButton authorNick = new AuthorDisplayPageButton(mainWindow,
				mb.mbData.authorPubKey, mb.mbData.authorNick);
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
		content.add(authorNick, "align left");
	}

	private void addTextPane(final ParsedMicroblog mb, TrMainWindow mainWindow) {
		final JTextPane messageTextPane = new JTextPane();
		messageTextPane.setBackground(Color.WHITE);
		messageTextPane.setEditable(false);

		// insert text into text pane
		final StyledDocument doc = messageTextPane.getStyledDocument();
		doc.addStyle("base", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
		// first add all the parts to an array so that we can easily add, based on their location, to the JTextPane
		TextPanelContentsPart[] textPaneParts = new TextPanelContentsPart[mb.getElementCount()];
		// prepare the mentions
		for (Map.Entry<Tuple2<RSAPublicKey, String>, Integer> entry : mb.getMentions().entrySet()) {
			Integer location = entry.getValue();
			String alias = entry.getKey().b;
			RSAPublicKey key = entry.getKey().a;
			TextPanelContentsPart part = new MentionPart(location.toString(), alias, key, doc, mainWindow);
			textPaneParts[location] = part;
		}
		// prepare the plain text
		for (Map.Entry<String, Integer> entry: mb.getText().entrySet()) {
			Integer location = entry.getValue();
			textPaneParts[location] = new TextPanelContentsPart(entry.getKey());
		}
		// now add the actual information to the text pane
		try {
			// parts were inserted in array in order
			for (TextPanelContentsPart part : textPaneParts) {
				doc.insertString(doc.getLength(), part.getText(), doc.getStyle(part.getStyleName()));
			}
		} catch (final BadLocationException e) {
			logger.error("Problem with inserting text into the text pane");
			throw new RuntimeException(e);
		}
		content.add(messageTextPane, "wrap, span");
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

	private class TextPanelContentsPart {
		private String text;

		public TextPanelContentsPart(String text) {
			this.text = text;
		}

		public String getStyleName() {
			return "base";
		}

		public String getText() {
			return text;
		}
		
		/*
		 This class could be made implement a comparator to avoid messiness of putting into an array in addTextPane()
		 */
	}

	private class MentionPart extends TextPanelContentsPart {
		private String styleName;

		public MentionPart(String toAppendToStyleName, String aliasOfMentioned, RSAPublicKey publicKeyOfMentioned,
				StyledDocument doc, TrMainWindow mainWindow) {
			// just a blank space as text, the other text is on the button
			super(" ");
			AuthorDisplayPageButton button = new AuthorDisplayPageButton(mainWindow, publicKeyOfMentioned,
					aliasOfMentioned);
			styleName = new String("mention" + toAppendToStyleName);
			Style s = doc.addStyle(styleName, doc.getStyle("default"));
			StyleConstants.setComponent(s, button);
		}

		@Override
		public String getStyleName() {
			return styleName;
		}
	}
}
