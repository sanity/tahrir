package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.io.net.microblogging.MicroblogParser.ParsedPart;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.text.DateFormat;
import java.util.Date;

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
		final JLabel postTime = new JLabel(DateParser.parseTime(mb.getMbData().getTimeCreated()));
		postTime.setForeground(Color.GRAY);
		postTime.setFont(new Font("time", Font.PLAIN, postTime.getFont().getSize() - 2));
		content.add(postTime, "wrap, align right, span");
	}

	private void addAuthorButton(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		final AuthorDisplayPageButton authorNick = new AuthorDisplayPageButton(mainWindow,
				mb.getMbData().getAuthorPubKey(), mb.getMbData().getAuthorNick());
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
		content.add(authorNick, "align left");
	}

	private void addTextPane(final ParsedMicroblog mb, TrMainWindow mainWindow) {
		final JTextPane messageTextPane = new JTextPane();
		messageTextPane.setBackground(Color.WHITE);
		messageTextPane.setEditable(false);

		Document doc = messageTextPane.getDocument();
		Style textStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		for (ParsedPart parsedPart : mb.getParsedParts()) {
			if (parsedPart.toSwingComponent(mainWindow).isPresent()) {
				JComponent asComponent = parsedPart.toSwingComponent(mainWindow).get();
				// make the component level with the text
				asComponent.setAlignmentY(0.85f);
				messageTextPane.insertComponent(asComponent);
			} else {
				// insert as text
				try {
					doc.insertString(doc.getLength(), parsedPart.toText(), textStyle);
				} catch (BadLocationException e) {
					throw new RuntimeException("Bad location in message text pane.");
				}
			}
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
}
