package tahrir.ui;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import tahrir.io.net.microblogging.filters.*;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import com.google.common.collect.Lists;

public class MicroblogDisplayPage implements FilterChangeListener {
	private final JScrollPane content;
	private MicroblogTableModel tableModel;
	private final TrMainWindow mainWindow;

	public MicroblogDisplayPage(final MicroblogFilter filter, final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
		synchronized(this) {
			final ArrayList<ParsedMicroblog> initialMicroblogs = filter.amInterestedInMicroblogs(this);
			tableModel = new MicroblogTableModel();
			for (final ParsedMicroblog parsedMb : initialMicroblogs) {
				tableModel.addNewMicroblog(new MicroblogPost(parsedMb, mainWindow));
			}
		}
		final JTable table = new JTable(tableModel);
		content = new JScrollPane(table);
	}

	@Override
	public synchronized void filterChange(final FilterChangeEvent event) {
		final ParsedMicroblog parsedMb = event.mb;
		if (event.removal) {
			tableModel.removeMicroblog(new MicroblogPost(parsedMb, mainWindow));
		} else { // i.e added
			tableModel.addNewMicroblog(new MicroblogPost(parsedMb, mainWindow));
		}
	}

	public JComponent getContent() {
		return content;
	}

	@SuppressWarnings("serial")
	private class MicroblogTableModel extends AbstractTableModel {
		private final ArrayList<MicroblogPost> microblogs;

		public MicroblogTableModel() {
			microblogs = Lists.newArrayList();
		}

		@Override
		public int getColumnCount() {
			return 0;
		}

		@Override
		public int getRowCount() {
			return microblogs.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			return microblogs.get(row);
		}

		@Override
		public void setValueAt(final Object value, final int row, int col) {
			col = 0;
			microblogs.add(row, (MicroblogPost) value);
			fireTableCellUpdated(row, col);
		}

		public void addNewMicroblog(final MicroblogPost mb) {
			setValueAt(mb, 0, 0);
		}

		public void removeMicroblog(final MicroblogPost mb) {
			final int mbIndex = microblogs.indexOf(mb);
			microblogs.remove(mbIndex);
			fireTableCellUpdated(mbIndex, 0);
		}
	}
}
