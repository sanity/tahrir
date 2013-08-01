package tahrir.ui;

import tahrir.io.net.microblogging.broadcastMessages.ParsedBroadcastMessage;

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
	private final TrMainWindow mainWindow;

	public BroadcastMessageRenderer(final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final ParsedBroadcastMessage parsedMb = (ParsedBroadcastMessage) value;
		final BroadcastMessagePostPanel panel = new BroadcastMessagePostPanel(parsedMb, mainWindow);

		return panel.getContent();
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table, final Object value,
			final boolean isSelected, final int row, final int column) {
		final ParsedBroadcastMessage parsedMb = (ParsedBroadcastMessage) value;
		final BroadcastMessagePostPanel panel = new BroadcastMessagePostPanel(parsedMb, mainWindow);
		return panel.getContent();
	}
}
