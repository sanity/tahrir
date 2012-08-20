package tahrir.ui;

import java.awt.Component;
import java.awt.event.*;

public class TabCreateButton extends TransparentButton implements ActionListener {
	private final TrMainWindow mainWindow;
	private final String tabName;
	private final Component tabContents;

	public TabCreateButton(final TrMainWindow mainWindow, final String tabName, final Component tabContents) {
		super(null , null);
		this.mainWindow = mainWindow;
		this.tabContents = tabContents;
		this.tabName = tabName;
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
		mainWindow.createClosableTab(tabName, tabContents);
	}
}
