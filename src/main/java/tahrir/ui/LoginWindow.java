package tahrir.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrNode;
import tahrir.network.broadcasts.UserIdentity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 11/11/13
 */
public class LoginWindow{
    public static Logger logger = LoggerFactory.getLogger(TrMainWindow.class.getName());

    private final JFrame frame;
    private JButton loginButton;
    private JButton createNewButton;
    private JLabel userNameLabel;
    private JLabel createNewLabel;
    private JLabel tahrir_logo;
    private final JComboBox usernameList;
    private JPanel panel = new JPanel();

    public LoginWindow(final TrNode node){


        //Adding existing userIdentites to the Model
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (UserIdentity userIdentity: node.mbClasses.identityStore.labelsOfUser.keySet()){
            if(userIdentity.getNick() !="Default" && userIdentity.hasPvtKey()){
                model.addElement(userIdentity.getNick());
            }
        }

        //construct components
        loginButton = new JButton ("Login");
        createNewButton = new JButton ("Create");
        userNameLabel = new JLabel ("<html>Select a username/ identity to post the message.<html>");
        createNewLabel = new JLabel("Set up a new Username");
        usernameList = new JComboBox (model);
        URL resource = this.getClass().getResource("tahrir-logo_small.png");
        tahrir_logo = new JLabel(new ImageIcon(resource));

        //set components properties
        usernameList.setToolTipText("Choose your Tahrir Identity");


        //adjust size and set layout
        panel.setPreferredSize(new Dimension(300, 400));
        panel.setLayout (null);

        //add components
        panel.add(loginButton);
        panel.add(createNewButton);
        panel.add(usernameList);
        panel.add(userNameLabel);
        panel.add(createNewLabel);
        panel.add(tahrir_logo);

        //set component bounds (Using Absolute Positioning (x, y, width, height))
        tahrir_logo.setBounds(80, 25, 140, 133);
        userNameLabel.setBounds(53, 185, 195, 40);
        usernameList.setBounds(65, 235, 170, 25);
        loginButton.setBounds(100, 265, 100, 20);
        createNewLabel.setBounds(78, 315, 195, 20);
        createNewButton.setBounds(100, 345, 100, 20);

        //Actions for buttons
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(usernameList.getSelectedItem()==null){
                    //Do nothing..
                }
                else{
                    frame.dispose();
                    node.setCurrentIdentity((String) usernameList.getSelectedItem());
                }
            }
        });

        createNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                RegisterWindow window  = new RegisterWindow(node);
            }
        });

        //Frame properties.
        frame = new JFrame();
        //Adding to Frame
        frame.getContentPane().add(panel);
        frame.setTitle("Tahrir");
       // int ypos = (int) ((TrConstants.screenSize.getHeight() - 400)/2);
       // int xpos = (int) (TrConstants.screenSize.getWidth() - 350/2);
       // frame.setLocation(xpos, ypos);
        frame.setSize(300, 400);
        frame.setLocationByPlatform(true);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


    }


}
