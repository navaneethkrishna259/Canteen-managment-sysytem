
package canteen;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public LoginFrame() {
        setTitle("Canteen Management System - Login");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 246, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("CANTEEN MANAGEMENT", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(25, 70, 140));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(18);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(18);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Login as:"), gbc);

        roleBox = new JComboBox<>(new String[]{"Student", "Admin"});
        gbc.gridx = 1;
        panel.add(roleBox, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(35, 95, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> login());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        getRootPane().setDefaultButton(loginButton);
        add(panel);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedRole = roleBox.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please enter username and password."
            );
            return;
        }

        String sql = "SELECT user_id, username, password, role "
                   + "FROM users WHERE username = ? AND role = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, selectedRole);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && password.equals(rs.getString("password"))) {

                    int userId = rs.getInt("user_id");
                    String dbUsername = rs.getString("username");
                    String dbPassword = rs.getString("password");

                    JOptionPane.showMessageDialog(this, "Login successful!");

                    dispose();

                    if ("Student".equalsIgnoreCase(selectedRole)) {
                        Student student = new Student(
                                userId, dbUsername, dbPassword
                        );
                        new StudentFrame(student).setVisible(true);
                    } else {
                        Admin admin = new Admin(
                                userId, dbUsername, dbPassword
                        );
                        new AdminFrame(admin).setVisible(true);
                    }

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid username, password, or role.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database connection failed.\n"
                    + "Please check your MySQL configuration.\n\n"
                    + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
