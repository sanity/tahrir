package tahrir.ui;

import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renderer for rendering parsed microblogs in a JTable.
 * 
 * We can make this more efficient by using a single JPanel, updating it and using it
 * for both rendering and editing.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	private final TrMainWindow mainWindow;

	public MicroblogRenderer(final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final ParsedMicroblog parsedMb = (ParsedMicroblog) value;
		final MicroblogPostPanel panel = new MicroblogPostPanel(parsedMb, mainWindow);

		return panel.getContent();
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table, final Object value,
			final boolean isSelected, final int row, final int column) {
		final ParsedMicroblog parsedMb = (ParsedMicroblog) value;
		final MicroblogPostPanel panel = new MicroblogPostPanel(parsedMb, mainWindow);
		return panel.getContent();
	}
}
