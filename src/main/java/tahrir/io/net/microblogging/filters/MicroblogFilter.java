package tahrir.io.net.microblogging.filters;

import java.util.*;

import tahrir.io.net.microblogging.containers.MicroblogsForViewing.MicroblogAddedEvent;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing.MicroblogRemovalEvent;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing.ParsedMicroblogTimeComparator;
import tahrir.io.net.microblogging.filters.FilterChangeListener.FilterChangeEvent;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;
import tahrir.tools.TrUtils;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

/**
 * An abstract class for filters, which are a subset of the microblogs available for viewing.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public abstract class MicroblogFilter {
	private TreeSet<ParsedMicroblog> microblogs;
	private final LinkedList<FilterChangeListener> listeners;

	public MicroblogFilter() {
		listeners = Lists.newLinkedList();
	}

	/**
	 * Adds all the current microblogs that pass the filter to this Filter.
	 * 
	 * A messy way of getting around the fact you can't call an abstract method in a constructor.
	 * 
	 * SHOULD BE CALLED IN CONSTRUCTOR OF EVERY SUBCLASS OF MicroblogFilter.
	 */
	public synchronized void initMicroblogStorage(final SortedSet<ParsedMicroblog> initFrom) {
		microblogs = new TreeSet<ParsedMicroblog>(new ParsedMicroblogTimeComparator());
		for (final ParsedMicroblog mb : initFrom) {
			if (passesFilter(mb)) {
				microblogs.add(mb);
			}
		}
		TrUtils.eventBus.register(this);
	}

	@Subscribe
	public synchronized void receiveMicroblog(final MicroblogAddedEvent event) {
		if (passesFilter(event.mb)) {
			microblogs.add(event.mb);
		}
		postToListeners(new FilterChangeEvent(event.mb, false) );
	}

	@Subscribe
	public synchronized void removeMicroblog(final MicroblogRemovalEvent event) {
		microblogs.remove(event.mb);
		postToListeners(new FilterChangeEvent(event.mb, true) );
	}

	public synchronized void registerListener(final FilterChangeListener listener) {
		listeners.add(listener);
	}

	public void removeListener(final FilterChangeListener listener) {
		listeners.remove(listener);
	}

	public synchronized void postToListeners(final FilterChangeEvent event) {
		for (final FilterChangeListener listener : listeners) {
			listener.filterChange(event);
		}
	}

	public synchronized ArrayList<ParsedMicroblog> getMicroblogs() {
		final ArrayList<ParsedMicroblog> mbList = Lists.newArrayList();
		mbList.addAll(microblogs);
		return mbList;
	}

	public synchronized ArrayList<ParsedMicroblog> amInterestedInMicroblogs(final FilterChangeListener listener) {
		// atomic operation of registering and getting the microblogs
		registerListener(listener);
		return getMicroblogs();
	}

	public abstract boolean passesFilter(ParsedMicroblog mb);
}
