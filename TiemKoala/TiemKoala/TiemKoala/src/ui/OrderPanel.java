package ui;

import dao.OrderDAO;
import model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.*;

public class OrderPanel extends JPanel {
    private Connection conn;
    private OrderDAO orderDAO;
    private DefaultTableModel tableModel;
    private JTable orderTable;
    private JTextField searchField;

    public OrderPanel(Connection conn) {
        this.conn = conn;
        this.orderDAO = new OrderDAO(conn);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadOrders();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Tiêu đề
        JLabel titleLabel = new JLabel("Quản lý đơn hàng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 105, 180));

        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(255, 182, 193));
        searchButton.setFont(new Font("Arial", Font.BOLD, 13));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchOrders());

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Top Panel chứa tiêu đề và tìm kiếm
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        // Bảng đơn hàng
        String[] columns = {"Mã đơn hàng", "Tên khách hàng", "Địa chỉ", "Ngày mua", "Tổng tiền", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(25);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 14));
        orderTable.setBackground(new Color(255, 245, 238));
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showOrderDetails();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        // Ghi chú
        JLabel noteLabel = new JLabel("*Nhấp đúp vào đơn hàng để xem chi tiết*", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);

        // ====== Panel nút chức năng ======
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton deleteButton = new JButton("Xóa đơn hàng.");
        JButton updateButton = new JButton("Cập nhật");

        // Cài đặt màu nền và font chữ cho các nút
        updateButton.setBackground(new Color(255, 182, 193));
        deleteButton.setBackground(new Color(255, 182, 193));
        updateButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Thêm ảnh cho các nút
        String[] buttonNames = {"update.png", "delete.png"};
        JButton[] buttons = { updateButton, deleteButton};

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
        updateButton.addActionListener(e -> updateOrder());
        deleteButton.addActionListener(e -> deleteOrder());

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // ====== Bottom panel (ghi chú + nút) ======
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(noteLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadOrders() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT dh.MaDonHang, dh.NgayLapDon, kh.TenKhachHang, kh.DiaChi, SUM(ct.SoLuong * ct.DonGia) as TongTien, dh.TrangThai " +
                           "FROM DonHang dh " +
                           "LEFT JOIN KhachHang kh ON dh.MaKhachHang = kh.MaKhachHang " +
                           "LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang " +
                           "GROUP BY dh.MaDonHang, dh.NgayLapDon, kh.TenKhachHang, kh.DiaChi, dh.TrangThai";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaDonHang"),
                    rs.getString("TenKhachHang") != null ? rs.getString("TenKhachHang") : "",
                    rs.getString("DiaChi") != null ? rs.getString("DiaChi") : "",
                    rs.getDate("NgayLapDon"),
                    rs.getDouble("TongTien"),
                    rs.getString("TrangThai")
                };
                tableModel.addRow(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchOrders() {
        String keyword = searchField.getText().trim();
        try {
            tableModel.setRowCount(0);
            String query = "SELECT dh.MaDonHang, dh.NgayLapDon, kh.TenKhachHang, kh.DiaChi, SUM(ct.SoLuong * ct.DonGia) as TongTien, dh.TrangThai " +
                           "FROM DonHang dh " +
                           "LEFT JOIN KhachHang kh ON dh.MaKhachHang = kh.MaKhachHang " +
                           "LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang " +
                           "WHERE dh.MaDonHang = ? " +
                           "GROUP BY dh.MaDonHang, dh.NgayLapDon, kh.TenKhachHang, kh.DiaChi, dh.TrangThai";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(keyword));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaDonHang"),
                    rs.getString("TenKhachHang") != null ? rs.getString("TenKhachHang") : "",
                    rs.getString("DiaChi") != null ? rs.getString("DiaChi") : "",
                    rs.getDate("NgayLapDon"),
                    rs.getDouble("TongTien"),
                    rs.getString("TrangThai")
                };
                tableModel.addRow(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn hàng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa đơn hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = orderDAO.deleteOrder(orderId);
                if (success) {
                    loadOrders();
                    JOptionPane.showMessageDialog(this, "Xóa đơn hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa đơn hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn hàng để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        String orderDate = tableModel.getValueAt(selectedRow, 3).toString();
        String customerName = tableModel.getValueAt(selectedRow, 1).toString();
        String customerAddress = tableModel.getValueAt(selectedRow, 2).toString();
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        double totalAmount = (double) tableModel.getValueAt(selectedRow, 4);

        // Tạo form giao diện đẹp
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(255, 228, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("CẬP NHẬT ĐƠN HÀNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(255, 105, 180));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBackground(new Color(255, 228, 225));

        JLabel dateLabel = new JLabel("Ngày lập đơn (YYYY-MM-DD):");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField orderDateField = new JTextField(orderDate, 20);
        orderDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        orderDateField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel customerNameLabel = new JLabel("Tên khách hàng:");
        customerNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField customerNameField = new JTextField(customerName, 20);
        customerNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        customerNameField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField addressField = new JTextField(customerAddress, 20);
        addressField.setFont(new Font("Arial", Font.PLAIN, 14));
        addressField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] statuses = {"Chờ xử lý", "Đang xử lý", "Hủy", "Hoàn thành"};
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setSelectedItem(status);
        statusComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        statusComboBox.setBackground(Color.WHITE);
        statusComboBox.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));

        inputPanel.add(dateLabel);
        inputPanel.add(orderDateField);
        inputPanel.add(customerNameLabel);
        inputPanel.add(customerNameField);
        inputPanel.add(addressLabel);
        inputPanel.add(addressField);
        inputPanel.add(statusLabel);
        inputPanel.add(statusComboBox);

        panel.add(inputPanel, BorderLayout.CENTER);

        // Thêm nút Thanh toán nếu trạng thái là "Hoàn thành"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(255, 228, 225));
        JButton updateButton = new JButton("Cập nhật");
        updateButton.setBackground(new Color(255, 182, 193));
        updateButton.setFont(new Font("Arial", Font.BOLD, 13));
        JButton paymentButton = new JButton("Thanh toán");
        paymentButton.setBackground(new Color(255, 182, 193));
        paymentButton.setFont(new Font("Arial", Font.BOLD, 13));
        paymentButton.setEnabled(status.equals("Hoàn thành"));

        updateButton.addActionListener(e -> {
            try {
                Date updatedOrderDate = Date.valueOf(orderDateField.getText());
                String updatedStatus = (String) statusComboBox.getSelectedItem();

                Integer customerId = null;
                String newCustomerName = customerNameField.getText().trim();
                String newAddress = addressField.getText().trim();

                if (!newCustomerName.isEmpty()) {
                    String findCustomerQuery = "SELECT MaKhachHang FROM KhachHang WHERE TenKhachHang = ?";
                    PreparedStatement findStmt = conn.prepareStatement(findCustomerQuery);
                    findStmt.setString(1, newCustomerName);
                    ResultSet rs = findStmt.executeQuery();
                    if (rs.next()) {
                        customerId = rs.getInt("MaKhachHang");
                    } else {
                        String insertCustomerQuery = "INSERT INTO KhachHang (TenKhachHang, DiaChi) VALUES (?, ?)";
                        PreparedStatement insertStmt = conn.prepareStatement(insertCustomerQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                        insertStmt.setString(1, newCustomerName);
                        insertStmt.setString(2, newAddress.isEmpty() ? null : newAddress);
                        insertStmt.executeUpdate();
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            customerId = generatedKeys.getInt(1);
                        }
                        insertStmt.close();
                    }
                    rs.close();
                    findStmt.close();
                }

                Order updatedOrder = new Order(orderId, updatedOrderDate, null, customerId, updatedStatus);
                boolean success = orderDAO.updateOrder(updatedOrder);
                if (success) {
                    loadOrders();
                    JOptionPane.showMessageDialog(this, "Cập nhật đơn hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật đơn hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật đơn hàng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        paymentButton.addActionListener(e -> showPaymentDialog(orderId, customerName, totalAmount));

        buttonPanel.add(updateButton);
        if (status.equals("Hoàn thành")) {
            buttonPanel.add(paymentButton);
        }

        panel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showConfirmDialog(this, panel, "Cập nhật đơn hàng", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private void showPaymentDialog(int orderId, String customerName, double totalAmount) {
        JDialog paymentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thanh toán Đơn hàng #" + orderId, true);
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
        String paymentInfo = "Nội dung chuyển khoản: " + customerName + " + " + String.format("%.2f", totalAmount) + " VND";
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
            if (processPayment(orderId)) {
                paymentDialog.dispose();
                JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadOrders(); // Làm mới danh sách đơn hàng
            }
        });
        buttonPanel.add(confirmButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        paymentDialog.add(mainPanel);
        paymentDialog.setVisible(true);
    }

    private boolean processPayment(int orderId) {
        try {
            conn.setAutoCommit(false);

            // Kiểm tra trạng thái
            String checkStatusQuery = "SELECT TrangThai FROM DonHang WHERE MaDonHang = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkStatusQuery);
            checkStmt.setInt(1, orderId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && !rs.getString("TrangThai").equals("Hoàn thành")) {
                JOptionPane.showMessageDialog(this, "Chỉ có thể thanh toán khi trạng thái là 'Hoàn thành'!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                rs.close();
                checkStmt.close();
                return false;
            }
            rs.close();
            checkStmt.close();

            // Lấy thông tin đơn hàng
            String orderQuery = "SELECT NgayLapDon, MaNhanVien, MaKhachHang FROM DonHang WHERE MaDonHang = ?";
            PreparedStatement orderStmt = conn.prepareStatement(orderQuery);
            orderStmt.setInt(1, orderId);
            rs = orderStmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy đơn hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                rs.close();
                orderStmt.close();
                return false;
            }
            Date orderDate = rs.getDate("NgayLapDon");
            Integer staffId = (Integer) rs.getObject("MaNhanVien"); // Lấy dưới dạng Object để kiểm tra null
            Integer customerId = (Integer) rs.getObject("MaKhachHang"); // Lấy dưới dạng Object để kiểm tra null
            rs.close();
            orderStmt.close();

            // Tạo hóa đơn mới
            String insertInvoiceQuery = "INSERT INTO HoaDon (NgayLapHoaDon, MaNhanVien, MaKhachHang, MaDonHang) VALUES (?, ?, ?, NULL)";
            PreparedStatement invoiceStmt = conn.prepareStatement(insertInvoiceQuery, Statement.RETURN_GENERATED_KEYS);
            invoiceStmt.setDate(1, orderDate);
            invoiceStmt.setObject(2, staffId); // Truyền null nếu staffId là null
            invoiceStmt.setObject(3, customerId); // Truyền null nếu customerId là null
            invoiceStmt.executeUpdate();
            ResultSet generatedKeys = invoiceStmt.getGeneratedKeys();
            int invoiceId = 0;
            if (generatedKeys.next()) {
                invoiceId = generatedKeys.getInt(1);
            }
            invoiceStmt.close();

            // Chuyển chi tiết đơn hàng sang chi tiết hóa đơn
            String detailQuery = "SELECT MaSanPham, SoLuong, DonGia FROM ChiTietDonHang WHERE MaDonHang = ?";
            PreparedStatement detailStmt = conn.prepareStatement(detailQuery);
            detailStmt.setInt(1, orderId);
            rs = detailStmt.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("MaSanPham");
                int quantity = rs.getInt("SoLuong");
                double unitPrice = rs.getDouble("DonGia");
                String insertDetailQuery = "INSERT INTO ChiTietHoaDon (SoHoaDon, MaSanPham, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
                PreparedStatement insertDetailStmt = conn.prepareStatement(insertDetailQuery);
                insertDetailStmt.setInt(1, invoiceId);
                insertDetailStmt.setInt(2, productId);
                insertDetailStmt.setInt(3, quantity);
                insertDetailStmt.setDouble(4, unitPrice);
                insertDetailStmt.executeUpdate();
                insertDetailStmt.close();
            }
            rs.close();
            detailStmt.close();

            // Xóa đơn hàng và chi tiết
            String deleteDetailQuery = "DELETE FROM ChiTietDonHang WHERE MaDonHang = ?";
            PreparedStatement deleteDetailStmt = conn.prepareStatement(deleteDetailQuery);
            deleteDetailStmt.setInt(1, orderId);
            deleteDetailStmt.executeUpdate();
            deleteDetailStmt.close();

            String deleteOrderQuery = "DELETE FROM DonHang WHERE MaDonHang = ?";
            PreparedStatement deleteOrderStmt = conn.prepareStatement(deleteOrderQuery);
            deleteOrderStmt.setInt(1, orderId);
            deleteOrderStmt.executeUpdate();
            deleteOrderStmt.close();

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showOrderDetails() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId;
        try {
            orderId = (int) tableModel.getValueAt(selectedRow, 0);
        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: Dữ liệu mã đơn hàng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new OrderDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), orderId, conn).setVisible(true);
    }
}