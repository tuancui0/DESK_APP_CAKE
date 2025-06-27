package ui;

import dao.CustomerDAO;
import model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerPanel extends JPanel {
    private Connection conn;
    private CustomerDAO customerDAO;
    private DefaultTableModel tableModel;
    private JTable customerTable;
    private JTextField searchField;

    public CustomerPanel(Connection conn) {
        this.conn = conn;
        this.customerDAO = new CustomerDAO(conn);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadCustomers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== Panel tiêu đề =====
        JLabel titleLabel = new JLabel("Quản lý khách hàng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 105, 180));

        // ===== Panel tìm kiếm =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(255, 182, 193));
        searchButton.addActionListener(e -> searchCustomers());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // ===== Gộp tiêu đề và tìm kiếm vào topPanel =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH); // Tiêu đề ở trên
        topPanel.add(searchPanel, BorderLayout.WEST);  // Tìm kiếm ở bên trái
        add(topPanel, BorderLayout.NORTH);             // Thêm topPanel vào trên cùng

        // ===== Bảng hiển thị khách hàng =====
        String[] columns = {"Mã KH", "Tên KH", "Địa chỉ", "Giới tính", "Số điện thoại"};
        tableModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(25);
        customerTable.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        // ===== Panel ghi chú =====
        JLabel noteLabel = new JLabel("*Hiển thị danh sách khách hàng lấy dữ liệu từ database*", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);

        // ===== Panel nút chức năng =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        JButton deleteButton = new JButton("Xóa khách hàng");
        JButton updateButton = new JButton("Cập nhật");
        JButton addButton = new JButton("Thêm khách hàng");

        // Cài đặt màu nền và font chữ cho các nút
        addButton.setBackground(new Color(255, 182, 193));
        updateButton.setBackground(new Color(255, 182, 193));
        deleteButton.setBackground(new Color(255, 182, 193));
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        updateButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setFocusPainted(false);
        updateButton.setFocusPainted(false);
        deleteButton.setFocusPainted(false);

        // Thêm hình ảnh cho các nút
        String[] buttonNames = {"add.png", "update.png", "delete.png"};
        JButton[] buttons = {addButton, updateButton, deleteButton};

        for (int i = 0; i < buttons.length; i++) {
            URL imageUrl = getClass().getResource("/Image/" + buttonNames[i]);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                buttons[i].setIcon(new ImageIcon(scaledImage));
                buttons[i].setHorizontalAlignment(SwingConstants.LEFT);
                buttons[i].setIconTextGap(10);
            } else {
                System.err.println("Không tìm thấy ảnh /Image/" + buttonNames[i] + ". Nút chỉ hiển thị text.");
            }
        }

        deleteButton.addActionListener(e -> deleteCustomer());
        updateButton.addActionListener(e -> updateCustomer());
        addButton.addActionListener(e -> addCustomer());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // ===== Panel dưới cùng (ghi chú + nút) =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(noteLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCustomers() {
        try {
            tableModel.setRowCount(0); // Xóa dữ liệu cũ
            ArrayList<Customer> customers = customerDAO.getAllCustomers();
            for (Customer customer : customers) {
                Object[] row = {
                    customer.getCustomerId(),
                    customer.getCustomerName(),
                    customer.getAddress(),
                    customer.getGender(),
                    customer.getPhone()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim();
        try {
            tableModel.setRowCount(0);
            ArrayList<Customer> customers = customerDAO.searchByName(keyword);
            for (Customer customer : customers) {
                Object[] row = {
                    customer.getCustomerId(),
                    customer.getCustomerName(),
                    customer.getAddress(),
                    customer.getGender(),
                    customer.getPhone()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khách hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = customerDAO.deleteCustomer(customerId);
                if (success) {
                    loadCustomers();
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String customerName = (String) tableModel.getValueAt(selectedRow, 1);
        String address = (String) tableModel.getValueAt(selectedRow, 2);
        String gender = (String) tableModel.getValueAt(selectedRow, 3);
        String phone = (String) tableModel.getValueAt(selectedRow, 4);

        // Tạo các trường nhập liệu
        JTextField idField = new JTextField(String.valueOf(customerId), 15);
        idField.setEditable(false); // Mã khách hàng không cho sửa
        JTextField nameField = new JTextField(customerName, 15);
        JTextField addressField = new JTextField(address, 15);

        // Lấy dữ liệu giới tính từ cơ sở dữ liệu
        JComboBox<String> genderCombo = new JComboBox<>();
        try {
            ArrayList<String> genderTypes = customerDAO.getGenderTypes();
            for (String type : genderTypes) {
                genderCombo.addItem(type);
            }
            genderCombo.addItem("Khác");
            genderCombo.setSelectedItem(gender); // Đặt giá trị mặc định
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải giới tính: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        JTextField phoneField = new JTextField(phone, 15);

        // Tạo form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 240, 245));
        formPanel.setBorder(BorderFactory.createTitledBorder("🧑 Cập nhật khách hàng"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Mã khách hàng:"), gbc);
        gbc.gridx = 1; formPanel.add(idField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Tên khách hàng:"), gbc);
        gbc.gridx = 3; formPanel.add(nameField, gbc);

        // Hàng 2
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(addressField, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Hàng 3
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1; formPanel.add(genderCombo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 3; formPanel.add(phoneField, gbc);

        // Nút chức năng
        JButton cancelButton = new JButton("❌ Hủy");
        JButton saveButton = new JButton("💾 Lưu");

        Color buttonColor = new Color(255, 182, 193);
        cancelButton.setBackground(buttonColor);
        saveButton.setBackground(buttonColor);
        cancelButton.setFocusPainted(false);
        saveButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(255, 240, 245));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Container
        JPanel container = new JPanel(new BorderLayout());
        container.add(formPanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);

        // Hiển thị dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "➕ Cập nhật khách hàng", true);
        dialog.setContentPane(container);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Xử lý nút Lưu
        saveButton.addActionListener(e -> {
            try {
                Customer updatedCustomer = new Customer(
                    customerId,
                    nameField.getText(),
                    addressField.getText(),
                    (String) genderCombo.getSelectedItem(),
                    phoneField.getText()
                );
                boolean success = customerDAO.updateCustomer(updatedCustomer);
                if (success) {
                    loadCustomers();
                    JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật khách hàng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xử lý nút Hủy
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void addCustomer() {
        JTextField nameField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField genderField = new JTextField(10);
        JTextField phoneField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Tên khách hàng:"));
        panel.add(nameField);
        panel.add(new JLabel("Địa chỉ:"));
        panel.add(addressField);
        panel.add(new JLabel("Giới tính:"));
        panel.add(genderField);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm khách hàng", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Customer newCustomer = new Customer(
                    0, // MaKhachHang sẽ tự tăng
                    nameField.getText(),
                    addressField.getText(),
                    genderField.getText(),
                    phoneField.getText()
                );
                boolean success = customerDAO.insertCustomer(newCustomer);
                if (success) {
                    loadCustomers();
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}