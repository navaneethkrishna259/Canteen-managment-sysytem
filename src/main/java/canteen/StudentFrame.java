
package canteen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StudentFrame extends JFrame {

    private final Student student;
    private JTable menuTable;
    private DefaultTableModel menuModel;
    private JSpinner quantitySpinner;
    private JLabel totalLabel;

    public StudentFrame(Student student) {
        this.student = student;

        setTitle("Canteen Management - Student");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel(
                "Welcome, " + student.getUsername(),
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

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        orderPanel.add(new JLabel("Quantity:"));

        quantitySpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 100, 1)
        );
        orderPanel.add(quantitySpinner);

        JButton orderButton = new JButton("Place Order");
        orderButton.addActionListener(e -> placeOrder());
        orderPanel.add(orderButton);

        JButton refreshButton = new JButton("Refresh Menu");
        refreshButton.addActionListener(e -> loadMenu());
        orderPanel.add(refreshButton);

        bottomPanel.add(orderPanel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton historyButton = new JButton("Order History");
        historyButton.addActionListener(e -> showOrderHistory());
        actionPanel.add(historyButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        actionPanel.add(logoutButton);

        bottomPanel.add(actionPanel);

        totalLabel = new JLabel(
                "Select a food item to view its price.",
                SwingConstants.CENTER
        );
        bottomPanel.add(totalLabel);

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

    private void placeOrder() {
        int selectedRow = menuTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food item.");
            return;
        }

        int itemId = (int) menuModel.getValueAt(selectedRow, 0);
        int quantity = (int) quantitySpinner.getValue();

        String selectSql =
                "SELECT item_name, price, stock FROM menu_items "
              + "WHERE item_id = ? FOR UPDATE";

        String insertSql =
                "INSERT INTO orders "
              + "(user_id, item_id, quantity, total, discount, final_amount) "
              + "VALUES (?, ?, ?, ?, ?, ?)";

        String updateSql =
                "UPDATE menu_items SET stock = stock - ? "
              + "WHERE item_id = ? AND stock >= ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                String itemName;
                double price;
                int stock;

                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setInt(1, itemId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Food item not found.");
                        }

                        itemName = rs.getString("item_name");
                        price = rs.getDouble("price");
                        stock = rs.getInt("stock");
                    }
                }

                if (quantity > stock) {
                    throw new SQLException(
                            "Insufficient stock. Available: " + stock
                    );
                }

                double total = price * quantity;
                double discount = student.calculateDiscount(total);
                double finalAmount = total - discount;

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, itemId);
                    ps.setInt(3, quantity);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Unable to update stock.");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, student.getUserId());
                    ps.setInt(2, itemId);
                    ps.setInt(3, quantity);
                    ps.setDouble(4, total);
                    ps.setDouble(5, discount);
                    ps.setDouble(6, finalAmount);
                    ps.executeUpdate();
                }

                con.commit();

                totalLabel.setText(String.format(
                        "Total: Rs. %.2f | Discount: Rs. %.2f | Pay: Rs. %.2f",
                        total, discount, finalAmount
                ));

                String bill = "========== CANTEEN BILL ==========\n"
                        + "Order Date: "
                        + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                .format(new Date())
                        + "\nStudent: " + student.getUsername()
                        + "\nItem: " + itemName
                        + "\nQuantity: " + quantity
                        + "\nPrice: Rs. " + String.format("%.2f", price)
                        + "\nTotal: Rs. " + String.format("%.2f", total)
                        + "\nDiscount: Rs. " + String.format("%.2f", discount)
                        + "\nFinal Amount: Rs. "
                        + String.format("%.2f", finalAmount)
                        + "\n==================================";

                JOptionPane.showMessageDialog(
                        this, bill, "Order Bill", JOptionPane.INFORMATION_MESSAGE
                );

                loadMenu();

            } catch (Exception ex) {
                con.rollback();

                JOptionPane.showMessageDialog(
                        this, "Order failed: " + ex.getMessage(),
                        "Order Error", JOptionPane.ERROR_MESSAGE
                );
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Database error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showOrderHistory() {
        String sql = "SELECT o.order_id, m.item_name, o.quantity, "
                   + "o.total, o.discount, o.final_amount, o.order_date "
                   + "FROM orders o JOIN menu_items m "
                   + "ON o.item_id = m.item_id "
                   + "WHERE o.user_id = ? ORDER BY o.order_id DESC";

        DefaultTableModel historyModel = new DefaultTableModel(
                new String[]{
                    "Order ID", "Item", "Quantity", "Total",
                    "Discount", "Final Amount", "Date"
                }, 0
        );

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, student.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historyModel.addRow(new Object[]{
                            rs.getInt("order_id"),
                            rs.getString("item_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("total"),
                            rs.getDouble("discount"),
                            rs.getDouble("final_amount"),
                            rs.getTimestamp("order_date")
                    });
                }
            }

            JTable historyTable = new JTable(historyModel);
            JScrollPane scrollPane = new JScrollPane(historyTable);
            scrollPane.setPreferredSize(new Dimension(700, 300));

            JOptionPane.showMessageDialog(
                    this, scrollPane, "My Order History",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Unable to load history: " + ex.getMessage()
            );
        }
    }
}
