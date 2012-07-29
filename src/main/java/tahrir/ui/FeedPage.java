package tahrir.ui;

import java.util.*;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import tahrir.io.net.microblogging.MicrobloggingManger.Microblog;

public class FeedPage extends JPanel {
	private final TrMainWindow mainWindow;

	public FeedPage(final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
		setLayout(new MigLayout());
		populate();
	}

	public void populate() {

		final LinkedList<Microblog> microblogs = mainWindow.node.mbManager.microblogFilter.getAllMicroblogs();
		if (microblogs.size() <= 0) {
			this.add(new JLabel("Nothing to display at this time"));
		} else {
			for (final Microblog mb : mainWindow.node.mbManager.microblogFilter.getAllMicroblogs()) {
				this.add(new MicroblogPanel(mb), "wrap");
			}
		}
	}

	public static class MicroblogPanel extends JPanel {
		public MicroblogPanel(final Microblog mb) {
			setLayout(new MigLayout());
			add(new JLabel(mb.authorNick), "growx");
			add(new JLabel(getReadableTimeCreated(mb.timeCreated) + " mins"), "wrap");
			// TODO: change to JTextPane?
			final JTextArea message = new JTextArea(mb.message);
			message.setLineWrap(true);
			message.setEditable(false);
			add(message, "span 20");
		}

		private String getReadableTimeCreated(final long timeInMillis) {
			final Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timeInMillis);
			return Integer.toString(calendar.getTime().getTimezoneOffset());
		}
	}
}
