package tahrir.ui;

import tahrir.TrUI;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renderer for rendering parsed broadcastMessages in a JTable.
 * 
 * We can make this more efficient by using a single JPanel, updating it and using it
 * for both rendering and editing.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class BroadcastMessageRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	private final TrUI mainWindow;

	public BroadcastMessageRenderer(final TrUI mainWindow) {
		this.mainWindow = mainWindow;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return getComponent(table, (BroadcastMessage) value, row);
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table, final Object value,
			final boolean isSelected, final int row, final int column) {
        return getComponent(table, (BroadcastMessage) value, row);
	}

    private Component getComponent(JTable table, BroadcastMessage broadcastMessage, int row) {
        final BroadcastMessagePostPanel panel = new BroadcastMessagePostPanel(broadcastMessage, mainWindow);

        table.setRowHeight(row, panel.getContent().getPreferredSize().height);

        return panel.getContent();
    }
}
