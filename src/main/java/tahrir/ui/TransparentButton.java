package tahrir.ui;

import javax.swing.*;

public class TransparentButton extends JButton {
	public TransparentButton(final ImageIcon icon, final String toolTip) {
		super(icon);
		setToolTipText(toolTip);
		setToolTipText("Close this tab");
		setContentAreaFilled(false);
		setFocusable(false);
	}
}
