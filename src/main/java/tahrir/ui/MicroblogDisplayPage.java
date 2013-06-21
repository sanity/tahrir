package tahrir.ui;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import tahrir.TrConstants;
import tahrir.io.net.microblogging.filters.FilterChangeListener;
import tahrir.io.net.microblogging.filters.MicroblogFilter;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import com.google.common.collect.Lists;

import static java.lang.Math.max;

public class MicroblogDisplayPage implements FilterChangeListener {
	private final JComponent content;
	private final MicroblogTableModel tableModel;

	public MicroblogDisplayPage(final MicroblogFilter filter, final TrMainWindow mainWindow) {
		// TODO: a microblog may be received while this is being done and hence be lost we
		// need a more advanced notification system. Google's event bus may handle this.F
		final ArrayList<ParsedMicroblog> initialMicroblogs = filter.amInterestedInMicroblogs(this);
		tableModel = new MicroblogTableModel();
		for (final ParsedMicroblog parsedMb : initialMicroblogs) {
			tableModel.addNewMicroblog(parsedMb);
		}
		final JTable table = new JTable(tableModel);
		final MicroblogRenderer renderer = new MicroblogRenderer(mainWindow);
		// will allow it to fill entire scroll pane
		table.setFillsViewportHeight(true);
		// TODO: change the size as needed


        table.setGridColor(new Color(244,242,242));
		table.setDefaultRenderer(ParsedMicroblog.class, renderer);
        table.setRowHeight(140);
        try {
            table.setSize(new Dimension(TrConstants.GUI_WIDTH_PX, 1000));
            for (int row=0; row<table.getRowCount(); row++) {

                int rowHeight = table.getRowHeight();

                for (int column=0; column<table.getColumnCount(); column++) {


                    Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                    rowHeight = max(rowHeight, comp.getPreferredSize().height);
                }

                table.setRowHeight(row, rowHeight);
            }
        } catch(ClassCastException e) { }
		table.setDefaultEditor(ParsedMicroblog.class, renderer);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(table);
		content = scrollPane;
	}

	@Override
	public void filterChange(final FilterChangeEvent event) {
		final ParsedMicroblog parsedMb = event.mb;
		if (event.removal) {
			tableModel.removeMicroblog(parsedMb);
		} else { // i.e added
			tableModel.addNewMicroblog(parsedMb);
		}
	}

	public JComponent getContent() {
		return content;
	}

	@SuppressWarnings("serial")
	private class MicroblogTableModel extends AbstractTableModel {
		private final ArrayList<ParsedMicroblog> microblogs;

		public MicroblogTableModel() {
			microblogs = Lists.newArrayList();
		}



		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return microblogs.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			return microblogs.get(row);
		}

		public void addNewMicroblog(final ParsedMicroblog mb) {
			microblogs.add(0, mb);
			// This is what updates the GUI with new microblogs.
			// Firing about the entire table here. It seems to be necessary.
			fireTableDataChanged();
		}

		public void removeMicroblog(final ParsedMicroblog mb) {
			final int mbIndex = microblogs.indexOf(mb);
			microblogs.remove(mbIndex);
			// should we fire a cell updated?
			//fireTableCellUpdated(mbIndex, 0);
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return ParsedMicroblog.class;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return null;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			// this allows clicking of buttons etc. in the table
			return true;
		}
	}
}
