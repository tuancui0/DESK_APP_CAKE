package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;

public class UserPanel extends JPanel {
    private Connection conn;
    private UserDAO userDAO;
    private DefaultTableModel tableModel;
    private JTable userTable;
    private JTextField searchField;

    public UserPanel(Connection conn) {
        this.conn = conn;
        this.userDAO = new UserDAO(conn);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ====== Tiêu đề ======
        JLabel titleLabel = new JLabel("Quản lý người dùng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 105, 180));

        // ====== Panel tìm kiếm ======
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(255, 182, 193));
        searchButton.setFont(new Font("Arial", Font.BOLD, 13));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchUsers());

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // ====== Top Panel chứa tiêu đề và tìm kiếm ======
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        // ====== Bảng người dùng ======
        String[] columns = {"Tên đăng nhập", "Mật khẩu", "Vai trò", "Mã NV"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // ====== Ghi chú ======
        JLabel noteLabel = new JLabel("*Hiển thị danh sách người dùng lấy dữ liệu từ database*", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);

        // ====== Nút chức năng ======
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton deleteButton = new JButton("Xóa người dùng");
        JButton updateButton = new JButton("Cập nhật");
        JButton addButton = new JButton("Thêm người dùng");

        // Cài đặt màu nền và font chữ cho các nút
        addButton.setBackground(new Color(255, 182, 193));
        updateButton.setBackground(new Color(255, 182, 193));
        deleteButton.setBackground(new Color(255, 182, 193));
        addButton.setFont(new Font("Arial", Font.BOLD, 13));
        updateButton.setFont(new Font("Arial", Font.BOLD, 13));
        deleteButton.setFont(new Font("Arial", Font.BOLD, 13));
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

        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // ====== Bottom Panel ======
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(noteLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        try {
            tableModel.setRowCount(0);
            ArrayList<User> users = userDAO.getAllUsers();
            for (User user : users) {
                Object[] row = {
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getEmployeeId() != null ? user.getEmployeeId() : ""
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách người dùng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchUsers() {
        String keyword = searchField.getText().trim();
        try {
            tableModel.setRowCount(0);
            ArrayList<User> users = userDAO.searchByName(keyword);
            for (User user : users) {
                Object[] row = {
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getEmployeeId() != null ? user.getEmployeeId() : ""
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm người dùng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa người dùng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userDAO.deleteUser(username);
                if (success) {
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "Xóa người dùng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa người dùng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa người dùng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String password = (String) tableModel.getValueAt(selectedRow, 1);
        String role = (String) tableModel.getValueAt(selectedRow, 2);
        Object empIdObj = tableModel.getValueAt(selectedRow, 3);
        String empIdStr = empIdObj != null && !empIdObj.toString().isEmpty() ? empIdObj.toString() : "";

        // Tạo form giao diện đẹp
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(255, 228, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("CẬP NHẬT NGƯỜI DÙNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(255, 105, 180));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(new Color(255, 228, 225));

        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField passwordField = new JTextField(password, 20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel roleLabel = new JLabel("Vai trò:");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField roleField = new JTextField(role, 20);
        roleField.setFont(new Font("Arial", Font.PLAIN, 14));
        roleField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel employeeIdLabel = new JLabel("Mã nhân viên:");
        employeeIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField employeeIdField = new JTextField(empIdStr, 20);
        employeeIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        employeeIdField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(roleLabel);
        inputPanel.add(roleField);
        inputPanel.add(employeeIdLabel);
        inputPanel.add(employeeIdField);

        panel.add(inputPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Cập nhật người dùng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Integer employeeId = employeeIdField.getText().isEmpty() ? null : Integer.parseInt(employeeIdField.getText());
                User updatedUser = new User(username, passwordField.getText(), roleField.getText(), employeeId);
                boolean success = userDAO.updateUser(updatedUser);
                if (success) {
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "Cập nhật người dùng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật người dùng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên phải là số nguyên!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật người dùng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addUser() {
        // Tạo form giao diện đẹp
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(255, 228, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("THÊM NGƯỜI DÙNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(255, 105, 180));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBackground(new Color(255, 228, 225));

        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField passwordField = new JTextField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel roleLabel = new JLabel("Vai trò:");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField roleField = new JTextField(20);
        roleField.setFont(new Font("Arial", Font.PLAIN, 14));
        roleField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel employeeIdLabel = new JLabel("Mã nhân viên:");
        employeeIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField employeeIdField = new JTextField(20);
        employeeIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        employeeIdField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(roleLabel);
        inputPanel.add(roleField);
        inputPanel.add(employeeIdLabel);
        inputPanel.add(employeeIdField);

        panel.add(inputPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm người dùng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String role = roleField.getText().trim();

                if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin bắt buộc!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Integer employeeId = employeeIdField.getText().isEmpty() ? null : Integer.parseInt(employeeIdField.getText());

                User newUser = new User(username, password, role, employeeId);
                boolean success = userDAO.insertUser(newUser);
                if (success) {
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "Thêm người dùng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm người dùng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên phải là số nguyên!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm người dùng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}