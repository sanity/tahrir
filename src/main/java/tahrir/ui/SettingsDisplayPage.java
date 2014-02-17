package tahrir.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import tahrir.io.net.broadcasts.UserIdentity;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 19/07/13
 */
public class SettingsDisplayPage {
    private final JComponent content;
    private final EventBus eventBus;
    DefaultComboBoxModel model = new DefaultComboBoxModel();

    public SettingsDisplayPage(final TrMainWindow mainWindow) {
        eventBus = mainWindow.getNode().mbClasses.identityStore.eventBus;
        final JPanel panel = new JPanel();
        panel.add(new JLabel("Choose a profile"));

        for (UserIdentity userIdentity: mainWindow.getNode().mbClasses.identityStore.labelsOfUser.keySet()){
            if(userIdentity.hasPvtKey()){
                model.addElement(userIdentity.getNick());
            }
        }


        final JComboBox comboBox = new JComboBox(model);
        panel.add(comboBox);
        final JButton useButton = new JButton("Use");
        useButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(comboBox.getSelectedItem()==null){
                    //Do nothing..
                }
                else{
                    mainWindow.getNode().setCurrentIdentity((String)comboBox.getSelectedItem());
                }
            }
        }

        );
        panel.add(useButton);
        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(panel);
        content = scrollPane;


        eventBus.register(this);
    }

    @Subscribe
   public void identityModified(IdentityModifiedEvent event){
        if(event.identity.hasPvtKey()){
            if(event.type.equals(IdentityModifiedEvent.IdentityModificationType.ADD)){
                model.addElement(event.identity.getNick());
            }
            else{
                model.removeElement(event.identity.getNick());
            }
        }
    }

    public JComponent getContent() {
        return content;
    }
}

