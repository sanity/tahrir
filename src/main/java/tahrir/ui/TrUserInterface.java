package tahrir.ui;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

public class TrUserInterface {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (final UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (final ClassNotFoundException e) {
			// handle exception
		} catch (final InstantiationException e) {
			// handle exception
		} catch (final IllegalAccessException e) {
			// handle exception
		}
	}

}
