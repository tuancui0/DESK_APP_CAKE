package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetailDialog extends JDialog {
    private Connection conn;
    private int orderId;
    private JTable detailTable;
    private DefaultTableModel tableModel;
    private JLabel totalValue;

    public OrderDetailDialog(Frame parent, int orderId, Connection conn) {
        super(parent, "Chi tiết Đơn hàng #" + orderId, true);
        this.conn = conn;
        this.orderId = orderId;
        setLayout(new BorderLayout());
        setSize(700, 500); // Tăng kích thước dialog
        setLocationRelativeTo(parent);
        initUI();
        loadOrderDetails();
    }

    private void initUI() {
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tiêu đề
        JLabel titleLabel = new JLabel("Chi tiết Đơn hàng #" + orderId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(255, 105, 180));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel thông tin đơn hàng
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 5));
        infoPanel.setBackground(Color.WHITE);

        JLabel idLabel = new JLabel("Mã đơn hàng:");
        idLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel idValue = new JLabel(String.valueOf(orderId));
        idValue.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel dateLabel = new JLabel("Ngày mua:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel dateValue = new JLabel();
        dateValue.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel customerLabel = new JLabel("Tên khách hàng:");
        customerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel customerValue = new JLabel();
        customerValue.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel addressValue = new JLabel();
        addressValue.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel statusValue = new JLabel();
        statusValue.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel totalLabel = new JLabel("Tổng tiền:");
        totalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalValue = new JLabel("0.00");
        totalValue.setFont(new Font("Arial", Font.PLAIN, 14));

        infoPanel.add(idLabel);
        infoPanel.add(idValue);
        infoPanel.add(dateLabel);
        infoPanel.add(dateValue);
        infoPanel.add(customerLabel);
        infoPanel.add(customerValue);
        infoPanel.add(addressLabel);
        infoPanel.add(addressValue);
        infoPanel.add(statusLabel);
        infoPanel.add(statusValue);
        infoPanel.add(totalLabel);
        infoPanel.add(totalValue);

        mainPanel.add(infoPanel, BorderLayout.NORTH); // Đặt thông tin đơn hàng ở trên

        // Bảng chi tiết đơn hàng
        String[] columns = {"Mã sản phẩm", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        detailTable = new JTable(tableModel);
        detailTable.setRowHeight(25);
        detailTable.setFont(new Font("Arial", Font.PLAIN, 14));
        detailTable.setBackground(new Color(255, 245, 238));
        // Đặt chiều cao tối thiểu cho bảng
        detailTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        JScrollPane scrollPane = new JScrollPane(detailTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Đặt bảng ở trung tâm

        // Nút đóng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton closeButton = new JButton("Đóng");
        closeButton.setBackground(new Color(255, 182, 193));
        closeButton.setFont(new Font("Arial", Font.BOLD, 13));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Gán giá trị cho các JLabel sau khi tải dữ liệu
        try {
            String query = "SELECT dh.NgayLapDon, kh.TenKhachHang, kh.DiaChi, dh.TrangThai " +
                           "FROM DonHang dh " +
                           "LEFT JOIN KhachHang kh ON dh.MaKhachHang = kh.MaKhachHang " +
                           "WHERE dh.MaDonHang = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                dateValue.setText(rs.getDate("NgayLapDon") != null ? rs.getDate("NgayLapDon").toString() : "");
                customerValue.setText(rs.getString("TenKhachHang") != null ? rs.getString("TenKhachHang") : "Khách vãng lai");
                addressValue.setText(rs.getString("DiaChi") != null ? rs.getString("DiaChi") : "");
                statusValue.setText(rs.getString("TrangThai"));
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy đơn hàng #" + orderId, "Lỗi", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void loadOrderDetails() {
        double totalAmount = 0.0;
        try {
            String query = "SELECT ct.MaSanPham, sp.TenSanPham, ct.SoLuong, ct.DonGia " +
                           "FROM ChiTietDonHang ct " +
                           "JOIN SanPham sp ON ct.MaSanPham = sp.MaSanPham " +
                           "WHERE ct.MaDonHang = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int quantity = rs.getInt("SoLuong");
                double unitPrice = rs.getDouble("DonGia");
                double total = quantity * unitPrice;
                totalAmount += total;
                tableModel.addRow(new Object[]{
                    rs.getInt("MaSanPham"),
                    rs.getString("TenSanPham"),
                    quantity,
                    unitPrice,
                    total
                });
            }
            if (!hasData) {
                JOptionPane.showMessageDialog(this, "Không có chi tiết đơn hàng cho đơn hàng #" + orderId, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            totalValue.setText(String.format("%.2f", totalAmount));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}