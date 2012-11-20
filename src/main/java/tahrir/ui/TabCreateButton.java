package tahrir.ui;

import java.awt.event.*;

import javax.swing.*;

/**
 * Represents a button which, when clicked, will open a new tab in the Tahrir interface.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class TabCreateButton extends JButton implements ActionListener {
	private final TrMainWindow mainWindow;
	private final String tabName;
	private JComponent tabContents;

	public TabCreateButton(final TrMainWindow mainWindow, final String tabName, final JComponent tabContents) {
		this(null , null, mainWindow, tabName, tabContents);
	}

	public TabCreateButton(final String textOnButton, final Icon icon, final TrMainWindow mainWindow,
			final String tabName, final JComponent tabContents) {
		super(textOnButton, icon);
		this.mainWindow = mainWindow;
		this.tabContents = tabContents;
		this.tabName = tabName;

		setFocusable(false);
		setContentAreaFilled(false);
	}

	public TabCreateButton(final TrMainWindow mainWindow, final String tabName) {
		this(null, null, mainWindow, tabName, null);
	}

	public void setContents(final JComponent component) {
		tabContents = component;
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
		mainWindow.createClosableTab(tabName, tabContents);
	}
}
