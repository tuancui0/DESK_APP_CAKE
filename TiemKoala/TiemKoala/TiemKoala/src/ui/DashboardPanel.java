package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardPanel extends JPanel {
    private Connection cnn;

    public DashboardPanel(Connection cnn) {
        this.cnn = cnn;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tiêu đề
        JLabel titleLabel = new JLabel("Trang chủ", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 105, 180));
        add(titleLabel, BorderLayout.NORTH);

        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Panel thống kê
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int totalProducts = getTotalProducts();
        int totalOrders = getTotalOrders();
        int totalEmployees = getTotalEmployees();

        JPanel productPanel = createStatPanel("TỔNG SẢN PHẨM", String.valueOf(totalProducts), new Color(255, 228, 225), "/Image/product_icon.png");
        JPanel orderPanel = createStatPanel("TỔNG ĐƠN HÀNG", String.valueOf(totalOrders), new Color(255, 228, 225), "/Image/nhanvien.png");
        JPanel employeePanel = createStatPanel("TỔNG NHÂN VIÊN", String.valueOf(totalEmployees), new Color(255, 228, 225), "/Image/employee_icon.png");

        statsPanel.add(productPanel);
        statsPanel.add(orderPanel);
        statsPanel.add(employeePanel);

        // Panel lịch sử đơn hàng
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel historyTitle = new JLabel("Hiện thị lịch sử đơn hàng", JLabel.CENTER);
        historyTitle.setFont(new Font("Arial", Font.BOLD, 18));
        historyTitle.setForeground(new Color(255, 105, 180));
        historyPanel.add(historyTitle, BorderLayout.NORTH);

        String[] columns = {"Mã đơn", "Khách hàng", "Ngày đặt", "Tổng tiền", "Trạng thái"};
        Object[][] data = getOrderHistory();
        JTable historyTable = new JTable(data, columns);
        historyTable.setFillsViewportHeight(true);
        historyTable.setBackground(Color.WHITE);
        historyTable.setGridColor(Color.LIGHT_GRAY);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(255, 228, 225));
        JScrollPane scrollPane = new JScrollPane(historyTable);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // Thêm các panel vào layout chính
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(historyPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createStatPanel(String title, String value, Color bgColor, String imagePath) {
        // Tạo panel với góc bo tròn
        JPanel panel = new JPanel(new BorderLayout()) {
            private final int cornerRadius = 15;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(255, 105, 180));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Panel chứa giá trị và hình ảnh
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(bgColor);
        centerPanel.setOpaque(false); // Đảm bảo centerPanel không che góc bo tròn

        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.BLACK);
        centerPanel.add(valueLabel, BorderLayout.CENTER);

        // Thêm hình ảnh
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerPanel.add(imageLabel, BorderLayout.EAST);
        } else {
            JLabel imageLabel = new JLabel("Ảnh", JLabel.CENTER);
            imageLabel.setForeground(Color.RED);
            centerPanel.add(imageLabel, BorderLayout.EAST);
            System.err.println("Không tìm thấy ảnh: " + imagePath);
        }

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private int getTotalProducts() {
        if (cnn == null) return 0;
        try {
            String sql = "SELECT COUNT(*) FROM SanPham";
            PreparedStatement stmt = cnn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy dữ liệu sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }

    private int getTotalOrders() {
        if (cnn == null) return 0;
        try {
            String sql = "SELECT COUNT(*) FROM DonHang";
            PreparedStatement stmt = cnn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy dữ liệu đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }

    private int getTotalEmployees() {
        if (cnn == null) return 0;
        try {
            String sql = "SELECT COUNT(*) FROM NhanVien";
            PreparedStatement stmt = cnn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy dữ liệu nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }

    private Object[][] getOrderHistory() {
        if (cnn == null) {
            return new Object[][] {
                {"DH001", "Nguyễn Văn A", "2025-05-17", "1,000,000", "Đã giao"},
                {"DH002", "Trần Thị B", "2025-05-16", "2,000,000", "Đang xử lý"}
            };
        }
        try {
            String sql = "SELECT dh.MaDonHang, kh.TenKhachHang, dh.NgayLapDon, " +
                        "COALESCE(SUM(ctd.SoLuong * ctd.DonGia), 0) AS TongTien, dh.TrangThai " +
                        "FROM DonHang dh " +
                        "LEFT JOIN KhachHang kh ON dh.MaKhachHang = kh.MaKhachHang " +
                        "LEFT JOIN ChiTietDonHang ctd ON dh.MaDonHang = ctd.MaDonHang " +
                        "GROUP BY dh.MaDonHang, kh.TenKhachHang, dh.NgayLapDon, dh.TrangThai " +
                        "ORDER BY dh.NgayLapDon DESC LIMIT 5";
            PreparedStatement stmt = cnn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            java.util.List<Object[]> dataList = new java.util.ArrayList<>();
            while (rs.next()) {
                Object[] row = {
                    rs.getString("MaDonHang"),
                    rs.getString("TenKhachHang"),
                    rs.getString("NgayLapDon"),
                    String.format("%,d", rs.getInt("TongTien")),
                    rs.getString("TrangThai")
                };
                dataList.add(row);
            }
            return dataList.toArray(new Object[0][]);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy dữ liệu lịch sử đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return new Object[][] {
                {"DH001", "Nguyễn Văn A", "2025-05-17", "1,000,000", "Đã giao"},
                {"DH002", "Trần Thị B", "2025-05-16", "2,000,000", "Đang xử lý"}
            };
        }
    }
}