package tahrir.ui;

import javax.swing.*;
import javax.swing.event.*;

import net.miginfocom.swing.MigLayout;
import tahrir.TrNode;

public class TrMainWindow extends JFrame {
	public TrNode node;
	private final TahrirMenu menu;
	private JScrollPane selectedPanel;
	//private final JTabbedPane pageTabbedPane;

	public TrMainWindow(final TrNode node) {
		this.node = node;

		setTitle("Tahrir");
		setSize(500, 700);
		setLayout(new MigLayout());

		/*pageTabbedPane = new JTabbedPane();
		pageTabbedPane.setTabPlacement(JTabbedPane.LEFT);

		final JScrollPane scroller = new JScrollPane(new FeedPage(this));
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		pageTabbedPane.addTab( "Feed", scroller);
		pageTabbedPane.addTab( "Contacts", new JPanel());
		pageTabbedPane.addTab( "Configure", new JPanel());

		getContentPane().add(pageTabbedPane);
		 */

		menu = new TahrirMenu();
		getContentPane().add(menu, "cell 0 0");
	}


	private class MenuSelectionHandler implements ListSelectionListener {
		@Override
		public void valueChanged(final ListSelectionEvent event) {
			final TrMainWindow parent = TrMainWindow.this;
			final int selected = parent.menu.menuList.getSelectedIndex();
			if (selectedPanel != null) {
				getContentPane().remove(selectedPanel);
			}
			if (selected == 0) {
				final JScrollPane scroller = new JScrollPane(new FeedPage(TrMainWindow.this));
				scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				selectedPanel = scroller;
				getContentPane().add(scroller, "cell 1 0");
			} else {
				selectedPanel = new JScrollPane();
				selectedPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				selectedPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				getContentPane().add(selectedPanel, "cell 1 0");
			}
		}
	}

	private class TahrirMenu extends JPanel {
		public final JList<String> menuList;

		public TahrirMenu() {
			setLayout(new MigLayout());
			final String[] data = {"Feed", "Contacts", "Configure"};
			menuList = new JList<String>(data);
			menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			menuList.addListSelectionListener(new MenuSelectionHandler());
			//menuList.setSelectedIndex(0);
			add(menuList, "wrap");
			add(new JButton("New post"));
		}
	}
}
