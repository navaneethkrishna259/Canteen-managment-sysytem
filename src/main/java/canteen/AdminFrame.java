
package canteen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminFrame extends JFrame {

    private final Admin admin;
    private JTable menuTable;
    private DefaultTableModel menuModel;

    private JTextField nameField;
    private JTextField priceField;
    private JTextField stockField;

    public AdminFrame(Admin admin) {
        this.admin = admin;

        setTitle("Canteen Management - Admin");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel(
                "Admin Dashboard - " + admin.getUsername(),
                SwingConstants.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        mainPanel.add(heading, BorderLayout.NORTH);

        menuModel = new DefaultTableModel(
                new String[]{"ID", "Food Item", "Price (Rs.)", "Stock"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        menuTable = new JTable(menuModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mainPanel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Food Item"));

        nameField = new JTextField(10);
        priceField = new JTextField(6);
        stockField = new JTextField(5);

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);

        JButton addButton = new JButton("Add Item");
        addButton.addActionListener(e -> addItem());
        formPanel.add(addButton);

        bottomPanel.add(formPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton deleteButton = new JButton("Delete Selected Item");
        deleteButton.addActionListener(e -> deleteItem());
        buttonPanel.add(deleteButton);

        JButton refreshButton = new JButton("Refresh Menu");
        refreshButton.addActionListener(e -> loadMenu());
        buttonPanel.add(refreshButton);

        JButton reportButton = new JButton("Sales Report");
        reportButton.addActionListener(e -> showSalesReport());
        buttonPanel.add(reportButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        buttonPanel.add(logoutButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadMenu();
    }

    private void loadMenu() {
        menuModel.setRowCount(0);

        String sql = "SELECT item_id, item_name, price, stock "
                   + "FROM menu_items ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                menuModel.addRow(new Object[]{
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Unable to load menu: " + ex.getMessage()
            );
        }
    }

    private void addItem() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a food item name.");
            return;
        }

        double price;
        int stock;

        try {
            price = Double.parseDouble(priceField.getText().trim());
            stock = Integer.parseInt(stockField.getText().trim());

            if (!Double.isFinite(price) || price <= 0 || stock < 0) {
                JOptionPane.showMessageDialog(
                        this, "Price must be positive and stock cannot be negative."
                );
                return;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter a valid price and whole-number stock."
            );
            return;
        }

        String sql = "INSERT INTO menu_items (item_name, price, stock) "
                   + "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, stock);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Food item added successfully!");

            nameField.setText("");
            priceField.setText("");
            stockField.setText("");

            loadMenu();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Unable to add item: " + ex.getMessage()
            );
        }
    }

    private void deleteItem() {
        int selectedRow = menuTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Select a food item to delete."
            );
            return;
        }

        int itemId = (int) menuModel.getValueAt(selectedRow, 0);
        String itemName = menuModel.getValueAt(selectedRow, 1).toString();

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete " + itemName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM menu_items WHERE item_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, itemId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Food item deleted.");
            }

            loadMenu();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to delete item. It may have existing orders.\n"
                    + "Details: " + ex.getMessage()
            );
        }
    }

    private void showSalesReport() {
        String sql = "SELECT COUNT(*) AS total_orders, "
                   + "COALESCE(SUM(total), 0) AS gross_sales, "
                   + "COALESCE(SUM(discount), 0) AS total_discount, "
                   + "COALESCE(SUM(final_amount), 0) AS net_sales "
                   + "FROM orders";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String report = "========== SALES REPORT ==========\n"
                        + "Total Order Records: "
                        + rs.getInt("total_orders")
                        + "\nGross Sales: Rs. "
                        + String.format("%.2f", rs.getDouble("gross_sales"))
                        + "\nTotal Discounts: Rs. "
                        + String.format("%.2f", rs.getDouble("total_discount"))
                        + "\nNet Sales: Rs. "
                        + String.format("%.2f", rs.getDouble("net_sales"))
                        + "\n==================================";

                JOptionPane.showMessageDialog(
                        this, report, "Sales Report",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Unable to generate report: " + ex.getMessage()
            );
        }
    }
}
