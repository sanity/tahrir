package tahrir.ui;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.net.broadcasts.UserIdentity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 11/11/13
 */
public class RegisterWindow {
    public static Logger logger = LoggerFactory.getLogger(TrMainWindow.class.getName());

    private final JFrame frame;
    private JButton createNewButton;
    private JButton backToLoginButton;
    private JLabel tahrir_logo;
    private JLabel createNewLabel;
    private JLabel loginLabel;
    private final JLabel helpText;
    private JPanel panel = new JPanel();
    private JTextField userIdField = new JTextField(10);

    public RegisterWindow(final TrNode node){

        //construct components
        createNewButton = new JButton ("Create");
        createNewLabel = new JLabel("Create a new username");
        backToLoginButton = new JButton("Login");
        loginLabel = new JLabel("Login with existing username");
        helpText = new JLabel("<html>To post a message in Tahrir," +
                " you must have at least one identity/ username." +
                " This can be your real name," +
                " or an anonymous nickname of your own choosing. <br/></html>");
        URL resource = this.getClass().getResource("tahrir-logo_small.png");
        //System.out.println(resource);
        tahrir_logo = new JLabel(new ImageIcon(resource));

        //adjust size and set layout
        panel.setPreferredSize(new Dimension(280, 500));
        panel.setLayout (null);

        //add components
        panel.add(helpText);
        panel.add(createNewButton);
        panel.add(createNewLabel);
        panel.add(userIdField);
        panel.add(backToLoginButton);
        panel.add(loginLabel);
        panel.add(tahrir_logo);

        //set component bounds (Using Absolute Positioning (x, y, width, height))
        tahrir_logo.setBounds(70, 25, 140, 133);
        helpText.setBounds(45, 188, 190, 100);
        createNewLabel.setBounds (68, 303, 180, 20);
        userIdField.setBounds (68, 333, 145, 25);
        createNewButton.setBounds (90, 368, 100, 20);
        loginLabel.setBounds (43, 398, 195, 25);
        backToLoginButton.setBounds(90, 428, 100, 25);

        //Actions for buttons
        createNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(userIdField.getText().length()>0 && (!userIdField.getText().equals("Default") || !userIdField.getText().equals("default"))){
                    node.setCurrentIdentity(userIdField.getText());
                    UserIdentity identity = new UserIdentity(userIdField.getText(), node.getRemoteNodeAddress().publicKey, Optional.of(node.getPrivateNodeId().privateKey));
                    node.mbClasses.identityStore.addIdentityWithLabel(TrConstants.OWN,identity);
                    frame.dispose();

                }
                else{
                    //TODO: Prompt either username not entered or username can't be default
                }
            }
        });

        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                if(!node.mbClasses.identityStore.labelsOfUser.keySet().isEmpty()){
                    LoginWindow window = new LoginWindow(node);
                }
                else{
                    //Do nothing..
                }
            }
        });

        //Frame properties.
        frame = new JFrame();
        frame.setTitle("Tahrir");
            //Adding to Frame
            frame.getContentPane().add(panel);
        //int ypos = (int) ((TrConstants.screenSize.getHeight() - 300)/2);
        //int xpos = (int) (TrConstants.screenSize.getWidth() - 300/2);
        //frame.setLocation(xpos, ypos);
        frame.setSize(280, 500);
        frame.setLocationByPlatform(true);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }


}
