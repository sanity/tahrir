package tahrir.ui;

import java.awt.EventQueue;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class TrMainWindow {

	private JFrame frmTahrir;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (final Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final TrMainWindow window = new TrMainWindow();
					window.frmTahrir.setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TrMainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTahrir = new JFrame();
		frmTahrir.setResizable(false);
		frmTahrir.setTitle("Tahrir");
		frmTahrir.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
			}
		});

		final JScrollPane scrollPane = new JScrollPane();

		final JPanel panel = new JPanel();

		final JTextArea textArea = new JTextArea();
		textArea.setRows(3);
		final GroupLayout groupLayout = new GroupLayout(frmTahrir.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(
						groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
								.createSequentialGroup()
								.addComponent(textArea, GroupLayout.DEFAULT_SIZE, 386,
										Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 56,
												GroupLayout.PREFERRED_SIZE))
												.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(panel, GroupLayout.PREFERRED_SIZE, 58, Short.MAX_VALUE).addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(
				groupLayout
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(
						groupLayout
						.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
						.addGroup(
								Alignment.LEADING,
								groupLayout
								.createSequentialGroup()
								.addGroup(
										groupLayout
										.createParallelGroup(Alignment.LEADING)
										.addComponent(textArea,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
												.addGroup(
														groupLayout.createSequentialGroup()
														.addGap(13)
														.addComponent(btnSend)))
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 276,
																Short.MAX_VALUE))).addContainerGap()));

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		scrollPane.setViewportView(tabbedPane);

		final JLabel lblInspector = new JLabel("Inspector");
		panel.add(lblInspector);
		frmTahrir.getContentPane().setLayout(groupLayout);

	}
}
