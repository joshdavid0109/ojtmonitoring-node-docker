package gui_classes.clientside;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import server_side.Server;
import shared_classes.User;
import shared_classes.XMLParse;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import static server_side.Server.*;
import static server_side.Server.loggedInUserHashMap;

public class LoginGUIForm extends JDialog implements Runnable{
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JRadioButton showPassword;
    private JPanel panel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private int result;
    private User u;

    public static final int OK = 1;
    public static final int CANCEL = 1;


    public LoginGUIForm(JFrame parent) {
        super(parent, "Login", true);

        showPassword.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    passwordField.setEchoChar('\u0000');
                else
                    passwordField.setEchoChar((Character) UIManager.get("PasswordField.echoChar"));
            }
        });


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;
                Document document = null;
                NodeList nodelist = null;
                try {
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    document = documentBuilder.parse("res/users.xml");
                   nodelist = document.getElementsByTagName("User");
                } catch (ParserConfigurationException | IOException | SAXException ex) {
                    throw new RuntimeException(ex);
                }

                Element element;
                boolean foundUser = false;

                for (User user: registeredUsersList) {
                    if (user.getUsername().equals(getUsername())) {
                        foundUser = true;
                        if (user.getPassword().equals(getPassword())) {
                            if (user.getStatus().equals("online")) {
                                JOptionPane.showMessageDialog(panel, "User is currently logged in on another device.");
                                break;
                            }
                                u = user;
                                result = OK;
                                dispose();
                                break;
                        } else {
                            //user exists pero mali password
                            JOptionPane.showMessageDialog(panel, "Incorrect password.");
                            break;
                        }
                    }

                }
                if (!foundUser) {
                    //user doesnot exist
                    JOptionPane.showMessageDialog(panel, "User does not exist.");
                }
            }
        });


        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

    }
    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public User getUser() {
        return u;
    }

    @Override
    public void run() {
        new LoginGUIForm(null);
    }

    public static void main(String[] args) {
        new LoginGUIForm(null);
    }
}
