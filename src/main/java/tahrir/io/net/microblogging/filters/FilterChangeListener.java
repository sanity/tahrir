package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

public interface FilterChangeListener {
	public void filterChange(FilterChangeEvent event);

	public static class FilterChangeEvent {
		public ParsedMicroblog mb;
		public boolean removal;

		public FilterChangeEvent(final ParsedMicroblog mbAdded, final boolean removal) {
			this.mb = mbAdded;
			this.removal = removal;
		}
	}
}

