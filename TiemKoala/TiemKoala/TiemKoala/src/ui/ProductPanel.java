package ui;

import dao.ProductDAO;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;

public class ProductPanel extends JPanel {
    private Connection conn;
    private ProductDAO productDAO;
    private DefaultTableModel tableModel;
    private JTable productTable;
    private JTextField searchField;

    public ProductPanel(Connection conn) {
        this.conn = conn;
        this.productDAO = new ProductDAO(conn);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadProducts();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== Panel tiêu đề =====
        JLabel titleLabel = new JLabel("Quản lý sản phẩm", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 105, 180));

        // ===== Panel tìm kiếm =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(255, 182, 193));
        searchButton.addActionListener(e -> searchProducts());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // ===== Gộp tiêu đề và tìm kiếm vào topPanel =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH); // Tiêu đề ở trên
        topPanel.add(searchPanel, BorderLayout.WEST); // Tìm kiếm ở góc trên bên trái
        add(topPanel, BorderLayout.NORTH); // Thêm vào phần NORTH của main panel

        // ===== Bảng hiển thị sản phẩm =====
        String[] columns = {"Mã SP", "Tên SP", "Loại SP", "Mô tả", "Giá nhập", "Giá bán", "Số lượng", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25);
        productTable.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // ===== Panel ghi chú =====
        JLabel noteLabel = new JLabel("*Hiển thị danh sách sản phẩm lấy dữ liệu từ database*", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);


        // ====== Panel nút chức năng ======
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton deleteButton = new JButton("Xóa sản phẩm");
        JButton updateButton = new JButton("Cập nhật sản phẩm");
        JButton addButton = new JButton("Thêm sản phẩm");

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
        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());

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


    private void loadProducts() {
        try {
            tableModel.setRowCount(0); // Xóa dữ liệu cũ
            ArrayList<Product> products = productDAO.getAllProducts();
            for (Product product : products) {
                Object[] row = {
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductType(),
                    product.getDescription(),
                    product.getPurchasePrice(),
                    product.getSellingPrice(),
                    product.getQuantity(),
                    product.getStatus()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim();
        try {
            tableModel.setRowCount(0);
            ArrayList<Product> products = productDAO.searchByName(keyword);
            for (Product product : products) {
                Object[] row = {
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductType(),
                    product.getDescription(),
                    product.getPurchasePrice(),
                    product.getSellingPrice(),
                    product.getQuantity(),
                    product.getStatus()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = productDAO.deleteProduct(productId);
                if (success) {
                    loadProducts();
                    JOptionPane.showMessageDialog(this, "Xóa sản phẩm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa sản phẩm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);
        String productType = (String) tableModel.getValueAt(selectedRow, 2);
        String description = (String) tableModel.getValueAt(selectedRow, 3);
        double purchasePrice = (double) tableModel.getValueAt(selectedRow, 4);
        double sellingPrice = (double) tableModel.getValueAt(selectedRow, 5);
        int quantity = (int) tableModel.getValueAt(selectedRow, 6);
        String status = (String) tableModel.getValueAt(selectedRow, 7);

        // Tạo các trường nhập liệu
        JTextField idField = new JTextField(String.valueOf(productId), 15);
        idField.setEditable(false); // Mã sản phẩm không cho sửa
        JTextField nameField = new JTextField(productName, 15);
        JComboBox<String> typeCombo = new JComboBox<>();
        JTextArea descArea = new JTextArea(description, 3, 15);
        JTextField purchasePriceField = new JTextField(String.valueOf(purchasePrice), 10);
        JTextField sellingPriceField = new JTextField(String.valueOf(sellingPrice), 10);
        JTextField quantityField = new JTextField(String.valueOf(quantity), 10);
        quantityField.setEditable(true);
        JTextField statusField = new JTextField(status, 10);

        // Lấy dữ liệu loại sản phẩm từ database
        try {
            ArrayList<String> productTypes = productDAO.getProductTypes();
            for (String type : productTypes) {
                typeCombo.addItem(type);
            }
            typeCombo.addItem("Khác");
            typeCombo.setSelectedItem(productType); // Đặt giá trị mặc định
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải loại sản phẩm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Xử lý khi chọn "Khác"
        typeCombo.addActionListener(e -> {
            if ("Khác".equals(typeCombo.getSelectedItem())) {
                String newType = JOptionPane.showInputDialog(this, "NhậpUng loại sản phẩm mới:");
                if (newType != null && !newType.trim().isEmpty()) {
                    typeCombo.insertItemAt(newType, typeCombo.getItemCount() - 1);
                    typeCombo.setSelectedItem(newType);
                } else {
                    typeCombo.setSelectedIndex(0);
                }
            }
        });

        // Tạo form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 240, 245));
        formPanel.setBorder(BorderFactory.createTitledBorder("🧁 Cập nhật sản phẩm"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Mã sản phẩm:"), gbc);
        gbc.gridx = 1; formPanel.add(idField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Giá nhập:"), gbc);
        gbc.gridx = 3; formPanel.add(purchasePriceField, gbc);
        gbc.gridx = 4; formPanel.add(new JLabel("Giá bán:"), gbc);
        gbc.gridx = 5; formPanel.add(sellingPriceField, gbc);

        // Hàng 2
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Tên sản phẩm:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Số lượng:"), gbc);
        gbc.gridx = 3; formPanel.add(quantityField, gbc);
        gbc.gridx = 4; formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 5; formPanel.add(statusField, gbc);

        // Hàng 3
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Loại sản phẩm:"), gbc);
        gbc.gridx = 1; formPanel.add(typeCombo, gbc);

        // Hàng 4
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả sản phẩm:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 5;
        JScrollPane scrollDesc = new JScrollPane(descArea);
        formPanel.add(scrollDesc, gbc);

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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "➕ Cập nhật sản phẩm", true);
        dialog.setContentPane(container);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Xử lý nút Lưu
        saveButton.addActionListener(e -> {
            try {
                Product updatedProduct = new Product(
                    productId,
                    nameField.getText(),
                    (String) typeCombo.getSelectedItem(),
                    descArea.getText(),
                    Double.parseDouble(purchasePriceField.getText()),
                    Double.parseDouble(sellingPriceField.getText()),
                    Integer.parseInt(quantityField.getText()),
                    statusField.getText()
                );
                boolean success = productDAO.updateProduct(updatedProduct);
                if (success) {
                    loadProducts();
                    JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật sản phẩm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xử lý nút Hủy
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void addProduct() {
        // Tạo các trường nhập liệu
    	JTextField idField = new JTextField(15);
    	JTextField nameField = new JTextField(15);
    	JComboBox<String> typeCombo = new JComboBox<>();
    	JTextArea descArea = new JTextArea(3, 15);
    	JTextField purchasePriceField = new JTextField(10);
    	JTextField sellingPriceField = new JTextField(10);
    	JTextField quantityField = new JTextField(10);
    	quantityField.setEditable(true); // Fixed line
    	JTextField statusField = new JTextField(10);

        // Lấy dữ liệu loại sản phẩm từ database
        try {
            ArrayList<String> productTypes = productDAO.getProductTypes();
            for (String type : productTypes) {
                typeCombo.addItem(type);
            }
            typeCombo.addItem("Khác");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải loại sản phẩm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Xử lý khi chọn "Khác"
        typeCombo.addActionListener(e -> {
            if ("Khác".equals(typeCombo.getSelectedItem())) {
                String newType = JOptionPane.showInputDialog(this, "Nhập thông tin sản phẩm mới:");
                if (newType != null && !newType.trim().isEmpty()) {
                    typeCombo.insertItemAt(newType, typeCombo.getItemCount() - 1);
                    typeCombo.setSelectedItem(newType);
                } else {
                    typeCombo.setSelectedIndex(0);
                }
            }
        });

        // Tạo form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 240, 245));
        formPanel.setBorder(BorderFactory.createTitledBorder("🧁 Thêm sản phẩm"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Mã sản phẩm:"), gbc);
        gbc.gridx = 1; formPanel.add(idField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Giá nhập:"), gbc);
        gbc.gridx = 3; formPanel.add(purchasePriceField, gbc);
        gbc.gridx = 4; formPanel.add(new JLabel("Giá bán:"), gbc);
        gbc.gridx = 5; formPanel.add(sellingPriceField, gbc);

        // Hàng 2
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Tên sản phẩm:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Số lượng:"), gbc);
        gbc.gridx = 3; formPanel.add(quantityField, gbc);
        gbc.gridx = 4; formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 5; formPanel.add(statusField, gbc);

        // Hàng 3
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Loại sản phẩm:"), gbc);
        gbc.gridx = 1; formPanel.add(typeCombo, gbc);

        // Hàng 4
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả sản phẩm:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 5;
        JScrollPane scrollDesc = new JScrollPane(descArea);
        formPanel.add(scrollDesc, gbc);

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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "➕ Thêm sản phẩm", true);
        dialog.setContentPane(container);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Xử lý nút Lưu
        saveButton.addActionListener(e -> {
            try {
                Product newProduct = new Product(
                    0, // Mã sản phẩm tự tăng
                    nameField.getText(),
                    (String) typeCombo.getSelectedItem(),
                    descArea.getText(),
                    Double.parseDouble(purchasePriceField.getText()),
                    Double.parseDouble(sellingPriceField.getText()),
                    Integer.parseInt(quantityField.getText()),
                    statusField.getText()
                );
                boolean success = productDAO.insertProduct(newProduct);
                if (success) {
                    loadProducts();
                    JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm sản phẩm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm sản phẩm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xử lý nút Hủy
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}