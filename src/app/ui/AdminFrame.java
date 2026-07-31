package app.ui;

import app.core.Session;
import app.dao.UserDAO;
import app.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JTextField passwordField = new JTextField();
    private final DefaultListModel<User> usersModel = new DefaultListModel<>();
    private final JList<User> usersList = new JList<>(usersModel);

    public AdminFrame() {
        super("Admin Dashboard");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        onCloseReturnToLogin();

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildTitle(), BorderLayout.NORTH);

        JPanel top = new JPanel(new GridLayout(1, 2, 12, 0));
        top.add(buildInputPanel());
        top.add(buildActionPanel());

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.add(top, BorderLayout.NORTH);
        center.add(buildListPanel(), BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        updateUserList();
    }

    private void onCloseReturnToLogin() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LoginFrame.instance.setVisible(true);
            }
        });
    }

    private JLabel buildTitle() {
        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return title;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("User Information"));

        usernameField.setPreferredSize(new Dimension(0, 30));
        passwordField.setPreferredSize(new Dimension(0, 30));

        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));

        panel.add(button("Add User", this::onAddUser));
        panel.add(button("Update User", this::onUpdateUser));
        panel.add(button("Remove User", this::onRemoveSelected));

        return panel;
    }

    private JPanel buildListPanel() {
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.setFixedCellHeight(24);
        usersList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersList.addListSelectionListener(_ -> onSelectionChanged());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Users"));
        panel.add(new JScrollPane(usersList));

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(button("Logout", this::onLogout), BorderLayout.WEST);
        panel.add(button("Exit", () -> System.exit(0)), BorderLayout.EAST);
        return panel;
    }

    private JButton button(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(110, 32));
        btn.addActionListener(_ -> action.run());
        return btn;
    }

    private void updateUserList() {
        usersModel.clear();
        for (User user : UserDAO.getAllUsers()) {
            usersModel.addElement(user);
        }
    }

    private void onAddUser() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Add User", "Please enter both username and password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (UserDAO.addUser(username, password)) {
            showMessage("Add User", "User added successfully!", JOptionPane.INFORMATION_MESSAGE);
            updateUserList();
            clearInputs();
        } else {
            showMessage("Add User", "Username already exists!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onUpdateUser() {

        User selected = usersList.getSelectedValue();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (selected == null) {
            showMessage("Update User", "Please select a user from the list", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.isBlank() || password.isBlank()) {
            showMessage("Update User", "Please enter both username and password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!username.equals(selected.username()) && UserDAO.usernameExists(username)) {
            showMessage("Update User", "Username already exists!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (UserDAO.updateUser(selected.id(), username, password)) {
            showMessage("Update User", "User updated successfully!", JOptionPane.INFORMATION_MESSAGE);
            updateUserList();
            clearInputs();
        } else {
            showMessage("Update User", "User not found!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onRemoveSelected() {

        User selected = usersList.getSelectedValue();

        if (selected == null) {
            showMessage("Remove User", "Please select a user from the list", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (UserDAO.removeUser(selected.id())) {
            showMessage("Remove User", "User removed successfully!", JOptionPane.INFORMATION_MESSAGE);
            updateUserList();
            clearInputs();
        }
    }

    private void onSelectionChanged() {

        User selected = usersList.getSelectedValue();

        if (selected != null) {
            usernameField.setText(selected.username());
            passwordField.setText("");
        }
    }

    private void onLogout() {
        Session.logout();
        LoginFrame.instance.setVisible(true);
        dispose();
    }

    private void clearInputs() {
        usernameField.setText("");
        passwordField.setText("");
        usersList.clearSelection();
    }

    private void showMessage(String title, String message, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }
}
