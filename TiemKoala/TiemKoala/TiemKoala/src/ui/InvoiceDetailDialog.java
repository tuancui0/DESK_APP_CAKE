package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InvoiceDetailDialog extends JDialog {
    private Connection cnn; // Thêm biến Connection
    private JTable detailTable;
    private DefaultTableModel tableModel;

    public InvoiceDetailDialog(Frame parent, int soHoaDon, Connection cnn) {
        super(parent, "Chi tiết hóa đơn", true);
        this.cnn = cnn;
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        initUI(soHoaDon);
        loadInvoiceDetails(soHoaDon);
    }

    private void initUI(int soHoaDon) {
        // Tiêu đề
        JLabel titleLabel = new JLabel("Chi tiết hóa đơn #" + soHoaDon, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(255, 105, 180));
        add(titleLabel, BorderLayout.NORTH);

        // Bảng chi tiết
        String[] columns = {"Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        tableModel = new DefaultTableModel(columns, 0);
        detailTable = new JTable(tableModel);
        detailTable.setRowHeight(25);
        detailTable.setFont(new Font("Arial", Font.PLAIN, 14));
        detailTable.setBackground(new Color(255, 245, 238));
        JScrollPane scrollPane = new JScrollPane(detailTable);
        add(scrollPane, BorderLayout.CENTER);

        // Nút đóng
        JButton closeButton = new JButton("Đóng");
        closeButton.setBackground(new Color(255, 182, 193));
        closeButton.setFont(new Font("Arial", Font.BOLD, 13));
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 228, 225));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadInvoiceDetails(int soHoaDon) {
        try {
            String query = "SELECT sp.TenSanPham, ct.SoLuong, ct.DonGia, (ct.SoLuong * ct.DonGia) as ThanhTien " +
                           "FROM ChiTietHoaDon ct JOIN SanPham sp ON ct.MaSanPham = sp.MaSanPham " +
                           "WHERE ct.SoHoaDon = ?";
            PreparedStatement stmt = cnn.prepareStatement(query);
            stmt.setInt(1, soHoaDon);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("TenSanPham"),
                    rs.getInt("SoLuong"),
                    rs.getDouble("DonGia"),
                    rs.getDouble("ThanhTien")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}