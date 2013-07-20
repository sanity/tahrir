package tahrir.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import tahrir.io.net.microblogging.UserIdentity;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 19/07/13
 */
public class SettingsDisplayPage {
    private final JComponent content;
    private final SettingTableModel tableModel;
    EventBus eventBus;

    public SettingsDisplayPage(final TrMainWindow mainWindow) {
        eventBus = mainWindow.node.eventBus;
        tableModel = new SettingTableModel();

        for (UserIdentity userIdentity: mainWindow.node.identityStore.labelsOfUser.keySet()){
            if(userIdentity.hasPvtKey()){
                tableModel.addNewIdentity(userIdentity);
            }
        }
        final JButton useButton = new JButton("Use");

        final JTable table = new JTable(tableModel);
        table.add(useButton);
        table.setFillsViewportHeight(true);
        // TODO: change the size as needed
        table.setRowHeight(50);

        table.setFont(new Font("Arial", Font.BOLD, 12));
        table.setGridColor(new Color(244,242,242));
        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(table);
        content = scrollPane;

        eventBus.register(this);
    }

    @Subscribe
    public void identityModified(IdentityModifiedEvent event){
        if(event.identity.hasPvtKey()){
            if(event.type.equals(IdentityModifiedEvent.IdentityModificationType.ADD)){
                tableModel.addNewIdentity(event.identity);
            }
            else{
                tableModel.removeIdentity(event.identity);
            }
        }
    }

    public JComponent getContent() {
        return content;
    }

    private class SettingTableModel extends AbstractTableModel {
        private final ArrayList<UserIdentity> identitiesOfUser;

        public SettingTableModel() {
            identitiesOfUser = new ArrayList<UserIdentity>();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return identitiesOfUser.size();
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            return ("        "+ identitiesOfUser.get(row).getNick());
            //Indenting it.
        }

        public void addNewIdentity(UserIdentity identity) {
            if(!(identitiesOfUser.contains(identity))){
                identitiesOfUser.add(identity);
                this.fireTableRowsInserted(0, tableModel.getRowCount());
            }
        }

        public void removeIdentity(UserIdentity identity) {
            final int identityIndex = identitiesOfUser.indexOf(identity);
            identitiesOfUser.remove(identityIndex);
        }


        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return UserIdentity.class;
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

