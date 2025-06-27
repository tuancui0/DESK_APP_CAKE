package ui;

import dao.EmployeeDAO;
import model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class EmployeePanel extends JPanel {
    private Connection conn;
    private EmployeeDAO employeeDAO;
    private DefaultTableModel tableModel;
    private JTable employeeTable;
    private JTextField searchField;

    public EmployeePanel(Connection conn) {
        this.conn = conn;
        this.employeeDAO = new EmployeeDAO(conn);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadEmployees();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ====== Tiêu đề và thanh tìm kiếm ======
        JLabel titleLabel = new JLabel("Quản lý nhân viên", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 105, 180));

        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(255, 182, 193));
        searchButton.addActionListener(e -> searchEmployees());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Gộp tiêu đề và tìm kiếm vào topPanel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // ====== Bảng nhân viên ======
        String[] columns = {"Mã NV", "Tên NV", "Giới tính", "Ngày sinh", "Địa chỉ", "Số điện thoại", "Lương"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(tableModel);
        employeeTable.setRowHeight(25);
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        add(scrollPane, BorderLayout.CENTER);

        // ====== Ghi chú ======
        JLabel noteLabel = new JLabel("*Hiển thị danh sách nhân viên lấy dữ liệu từ database*", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);

        // ====== Panel nút chức năng ======
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton deleteButton = new JButton("Xóa nhân viên");
        JButton updateButton = new JButton("Cập nhật");
        JButton addButton = new JButton("Thêm nhân viên");

        // Cài đặt màu nền và font chữ cho các nút
        addButton.setBackground(new Color(255, 182, 193));
        updateButton.setBackground(new Color(255, 182, 193));
        deleteButton.setBackground(new Color(255, 182, 193));
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        updateButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Thêm ảnh cho các nút
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

        // Thêm ActionListener cho các nút
        addButton.addActionListener(e -> addEmployee());
        updateButton.addActionListener(e -> updateEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // ====== Bottom panel (ghi chú + nút) ======
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(noteLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadEmployees() {
        try {
            tableModel.setRowCount(0); // Xóa dữ liệu cũ
            ArrayList<Employee> employees = employeeDAO.getAllEmployees();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            for (Employee employee : employees) {
                Object[] row = {
                    employee.getEmployeeId(),
                    employee.getEmployeeName(),
                    employee.getGender(),
                    dateFormat.format(employee.getDateOfBirth()),
                    employee.getAddress(),
                    employee.getPhone(),
                    String.format("%,.0f VND", employee.getSalary())
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchEmployees() {
        String keyword = searchField.getText().trim();
        try {
            tableModel.setRowCount(0);
            ArrayList<Employee> employees = employeeDAO.searchByName(keyword);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            for (Employee employee : employees) {
                Object[] row = {
                    employee.getEmployeeId(),
                    employee.getEmployeeName(),
                    employee.getGender(),
                    dateFormat.format(employee.getDateOfBirth()),
                    employee.getAddress(),
                    employee.getPhone(),
                    String.format("%,.0f VND", employee.getSalary())
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa nhân viên này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = employeeDAO.deleteEmployee(employeeId);
                if (success) {
                    loadEmployees();
                    JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        String employeeName = (String) tableModel.getValueAt(selectedRow, 1);
        String gender = (String) tableModel.getValueAt(selectedRow, 2);
        String dateOfBirthStr = (String) tableModel.getValueAt(selectedRow, 3);
        String address = (String) tableModel.getValueAt(selectedRow, 4);
        String phone = (String) tableModel.getValueAt(selectedRow, 5);
        String salaryStr = ((String) tableModel.getValueAt(selectedRow, 6)).replace(" VND", "").replace(",", "");

        // Tạo các trường nhập liệu
        JTextField idField = new JTextField(String.valueOf(employeeId), 15);
        idField.setEditable(false); // Mã nhân viên không cho sửa
        JTextField nameField = new JTextField(employeeName, 15);
        
        // Lấy dữ liệu giới tính từ cơ sở dữ liệu
        JComboBox<String> genderCombo = new JComboBox<>();
        try {
            ArrayList<String> genderTypes = employeeDAO.getGenderTypes();
            for (String type : genderTypes) {
                genderCombo.addItem(type);
            }
            genderCombo.addItem("Khác");
            genderCombo.setSelectedItem(gender); // Đặt giá trị mặc định
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải giới tính: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Xử lý khi chọn "Khác"
        genderCombo.addActionListener(e -> {
            if ("Khác".equals(genderCombo.getSelectedItem())) {
                String newGender = JOptionPane.showInputDialog(this, "Nhập giới tính mới:");
                if (newGender != null && !newGender.trim().isEmpty()) {
                    try {
                        employeeDAO.insertGenderType(newGender);
                        genderCombo.insertItemAt(newGender, genderCombo.getItemCount() - 1);
                        genderCombo.setSelectedItem(newGender);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi thêm giới tính mới: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        genderCombo.setSelectedIndex(0);
                    }
                } else {
                    genderCombo.setSelectedIndex(0);
                }
            }
        });

        // Chọn ngày sinh
        JDateChooser dateChooser = new JDateChooser();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateChooser.setDate(dateFormat.parse(dateOfBirthStr));
        } catch (Exception e) {
            dateChooser.setDate(null); // Đặt mặc định nếu lỗi
        }

        JTextField addressField = new JTextField(address, 15);
        JTextField phoneField = new JTextField(phone, 15);
        JTextField salaryField = new JTextField(String.valueOf(Double.parseDouble(salaryStr)), 15);

        // Tạo form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 240, 245));
        formPanel.setBorder(BorderFactory.createTitledBorder("👨‍💼 Cập nhật nhân viên"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Mã nhân viên:"), gbc);
        gbc.gridx = 1; formPanel.add(idField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Tên nhân viên:"), gbc);
        gbc.gridx = 3; formPanel.add(nameField, gbc);

        // Hàng 2
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(addressField, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Hàng 3
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1; formPanel.add(genderCombo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Ngày sinh:"), gbc);
        gbc.gridx = 3; formPanel.add(dateChooser, gbc);

        // Hàng 4
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(phoneField, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Hàng 5
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Lương:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(salaryField, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "➕ Cập nhật nhân viên", true);
        dialog.setContentPane(container);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Xử lý nút Lưu
        saveButton.addActionListener(e -> {
            try {
                Date dateOfBirth = dateChooser.getDate();
                if (dateOfBirth == null) {
                    throw new IllegalArgumentException("Vui lòng chọn ngày sinh hợp lệ!");
                }
                double salary = Double.parseDouble(salaryField.getText().trim());
                if (salary <= 0) {
                    throw new IllegalArgumentException("Lương phải lớn hơn 0!");
                }
                Employee updatedEmployee = new Employee(
                    employeeId,
                    nameField.getText(),
                    (String) genderCombo.getSelectedItem(),
                    dateOfBirth,
                    addressField.getText(),
                    phoneField.getText(),
                    salary
                );
                boolean success = employeeDAO.updateEmployee(updatedEmployee);
                if (success) {
                    loadEmployees();
                    JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lương phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật nhân viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xử lý nút Hủy
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void addEmployee() {
        // Tạo các trường nhập liệu
        JTextField nameField = new JTextField(15);

        // Lấy dữ liệu giới tính từ cơ sở dữ liệu
        JComboBox<String> genderCombo = new JComboBox<>();
        try {
            ArrayList<String> genderTypes = employeeDAO.getGenderTypes();
            for (String type : genderTypes) {
                genderCombo.addItem(type);
            }
            genderCombo.addItem("Khác");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải giới tính: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Xử lý khi chọn "Khác"
        genderCombo.addActionListener(e -> {
            if ("Khác".equals(genderCombo.getSelectedItem())) {
                String newGender = JOptionPane.showInputDialog(this, "Nhập giới tính mới:");
                if (newGender != null && !newGender.trim().isEmpty()) {
                    try {
                        employeeDAO.insertGenderType(newGender);
                        genderCombo.insertItemAt(newGender, genderCombo.getItemCount() - 1);
                        genderCombo.setSelectedItem(newGender);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi thêm giới tính mới: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        genderCombo.setSelectedIndex(0);
                    }
                } else {
                    genderCombo.setSelectedIndex(0);
                }
            }
        });

        // Chọn ngày sinh
        JDateChooser dateChooser = new JDateChooser();
        Calendar cal = Calendar.getInstance();
        dateChooser.setDate(cal.getTime()); // Đặt mặc định là ngày hiện tại (03/06/2025)

        JTextField addressField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField salaryField = new JTextField("5000000", 15); // Giá trị mặc định 5 triệu

        // Tạo form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 240, 245));
        formPanel.setBorder(BorderFactory.createTitledBorder("👨‍💼 Thêm nhân viên"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Tên nhân viên:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);

        // Hàng 2
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(addressField, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Hàng 3
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1; formPanel.add(genderCombo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Ngày sinh:"), gbc);
        gbc.gridx = 3; formPanel.add(dateChooser, gbc);

        // Hàng 4
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(phoneField, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Hàng 5
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Lương:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(salaryField, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "➕ Thêm nhân viên", true);
        dialog.setContentPane(container);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Xử lý nút Lưu
        saveButton.addActionListener(e -> {
            try {
                Date dateOfBirth = dateChooser.getDate();
                if (dateOfBirth == null) {
                    throw new IllegalArgumentException("Vui lòng chọn ngày sinh hợp lệ!");
                }
                double salary = Double.parseDouble(salaryField.getText().trim());
                if (salary <= 0) {
                    throw new IllegalArgumentException("Lương phải lớn hơn 0!");
                }
                Employee newEmployee = new Employee(
                    0, // MaNhanVien sẽ tự tăng
                    nameField.getText(),
                    (String) genderCombo.getSelectedItem(),
                    dateOfBirth,
                    addressField.getText(),
                    phoneField.getText(),
                    salary
                );
                boolean success = employeeDAO.insertEmployee(newEmployee);
                if (success) {
                    loadEmployees();
                    JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lương phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm nhân viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xử lý nút Hủy
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}