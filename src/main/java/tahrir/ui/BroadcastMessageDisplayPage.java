package tahrir.ui;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.io.net.microblogging.broadcastMessages.ParsedBroadcastMessage;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.SortedSet;

public class BroadcastMessageDisplayPage {
	private final JComponent content;
	private final MicroblogTableModel tableModel;
    private final EventBus eventBus;
    private final Predicate<ParsedBroadcastMessage> filter;
    private static final Logger logger = LoggerFactory.getLogger(BroadcastMessageDisplayPage.class.getName());
    public BroadcastMessageDisplayPage(final Predicate<ParsedBroadcastMessage> filter, final TrMainWindow mainWindow) {
        this.filter = filter;
        eventBus = mainWindow.node.mbClasses.eventBus;
		tableModel = new MicroblogTableModel();

		final JTable table = new JTable(tableModel);
		final BroadcastMessageRenderer renderer = new BroadcastMessageRenderer(mainWindow);
		// will allow it to fill entire scroll pane
		table.setFillsViewportHeight(true);
		// TODO: change the size as needed
		table.setRowHeight(110);
        table.setGridColor(new Color(244,242,242));
		table.setDefaultRenderer(ParsedBroadcastMessage.class, renderer);
		table.setDefaultEditor(ParsedBroadcastMessage.class, renderer);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(table);
		content = scrollPane;
        logger.debug("EventBus registered");
        eventBus.register(this);

        final SortedSet<ParsedBroadcastMessage> existingMicroblogs = mainWindow.node.mbClasses.mbsForViewing.getMicroblogSet();

        for (ParsedBroadcastMessage parsedBroadcastMessage : existingMicroblogs) {
            if (filter.apply(parsedBroadcastMessage)) {
                tableModel.addNewMicroblog(parsedBroadcastMessage);
            }
        }
    }

    @Subscribe
    public void modifyMicroblogsDisplay(BroadcastMessageModifiedEvent event){
        if(event.type.equals(BroadcastMessageModifiedEvent.ModificationType.RECEIVED)){
            if(filter.apply(event.parsedMb)){
                tableModel.addNewMicroblog(event.parsedMb);
            }
        }
    }

	public JComponent getContent() {
		return content;
	}

	@SuppressWarnings("serial")
	private class MicroblogTableModel extends AbstractTableModel {
		private final ArrayList<ParsedBroadcastMessage> microblogs;
        // TODO: Use a separate Set so that we can efficiently check whether
        // broadcastMessages are being added more than once

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

		public void addNewMicroblog(final ParsedBroadcastMessage mb) {
            microblogs.add(0, mb);
            // This is what updates the GUI with new broadcastMessages.
            this.fireTableRowsInserted(0, tableModel.getRowCount());
		}

		public void removeMicroblog(final ParsedBroadcastMessage mb) {
			final int mbIndex = microblogs.indexOf(mb);
			microblogs.remove(mbIndex);
			// should we fire a cell updated?
			//fireTableCellUpdated(mbIndex, 0);
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return ParsedBroadcastMessage.class;
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
