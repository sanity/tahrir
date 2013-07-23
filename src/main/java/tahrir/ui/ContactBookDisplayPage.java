package tahrir.ui;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import tahrir.io.net.microblogging.UserIdentity;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.*;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 8/7/13
 */
public class ContactBookDisplayPage {
    private final JComponent content;
    private final ContactBookTableModel tableModel;
    EventBus eventBus;

    public ContactBookDisplayPage(final TrMainWindow mainWindow) {
        eventBus = mainWindow.node.mbClasses.identityStore.eventBus;
        tableModel = new ContactBookTableModel();
        for (UserIdentity userIdentity: mainWindow.node.mbClasses.identityStore.labelsOfUser.keySet()){
            {
                tableModel.addNewIdentity(userIdentity);
            }
        }

        final JTable table = new JTable(tableModel);
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
        if(event.type.equals(IdentityModifiedEvent.IdentityModificationType.ADD)){
            if(!event.identity.hasPvtKey()){
                tableModel.addNewIdentity(event.identity);
            }
        }
        else{
            tableModel.removeIdentity(event.identity);
        }
    }

    public JComponent getContent() {
        return content;
    }

    private class ContactBookTableModel extends AbstractTableModel{
        private final ArrayList<UserIdentity> users;

        public ContactBookTableModel() {
            users = new ArrayList<UserIdentity>();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return users.size();
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            return ("        "+users.get(row).getNick());
            //Indenting it.
        }

        public void addNewIdentity(UserIdentity identity) {
            if(!(users.contains(identity)) && !(identity.hasPvtKey())){
            users.add(identity);
            this.fireTableRowsInserted(0, tableModel.getRowCount());
            }
        }

        public void removeIdentity(UserIdentity identity) {
            final int identityIndex = users.indexOf(identity);
            users.remove(identityIndex);
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