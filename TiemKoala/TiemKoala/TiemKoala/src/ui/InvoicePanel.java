package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class InvoicePanel extends JPanel {
    private Connection cnn;
    private JTable invoiceTable;
    private DefaultTableModel tableModel;
    private JTextField dateField;

    public InvoicePanel(Connection cnn) {
        this.cnn = cnn;
        setLayout(new BorderLayout());
        setBackground(new Color(255, 228, 225));
        initUI();
        loadInvoices();
    }

    private void initUI() {
        // Tiêu đề
        JLabel titleLabel = new JLabel("Quản lý hóa đơn", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 105, 180));

        // Panel tìm kiếm theo ngày và nút xóa
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(255, 228, 225));
        dateField = new JTextField(10);
        dateField.setFont(new Font("Arial", Font.PLAIN, 14));
        dateField.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 1));
        
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(255, 182, 193));
        searchButton.setFont(new Font("Arial", Font.BOLD, 13));
        searchButton.addActionListener(e -> searchInvoices());

        JButton deleteButton = new JButton("Xóa");
        deleteButton.setBackground(new Color(255, 182, 193));
        deleteButton.setFont(new Font("Arial", Font.BOLD, 13));
        deleteButton.addActionListener(e -> deleteInvoice());

        searchPanel.add(new JLabel("Ngày (YYYY-MM-DD):"));
        searchPanel.add(dateField);
        searchPanel.add(searchButton);
        searchPanel.add(deleteButton);

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 228, 225));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Bảng hóa đơn
        String[] columns = {"Số hóa đơn", "Ngày lập", "Tên khách hàng", "Tổng tiền"};
        tableModel = new DefaultTableModel(columns, 0);
        invoiceTable = new JTable(tableModel);
        invoiceTable.setRowHeight(25);
        invoiceTable.setFont(new Font("Arial", Font.PLAIN, 14));
        invoiceTable.setBackground(new Color(255, 245, 238));
        invoiceTable.setDefaultEditor(Object.class, null); // Vô hiệu hóa chỉnh sửa ô
        invoiceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showInvoiceDetails();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        add(scrollPane, BorderLayout.CENTER);

        // Ghi chú
        JLabel noteLabel = new JLabel("*Nhấp đúp vào hóa đơn để xem chi tiết*", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);
        add(noteLabel, BorderLayout.SOUTH);
    }

    private void loadInvoices() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT h.SoHoaDon, h.NgayLapHoaDon, k.TenKhachHang, SUM(ct.SoLuong * ct.DonGia) as TongTien " +
                           "FROM HoaDon h " +
                           "LEFT JOIN KhachHang k ON h.MaKhachHang = k.MaKhachHang " +
                           "JOIN ChiTietHoaDon ct ON h.SoHoaDon = ct.SoHoaDon " +
                           "GROUP BY h.SoHoaDon, h.NgayLapHoaDon, k.TenKhachHang";
            PreparedStatement stmt = cnn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("SoHoaDon"),
                    rs.getDate("NgayLapHoaDon"),
                    rs.getString("TenKhachHang") != null ? rs.getString("TenKhachHang") : "Khách vãng lai",
                    rs.getDouble("TongTien")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchInvoices() {
        String date = dateField.getText().trim();
        if (date.isEmpty()) {
            loadInvoices();
            return;
        }

        try {
            String query = "SELECT h.SoHoaDon, h.NgayLapHoaDon, k.TenKhachHang, SUM(ct.SoLuong * ct.DonGia) as TongTien " +
                           "FROM HoaDon h " +
                           "LEFT JOIN KhachHang k ON h.MaKhachHang = k.MaKhachHang " +
                           "JOIN ChiTietHoaDon ct ON h.SoHoaDon = ct.SoHoaDon " +
                           "WHERE h.NgayLapHoaDon = ? " +
                           "GROUP BY h.SoHoaDon, h.NgayLapHoaDon, k.TenKhachHang";
            PreparedStatement stmt = cnn.prepareStatement(query);
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("SoHoaDon"),
                    rs.getDate("NgayLapHoaDon"),
                    rs.getString("TenKhachHang") != null ? rs.getString("TenKhachHang") : "Khách vãng lai",
                    rs.getDouble("TongTien")
                });
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showInvoiceDetails() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int soHoaDon = (int) tableModel.getValueAt(selectedRow, 0);
        new InvoiceDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), soHoaDon, cnn).setVisible(true);
    }

    private void deleteInvoice() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int soHoaDon = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa hóa đơn #" + soHoaDon + "?", 
            "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Bắt đầu giao dịch
                cnn.setAutoCommit(false);

                // Xóa chi tiết hóa đơn trước
                String deleteDetailsQuery = "DELETE FROM ChiTietHoaDon WHERE SoHoaDon = ?";
                PreparedStatement stmtDetails = cnn.prepareStatement(deleteDetailsQuery);
                stmtDetails.setInt(1, soHoaDon);
                stmtDetails.executeUpdate();
                stmtDetails.close();

                // Xóa hóa đơn
                String deleteInvoiceQuery = "DELETE FROM HoaDon WHERE SoHoaDon = ?";
                PreparedStatement stmtInvoice = cnn.prepareStatement(deleteInvoiceQuery);
                stmtInvoice.setInt(1, soHoaDon);
                stmtInvoice.executeUpdate();
                stmtInvoice.close();

                // Commit giao dịch
                cnn.commit();

                // Làm mới bảng
                loadInvoices();
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                try {
                    cnn.rollback(); // Rollback nếu có lỗi
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    cnn.setAutoCommit(true); // Khôi phục chế độ auto-commit
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}