package tahrir.ui;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import tahrir.TrNode;
import tahrir.io.net.microblogging.*;
import tahrir.io.net.microblogging.filters.MicroblogFilter;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

public class MicroblogViewingPage {
	private final MicroblogFilter filter;

	private final MicroblogContainer microblogContainer;

	private final JPanel content;
	private final JScrollPane scroller;

	private final LoadNewPostsButton loadNewPostsButton;

	private final TrMainWindow mainWindow;

	public MicroblogViewingPage(final TrNode node, final MicroblogFilter filter, final TrMainWindow mainWindow) {
		this.filter = filter;
		this.mainWindow = mainWindow;

		microblogContainer = node.mbManager.getMicroblogContainer();

		content = new JPanel(new MigLayout());

		scroller = new JScrollPane();
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//scroller.setPreferredSize(new Dimension(TrContants))
		scroller.setViewportView(content);

		loadNewPostsButton = new LoadNewPostsButton();

		addMicroblogs();

		microblogContainer.eventBus.register(this);
	}

	@Subscribe
	public void listenForNewMicroblog(final Microblog mb) {
		if (filter.passesFilter(mb)) {
			if (!loadNewPostsButton.presentInGUI) {
				content.removeAll();
				content.add(loadNewPostsButton, "wrap, span");
				loadNewPostsButton.presentInGUI = true;
				addMicroblogs();
			}
		}
	}

	public JComponent getContentPane() {
		return scroller;
	}

	private LinkedList<Microblog> getFilteredMicroblogs() {
		final LinkedList<Microblog> microblogs = Lists.newLinkedList();

		final Iterator<Microblog> iter = microblogContainer.getMicroblogsViewingIter();
		while (iter.hasNext()) {
			final Microblog mb = iter.next();
			if (filter.passesFilter(mb)) {
				microblogs.add(mb);
			}
		}

		return microblogs;
	}

	private void addMicroblogs() {
		final LinkedList<Microblog> microblogs = getFilteredMicroblogs();

		if (microblogs.size() > 0) {
			for (final Microblog mb : microblogs) {
				final MicroblogPost microblogPost = new MicroblogPost(mb, mainWindow);
				content.add(microblogPost.getContentPanel(), "wrap, span");
			}
		} else {
			content.add(new JLabel("Nothing to display at this time"));
		}
	}

	private void readdMicroblogs() {
		scroller.revalidate();
		addMicroblogs();
	}

	@SuppressWarnings("serial")
	private class LoadNewPostsButton extends JButton implements ActionListener {
		public boolean presentInGUI = false;

		public LoadNewPostsButton() {
			setText("Load new posts...");
			addActionListener(this);
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			presentInGUI = false;
			content.removeAll();
			readdMicroblogs();
		}
	}
}
