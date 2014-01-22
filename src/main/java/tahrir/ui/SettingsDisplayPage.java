package tahrir.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import tahrir.io.net.broadcasts.UserIdentity;

import javax.swing.*;
import java.awt.*;
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
        eventBus = mainWindow.node.mbClasses.identityStore.eventBus;
        final JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel profile = new JLabel("Choose a profile");

        for (UserIdentity userIdentity: mainWindow.node.mbClasses.identityStore.labelsOfUser.keySet()){
            if(userIdentity.hasPvtKey()){
                model.addElement(userIdentity.getNick());
            }
        }


        final JComboBox comboBox = new JComboBox(model);
        final JButton useButton = new JButton("Use");
        useButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(comboBox.getSelectedItem()==null){
                    //Do nothing..
                }
                else{
                    mainWindow.node.setCurrentIdentity((String)comboBox.getSelectedItem());
                }
            }
        }

        );

        JLabel label = new JLabel("Create a new user identity");
        final JButton createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final RegisterWindow registerWindow = new RegisterWindow(mainWindow.node);
            }
        }
        );

        profile.setBounds(60, 60, 170, 30);
        comboBox.setBounds(235, 60, 180, 30);
        useButton.setBounds(420, 60, 60, 30);
        label.setBounds(60, 110, 200, 30);
        createButton.setBounds(235, 110, 100, 30);

        panel.add(profile);
        panel.add(comboBox);
        panel.add(useButton);
        panel.add(label);
        panel.add(createButton);

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

