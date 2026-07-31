package app.ui;

import app.core.Session;
import app.dao.UserDAO;
import app.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    public static LoginFrame instance;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private JButton loginButton;

    public LoginFrame() {
        super("Login");

        instance = this;

        initializeFrame();
        initializeComponents();
        setContentPane(createMainPanel());
        registerListeners();
    }

    private void initializeFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 320);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        showPasswordCheckBox = new JCheckBox("Show Password");
        loginButton = new JButton("Login");

        usernameField.setPreferredSize(new Dimension(0, 30));
        passwordField.setPreferredSize(new Dimension(0, 30));

        loginButton.setPreferredSize(new Dimension(110, 32));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(12, 24, 12, 24));

        panel.add(createTitleLabel(), BorderLayout.NORTH);
        panel.add(createFormPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createTitleLabel() {
        JLabel title = new JLabel("Sign In", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return title;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));

        panel.add(new JLabel("User ID"));
        panel.add(usernameField);

        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        panel.add(showPasswordCheckBox);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(loginButton);
        return panel;
    }

    private void registerListeners() {
        loginButton.addActionListener(_ -> onLogin());

        showPasswordCheckBox.addActionListener(_ -> passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char) 0 : '•'));
    }

    private void showLoginError() {
        JOptionPane.showMessageDialog(this, "Invalid Login", "Login", JOptionPane.ERROR_MESSAGE);
    }

    private void showLoginSuccess(User user) {
        JOptionPane.showMessageDialog(this, user.role().equals("ADMIN") ? "Login Succeeded As Admin" : "Login Succeeded As User", "Login", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openNextFrame(User user) {
        JFrame frame = user.role().equals("ADMIN") ? new AdminFrame() : new UserFrame();

        frame.setVisible(true);
        setVisible(false);
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        showPasswordCheckBox.setSelected(false);
        passwordField.setEchoChar('•');
    }

    private void onLogin() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        User user = UserDAO.login(username, password);

        if (user == null) {
            showLoginError();
            clearForm();
            return;
        }

        Session.setCurrentUser(user);

        showLoginSuccess(user);
        openNextFrame(user);
        clearForm();
    }
}