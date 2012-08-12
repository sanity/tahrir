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

	private final LinkedList<Microblog> microblogsNotShown;

	private final LoadNewPostsButton loadNewPostsButton;

	public MicroblogViewingPage(final TrNode node, final MicroblogFilter filter) {
		this.filter = filter;
		microblogContainer = node.mbManager.getMicroblogContainer();

		content = new JPanel(new MigLayout());

		scroller = new JScrollPane();
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//scroller.setPreferredSize(new Dimension(TrContants))
		scroller.setViewportView(content);

		microblogsNotShown = Lists.newLinkedList();

		loadNewPostsButton = new LoadNewPostsButton();

		addInitialMicroblogs();
	}

	@Subscribe
	public void listenForNewMicroblog(final Microblog mb) {
		if (filter.passesFilter(mb)) {
			microblogsNotShown.add(mb);
			loadNewPostsButton.setText(String.valueOf(microblogsNotShown.size()));
			if (!loadNewPostsButton.presentInGUI) {
				content.add(loadNewPostsButton, "cell 0 0, wrap, span");
				loadNewPostsButton.presentInGUI = true;
			}
		}
	}

	public JComponent getContentPane() {
		return scroller;
	}

	private LinkedList<Microblog> getFilteredMicroblogs() {
		final LinkedList<Microblog> microblogs = Lists.newLinkedList();

		for (final Microblog mb : microblogContainer.getMicroblogsForViewing()) {
			if (filter.passesFilter(mb)) {
				microblogs.add(mb);
			}
		}

		return microblogs;
	}

	private void addInitialMicroblogs() {
		final LinkedList<Microblog> microblogs = getFilteredMicroblogs();

		if (microblogs.size() > 0) {
			for (final Microblog mb : microblogs) {
				final MicroblogPost microblogPost = new MicroblogPost(mb);
				content.add(microblogPost.getContentPanel(), "wrap, span");
			}
		} else {
			content.add(new JLabel("Nothing to display at this time"));
		}
	}

	private void addNewMicroblogs() {
		// move backwards through list so time order is preserved
		final Iterator<Microblog> iter = microblogsNotShown.descendingIterator();
		while (iter.hasNext()) {
			final Microblog mb = iter.next();
			final MicroblogPost microblogPost = new MicroblogPost(mb);
			content.add(microblogPost.getContentPanel(), "cell 0 0, wrap, span");
		}
	}

	@SuppressWarnings("serial")
	private class LoadNewPostsButton extends JButton implements ActionListener {
		public boolean presentInGUI = false;

		public LoadNewPostsButton() {
			addActionListener(this);
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			removeFromGUI();
			addNewMicroblogs();
		}

		private void removeFromGUI() {
			presentInGUI = false;
			content.remove(this);
			addNewMicroblogs();
		}
	}
}
