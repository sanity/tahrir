package tahrir.ui;

import java.awt.*;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.text.*;

import net.miginfocom.swing.MigLayout;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.Microblog;

public class MicroblogPost {
	private final JPanel contentPanel;

	public MicroblogPost(final Microblog mb, final TrMainWindow mainWindow) {
		contentPanel = new JPanel(new MigLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));

		final TabCreateButton authorNick = new TabCreateButton(mb.authorNick, mb.authorNick, mainWindow);
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
		authorNick.setContentAreaFilled(false);
		authorNick.setText(mb.authorNick);

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
		contentPanel.setPreferredSize(new Dimension(TrConstants.GUI_WIDTH_PX - 40, messageTextPane.getHeight()));
		contentPanel.add(messageTextPane, "wrap, span");
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
