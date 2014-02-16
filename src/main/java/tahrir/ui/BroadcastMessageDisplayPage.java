package tahrir.ui;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrUI;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.vaadin.TestVaadinUI;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.SortedSet;

public class BroadcastMessageDisplayPage {
	private final JComponent content;
	private final MicroblogTableModel tableModel;
    private final EventBus eventBus;
    private final Predicate<BroadcastMessage> filter;
    private static final Logger logger = LoggerFactory.getLogger(BroadcastMessageDisplayPage.class.getName());

    public BroadcastMessageDisplayPage(final Predicate<BroadcastMessage> filter, final TrUI mainWindow) {
        this.filter = filter;
        eventBus = mainWindow.getNode().mbClasses.eventBus;
		tableModel = new MicroblogTableModel();

		final JTable table = new JTable(tableModel);
		final BroadcastMessageRenderer renderer = new BroadcastMessageRenderer(mainWindow);
		// will allow it to fill entire scroll pane
		table.setFillsViewportHeight(true);
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

        final SortedSet<BroadcastMessage> existingMicroblogs = mainWindow.getNode().mbClasses.mbsForViewing.getMicroblogSet();

        for (BroadcastMessage broadcastMessage : existingMicroblogs) {
            if (filter.apply(broadcastMessage)) {
                tableModel.addNewMicroblog(broadcastMessage);
            }
        }
    }
/*
    public BroadcastMessageDisplayPage(final Predicate<BroadcastMessage> filter, final TestVaadinUI vaadinUI) { //vaadin version
        this.filter = filter;
        eventBus = vaadinUI.getNode().mbClasses.eventBus;
        tableModel = new MicroblogTableModel();

        final JTable table = new JTable(tableModel);
        final BroadcastMessageRenderer renderer = new BroadcastMessageRenderer(vaadinUI);
        // will allow it to fill entire scroll pane
        table.setFillsViewportHeight(true);
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

        final SortedSet<BroadcastMessage> existingMicroblogs = vaadinUI.getNode().mbClasses.mbsForViewing.getMicroblogSet();

        for (BroadcastMessage broadcastMessage : existingMicroblogs) {
            if (filter.apply(broadcastMessage)) {
                tableModel.addNewMicroblog(broadcastMessage);
            }
        }
    }*/

    @Subscribe
    public void modifyMicroblogsDisplay(BroadcastMessageModifiedEvent event){
        if(event.type.equals(BroadcastMessageModifiedEvent.ModificationType.RECEIVED)){
            if(filter.apply(event.broadcastMessage)){
                tableModel.addNewMicroblog(event.broadcastMessage);
            }
        }
    }

	public JComponent getContent() {
		return content;
	}

	@SuppressWarnings("serial")
	private class MicroblogTableModel extends AbstractTableModel {
		private final ArrayList<BroadcastMessage> broadcastMessages;
        // TODO: Use a separate Set so that we can efficiently check whether
        // broadcastMessages are being added more than once

  		public MicroblogTableModel() {
			broadcastMessages = Lists.newArrayList();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return broadcastMessages.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			return broadcastMessages.get(row);
		}

		public void addNewMicroblog(final BroadcastMessage bm) {
            broadcastMessages.add(0, bm);
            // This is what updates the GUI with new broadcastMessages.
            this.fireTableRowsInserted(0, tableModel.getRowCount());
		}

		public void removeMicroblog(final ParsedBroadcastMessage mb) {
			final int mbIndex = broadcastMessages.indexOf(mb);
			broadcastMessages.remove(mbIndex);
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
