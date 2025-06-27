package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesPanel extends JPanel {
    private Connection cnn;
    private JTextField searchField;
    private JTable productTable;
    private JTable billTable;
    private JLabel totalLabel;
    private DefaultTableModel productTableModel;
    private DefaultTableModel billTableModel;
    private List<BillItem> billItems;
    private double totalAmount = 0.0;

    public SalesPanel(Connection cnn) {
        this.cnn = cnn;
        this.billItems = new ArrayList<>();
        initUI();
        loadProducts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 228, 225));

        // Tiêu đề trang
        JLabel titleLabel = new JLabel("Bán hàng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 105, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(255, 228, 225));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(255, 182, 193));
        searchButton.setFont(new Font("Arial", Font.BOLD, 13));
        searchButton.addActionListener(e -> searchProducts());
        searchPanel.add(new JLabel("Nhập sản phẩm cần tìm:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Panel chính: Danh sách sản phẩm và Hóa đơn
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Danh sách sản phẩm
        JPanel productPanel = new JPanel(new BorderLayout());
        productPanel.setBorder(BorderFactory.createTitledBorder("Danh sách sản phẩm"));
        productTableModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Loại SP", "Giá Bán", "Số Lượng"}, 0);
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setBackground(new Color(255, 245, 238));
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Hóa đơn
        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setBorder(BorderFactory.createTitledBorder("Hóa đơn"));
        billTableModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Số Lượng", "Đơn Giá", "Thành Tiền"}, 0);
        billTable = new JTable(billTableModel);
        billTable.setBackground(new Color(255, 245, 238));
        billPanel.add(new JScrollPane(billTable), BorderLayout.CENTER);

        mainPanel.add(productPanel);
        mainPanel.add(billPanel);

        // Panel dưới cùng: Tổng tiền, Chọn, Thanh toán, Đặt hàng
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("TỔNG TIỀN: 0.00 VND");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(255, 105, 180));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Chọn");
        addButton.setBackground(new Color(255, 182, 193));
        addButton.setFont(new Font("Arial", Font.BOLD, 13));
        addButton.addActionListener(e -> addToBill());

        JButton checkoutButton = new JButton("Thanh toán");
        checkoutButton.setBackground(new Color(255, 182, 193));
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 13));
        checkoutButton.addActionListener(e -> showPaymentDialog());

        JButton orderButton = new JButton("Đặt hàng");
        orderButton.setBackground(new Color(255, 182, 193));
        orderButton.setFont(new Font("Arial", Font.BOLD, 13));
        orderButton.addActionListener(e -> placeOrder());

        buttonPanel.add(addButton);
        buttonPanel.add(orderButton);
        buttonPanel.add(checkoutButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(searchPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadProducts() {
        try {
            String query = "SELECT MaSanPham, TenSanPham, LoaiSanPham, GiaBan, SoLuong FROM SanPham WHERE TrangThai = 'Còn hàng'";
            PreparedStatement stmt = cnn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            productTableModel.setRowCount(0);

            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                        rs.getInt("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getString("LoaiSanPham"),
                        rs.getDouble("GiaBan"),
                        rs.getInt("SoLuong")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim();
        try {
            String query = "SELECT MaSanPham, TenSanPham, LoaiSanPham, GiaBan, SoLuong FROM SanPham " +
                    "WHERE TrangThai = 'Còn hàng' AND (TenSanPham LIKE ? OR MaSanPham LIKE ?)";
            PreparedStatement stmt = cnn.prepareStatement(query);
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            productTableModel.setRowCount(0);

            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                        rs.getInt("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getString("LoaiSanPham"),
                        rs.getDouble("GiaBan"),
                        rs.getInt("SoLuong")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToBill() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maSanPham = (int) productTableModel.getValueAt(selectedRow, 0);
        String tenSanPham = (String) productTableModel.getValueAt(selectedRow, 1);
        double giaBan = (double) productTableModel.getValueAt(selectedRow, 3);
        int soLuongTon = (int) productTableModel.getValueAt(selectedRow, 4);

        String input = JOptionPane.showInputDialog(this, "Nhập số lượng:", "Thêm vào hóa đơn", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;

        try {
            int soLuong = Integer.parseInt(input);
            if (soLuong <= 0 || soLuong > soLuongTon) {
                JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ hoặc vượt quá tồn kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (BillItem item : billItems) {
                if (item.getMaSanPham() == maSanPham) {
                    int newQuantity = item.getSoLuong() + soLuong;
                    if (newQuantity > soLuongTon) {
                        JOptionPane.showMessageDialog(this, "Tổng số lượng vượt quá tồn kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    item.setSoLuong(newQuantity);
                    updateBillTable();
                    return;
                }
            }

            BillItem newItem = new BillItem(maSanPham, tenSanPham, soLuong, giaBan);
            billItems.add(newItem);
            updateBillTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là một số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBillTable() {
        billTableModel.setRowCount(0);
        totalAmount = 0.0;

        for (BillItem item : billItems) {
            double thanhTien = item.getSoLuong() * item.getDonGia();
            totalAmount += thanhTien;
            billTableModel.addRow(new Object[]{
                    item.getMaSanPham(),
                    item.getTenSanPham(),
                    item.getSoLuong(),
                    item.getDonGia(),
                    thanhTien
            });
        }
        totalLabel.setText("TỔNG TIỀN: " + String.format("%.2f", totalAmount) + " VND");
    }

    private void showPaymentDialog() {
        if (billItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog paymentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thanh toán", true);
        paymentDialog.setLayout(new BorderLayout());
        paymentDialog.setSize(400, 300);
        paymentDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Hiển thị QR code
        URL qrUrl = getClass().getResource("/Image/qrcode.png");
        if (qrUrl != null) {
            ImageIcon qrIcon = new ImageIcon(qrUrl);
            Image scaledImage = qrIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel qrLabel = new JLabel(new ImageIcon(scaledImage));
            mainPanel.add(qrLabel, BorderLayout.CENTER);
        } else {
            JLabel noQrLabel = new JLabel("Không tìm thấy QR code. Vui lòng liên hệ quản lý!");
            noQrLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            mainPanel.add(noQrLabel, BorderLayout.CENTER);
        }

        // Thông tin chuyển khoản
        String paymentInfo = "Nội dung chuyển khoản: " + String.format("%.2f", totalAmount) + " VND";
        JLabel infoLabel = new JLabel("<html><body style='text-align:center;'>" + paymentInfo + "</body></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(infoLabel, BorderLayout.NORTH);

        // Nút xác nhận
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setBackground(new Color(255, 182, 193));
        confirmButton.setFont(new Font("Arial", Font.BOLD, 13));
        confirmButton.addActionListener(e -> {
            paymentDialog.dispose();
            if (processPayment()) {
                JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                askPrintAndSaveInvoice();
            }
        });
        buttonPanel.add(confirmButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        paymentDialog.add(mainPanel);
        paymentDialog.setVisible(true);
    }

    private boolean processPayment() {
        try {
            cnn.setAutoCommit(false);

            // Cập nhật số lượng sản phẩm trong SanPham
            for (BillItem item : billItems) {
                String updateSanPham = "UPDATE SanPham SET SoLuong = SoLuong - ? WHERE MaSanPham = ?";
                PreparedStatement stmtUpdate = cnn.prepareStatement(updateSanPham);
                stmtUpdate.setInt(1, item.getSoLuong());
                stmtUpdate.setInt(2, item.getMaSanPham());
                stmtUpdate.executeUpdate();
                stmtUpdate.close();
            }

            // Lưu hóa đơn vào HoaDon
            String insertHoaDon = "INSERT INTO HoaDon (NgayLapHoaDon, MaNhanVien, MaKhachHang) VALUES (CURDATE(), NULL, NULL)";
            PreparedStatement stmtHoaDon = cnn.prepareStatement(insertHoaDon, Statement.RETURN_GENERATED_KEYS);
            stmtHoaDon.executeUpdate();
            ResultSet rsHoaDon = stmtHoaDon.getGeneratedKeys();
            int soHoaDon = 0;
            if (rsHoaDon.next()) {
                soHoaDon = rsHoaDon.getInt(1);
            }
            rsHoaDon.close();
            stmtHoaDon.close();

            // Lưu chi tiết hóa đơn vào ChiTietHoaDon
            for (BillItem item : billItems) {
                String insertChiTiet = "INSERT INTO ChiTietHoaDon (SoHoaDon, MaSanPham, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
                PreparedStatement stmtChiTiet = cnn.prepareStatement(insertChiTiet);
                stmtChiTiet.setInt(1, soHoaDon);
                stmtChiTiet.setInt(2, item.getMaSanPham());
                stmtChiTiet.setInt(3, item.getSoLuong());
                stmtChiTiet.setDouble(4, item.getDonGia());
                stmtChiTiet.executeUpdate();
                stmtChiTiet.close();
            }

            cnn.commit();
            return true;
        } catch (SQLException e) {
            try {
                cnn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                cnn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void askPrintAndSaveInvoice() {
        int printOption = JOptionPane.showConfirmDialog(this, "Bạn có muốn in hóa đơn không?", "In hóa đơn", JOptionPane.YES_NO_OPTION);
        if (printOption == JOptionPane.YES_OPTION) {
            printBill();
        }

        billItems.clear();
        updateBillTable();
        loadProducts();
    }

    private void printBill() {
        // Hiển thị chi tiết hóa đơn
        StringBuilder billDetails = new StringBuilder();
        billDetails.append("=== CHI TIẾT HÓA ĐƠN ===\n");
        billDetails.append(String.format("%-10s %-20s %-10s %-15s %-15s\n", "Mã SP", "Tên SP", "Số Lượng", "Đơn Giá", "Thành Tiền"));
        billDetails.append("------------------------------------------------\n");

        for (BillItem item : billItems) {
            double thanhTien = item.getSoLuong() * item.getDonGia();
            billDetails.append(String.format("%-10d %-20s %-10d %-15.2f %-15.2f\n",
                    item.getMaSanPham(),
                    item.getTenSanPham(),
                    item.getSoLuong(),
                    item.getDonGia(),
                    thanhTien));
        }
        billDetails.append("------------------------------------------------\n");
        billDetails.append(String.format("TỔNG TIỀN: %-15.2f VND\n", totalAmount));

        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(billDetails.toString())), "Hóa đơn", JOptionPane.INFORMATION_MESSAGE);
    }

    private void placeOrder() {
        if (billItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tạo form giao diện cho việc chọn khách hàng
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(255, 228, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("THÔNG TIN KHÁCH HÀNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(255, 105, 180));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(new Color(255, 228, 225));

        // Lấy danh sách khách hàng hiện có
        List<String> customerNames = new ArrayList<>();
        List<Integer> customerIds = new ArrayList<>();
        try {
            String query = "SELECT MaKhachHang, TenKhachHang FROM KhachHang";
            PreparedStatement stmt = cnn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            customerNames.add("Khách hàng mới");
            customerIds.add(-1); // -1 đại diện cho khách hàng mới
            while (rs.next()) {
                customerNames.add(rs.getString("TenKhachHang"));
                customerIds.add(rs.getInt("MaKhachHang"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JLabel customerLabel = new JLabel("Chọn khách hàng:");
        customerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JComboBox<String> customerComboBox = new JComboBox<>(customerNames.toArray(new String[0]));
        customerComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        customerComboBox.setBackground(Color.WHITE);
        customerComboBox.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel newCustomerLabel = new JLabel("Tên khách hàng mới:");
        newCustomerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField newCustomerField = new JTextField(20);
        newCustomerField.setFont(new Font("Arial", Font.PLAIN, 14));
        newCustomerField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));
        newCustomerField.setEnabled(false);

        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField addressField = new JTextField(20);
        addressField.setFont(new Font("Arial", Font.PLAIN, 14));
        addressField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));
        addressField.setEnabled(false);

        customerComboBox.addActionListener(e -> {
            int selectedIndex = customerComboBox.getSelectedIndex();
            boolean isNewCustomer = selectedIndex == 0;
            newCustomerField.setEnabled(isNewCustomer);
            addressField.setEnabled(isNewCustomer);
        });

        inputPanel.add(customerLabel);
        inputPanel.add(customerComboBox);
        inputPanel.add(newCustomerLabel);
        inputPanel.add(newCustomerField);
        inputPanel.add(addressLabel);
        inputPanel.add(addressField);

        panel.add(inputPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Đặt hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                cnn.setAutoCommit(false);

                // Xác định khách hàng
                int selectedIndex = customerComboBox.getSelectedIndex();
                Integer customerId = null;

                if (selectedIndex == 0) { // Khách hàng mới
                    String newCustomerName = newCustomerField.getText().trim();
                    String address = addressField.getText().trim();

                    if (newCustomerName.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng mới!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String insertCustomerQuery = "INSERT INTO KhachHang (TenKhachHang, DiaChi) VALUES (?, ?)";
                    PreparedStatement insertStmt = cnn.prepareStatement(insertCustomerQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                    insertStmt.setString(1, newCustomerName);
                    insertStmt.setString(2, address.isEmpty() ? null : address);
                    insertStmt.executeUpdate();
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        customerId = generatedKeys.getInt(1);
                    }
                    insertStmt.close();
                } else {
                    customerId = customerIds.get(selectedIndex);
                }

                // Tạo đơn hàng
                String insertDonHang = "INSERT INTO DonHang (NgayLapDon, MaKhachHang, TrangThai) VALUES (CURDATE(), ?, 'Đang xử lý')";
                PreparedStatement stmtDonHang = cnn.prepareStatement(insertDonHang, Statement.RETURN_GENERATED_KEYS);
                stmtDonHang.setInt(1, customerId);
                stmtDonHang.executeUpdate();
                ResultSet rsDonHang = stmtDonHang.getGeneratedKeys();
                int maDonHang = 0;
                if (rsDonHang.next()) {
                    maDonHang = rsDonHang.getInt(1);
                }
                rsDonHang.close();
                stmtDonHang.close();

                // Thêm chi tiết đơn hàng
                for (BillItem item : billItems) {
                    String insertChiTiet = "INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmtChiTiet = cnn.prepareStatement(insertChiTiet);
                    stmtChiTiet.setInt(1, maDonHang);
                    stmtChiTiet.setInt(2, item.getMaSanPham());
                    stmtChiTiet.setInt(3, item.getSoLuong());
                    stmtChiTiet.setDouble(4, item.getDonGia());
                    stmtChiTiet.executeUpdate();
                    stmtChiTiet.close();
                }

                cnn.commit();
                JOptionPane.showMessageDialog(this, "Đặt hàng thành công! Mã đơn hàng: " + maDonHang, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                billItems.clear();
                updateBillTable();
                loadProducts();
            } catch (SQLException e) {
                try {
                    cnn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    cnn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class BillItem {
    private int maSanPham;
    private String tenSanPham;
    private int soLuong;
    private double donGia;

    public BillItem(int maSanPham, String tenSanPham, int soLuong, double donGia) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public int getMaSanPham() {
        return maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }
}