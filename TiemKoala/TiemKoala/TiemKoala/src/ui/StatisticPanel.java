package ui;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook; 
import java.io.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class StatisticPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JPanel chartPanel, revenuePanel, topProductsPanel, recentOrdersPanel;
    private JComboBox<String> timeFilterComboBox;
    private JTextField startDateField, endDateField;
    private JTable topProductsTable, recentOrdersTable;
    private Connection conn;
    private JLabel noDataLabel;

    public StatisticPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        initComponents();
    }

    // Khởi tạo các thành phần giao diện chính
    private void initComponents() {
        setBackground(new Color(255, 228, 225));

        // Tạo JTabbedPane để chứa các tab thống kê
        tabbedPane = new JTabbedPane();
        chartPanel = new JPanel(new BorderLayout());
        revenuePanel = new JPanel(new BorderLayout());
        topProductsPanel = new JPanel(new BorderLayout());
        recentOrdersPanel = new JPanel(new BorderLayout());

        // Tạo thanh lọc thời gian
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeFilterComboBox = new JComboBox<>(new String[]{"Ngày", "Tuần", "Tháng", "Năm"});
        timeFilterComboBox.addActionListener(e -> updateStatistics());
        filterPanel.add(new JLabel("Lọc theo:"));
        filterPanel.add(timeFilterComboBox);

        // Thêm ô nhập ngày tùy chỉnh (từ ngày đến ngày)
        filterPanel.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        startDateField = new JTextField(10);
        filterPanel.add(startDateField);
        filterPanel.add(new JLabel("Đến ngày (yyyy-MM-dd):"));
        endDateField = new JTextField(10);
        filterPanel.add(endDateField);

        // Thêm nút xuất Excel
        JButton exportExcelButton = new JButton("Xuất Excel");
        exportExcelButton.addActionListener(e -> exportToExcel());
        filterPanel.add(exportExcelButton);

        // Thêm filterPanel vào panel chính
        add(filterPanel, BorderLayout.NORTH);

        // Khởi tạo các tab ban đầu
        updateChart();
        updateRevenueAnalysis();
        initTopProductsTable();
        initRecentOrdersTable();

        // Thêm các tab vào tabbedPane
        tabbedPane.addTab("Biểu đồ", chartPanel);
        tabbedPane.addTab("Doanh thu", revenuePanel);
        tabbedPane.addTab("Sản phẩm bán chạy", topProductsPanel);
        tabbedPane.addTab("Đơn hàng gần đây", recentOrdersPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Cập nhật dữ liệu ban đầu
        updateStatistics();
    }

    // Cập nhật toàn bộ thống kê khi thay đổi bộ lọc thời gian
    private void updateStatistics() {
        String timeFilter = timeFilterComboBox.getSelectedItem().toString();
        String dateCondition = getDateCondition(timeFilter);
        if (dateCondition == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng ngày (yyyy-MM-dd)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateChart();
        updateRevenueAnalysis();
        updateTopProductsTable();
        updateRecentOrdersTable();
    }

    // Tạo điều kiện lọc ngày dựa trên lựa chọn thời gian hoặc khoảng ngày tùy chỉnh
    private String getDateCondition(String timeFilter) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        // Kiểm tra nếu người dùng nhập khoảng ngày tùy chỉnh
        String startDateStr = startDateField.getText().trim();
        String endDateStr = endDateField.getText().trim();

        if (!startDateStr.isEmpty() && !endDateStr.isEmpty()) {
            try {
                Date startDate = sdf.parse(startDateStr);
                Date endDate = sdf.parse(endDateStr);
                if (endDate.before(startDate)) {
                    JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                return "hd.NgayLapHoaDon BETWEEN '" + sdf.format(startDate) + "' AND '" + sdf.format(endDate) + "'";
            } catch (ParseException e) {
                return null;
            }
        } else {
            // Sử dụng các khoảng thời gian mặc định nếu không nhập ngày tùy chỉnh
            switch (timeFilter) {
                case "Ngày":
                    return "DATE(hd.NgayLapHoaDon) = CURDATE()";
                case "Tuần":
                    return "hd.NgayLapHoaDon BETWEEN DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND CURDATE()";
                case "Tháng":
                    return "hd.NgayLapHoaDon BETWEEN DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND CURDATE()";
                case "Năm":
                    return "hd.NgayLapHoaDon BETWEEN DATE_SUB(CURDATE(), INTERVAL 365 DAY) AND CURDATE()";
                default:
                    return "hd.NgayLapHoaDon >= '1970-01-01'";
            }
        }
    }

    // Cập nhật biểu đồ xu hướng doanh thu (Line Chart)
    private void updateChart() {
        chartPanel.removeAll();
        String timeFilter = timeFilterComboBox.getSelectedItem().toString();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String dateCondition = getDateCondition(timeFilter);

        // Kiểm tra lỗi định dạng ngày
        if (dateCondition == null) {
            JLabel errorLabel = new JLabel("Lỗi định dạng ngày. Vui lòng nhập đúng (yyyy-MM-dd).", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            chartPanel.add(errorLabel, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            return;
        }

        try {
            // Xác định cách nhóm dữ liệu dựa trên bộ lọc thời gian
            String groupByFormat;
            switch (timeFilter) {
                case "Ngày":
                    groupByFormat = "%Y-%m-%d";
                    break;
                case "Tuần":
                    groupByFormat = "%Y-%u";
                    break;
                case "Tháng":
                    groupByFormat = "%Y-%m";
                    break;
                case "Năm":
                    groupByFormat = "%Y";
                    break;
                default:
                    groupByFormat = "%Y-%m-%d";
            }

            // Truy vấn dữ liệu doanh thu từ cơ sở dữ liệu
            String query = "SELECT DATE_FORMAT(hd.NgayLapHoaDon, ?) as date, " +
                    "SUM(ct.SoLuong * ct.DonGia) as revenue " +
                    "FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.SoHoaDon = hd.SoHoaDon " +
                    "WHERE " + dateCondition + " " +
                    "GROUP BY DATE_FORMAT(hd.NgayLapHoaDon, ?) " +
                    "ORDER BY date";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, groupByFormat);
            stmt.setString(2, groupByFormat);
            ResultSet rs = stmt.executeQuery();

            // Xử lý dữ liệu để vẽ biểu đồ
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String dateLabel = rs.getString("date");
                dataset.addValue(rs.getDouble("revenue"), "Doanh thu", dateLabel);
            }

            rs.close();
            stmt.close();

            // Hiển thị thông báo nếu không có dữ liệu
            if (!hasData) {
                JLabel noDataLabel = new JLabel("Không có dữ liệu để hiển thị.", SwingConstants.CENTER);
                noDataLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                chartPanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                // Tạo và hiển thị biểu đồ đường (Line Chart)
                JFreeChart chart = ChartFactory.createLineChart(
                    "Xu hướng doanh thu",
                    "Thời gian",
                    "Doanh thu (VNĐ)",
                    dataset
                );
                chart.getPlot().setBackgroundPaint(new Color(255, 245, 238));
                chart.getPlot().setOutlineVisible(false);
                ChartPanel chartPanelContent = new ChartPanel(chart);
                chartPanel.add(chartPanelContent, BorderLayout.CENTER);
            }

            chartPanel.revalidate();
            chartPanel.repaint();

        } catch (SQLException e) {
            // Xử lý lỗi SQL
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Lỗi khi tải dữ liệu biểu đồ.", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            chartPanel.add(errorLabel, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
        }
    }

    // Cập nhật tab phân tích doanh thu với bảng và biểu đồ tròn (Pie Chart)
    private void updateRevenueAnalysis() {
        revenuePanel.removeAll();
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Danh mục", "Doanh thu", "Tỷ lệ (%)"}, 0
        );
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        revenuePanel.add(scrollPane, BorderLayout.CENTER);

        String dateCondition = getDateCondition(timeFilterComboBox.getSelectedItem().toString());
        if (dateCondition == null) {
            JLabel errorLabel = new JLabel("Lỗi định dạng ngày. Vui lòng nhập đúng (yyyy-MM-dd).", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            revenuePanel.add(errorLabel, BorderLayout.CENTER);
            revenuePanel.revalidate();
            revenuePanel.repaint();
            return;
        }

        try {
            // Truy vấn dữ liệu doanh thu theo danh mục sản phẩm
            String query = "SELECT sp.LoaiSanPham, SUM(ct.SoLuong * ct.DonGia) as revenue " +
                "FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.SoHoaDon = hd.SoHoaDon " +
                "JOIN SanPham sp ON ct.MaSanPham = sp.MaSanPham " +
                "WHERE " + dateCondition +
                " GROUP BY sp.LoaiSanPham";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Xử lý dữ liệu để hiển thị bảng và biểu đồ tròn
            double totalRevenue = 0;
            DefaultPieDataset pieDataset = new DefaultPieDataset();
            Vector<Object[]> rows = new Vector<>();
            while (rs.next()) {
                double revenue = rs.getDouble("revenue");
                totalRevenue += revenue;
                String category = rs.getString("LoaiSanPham");
                rows.add(new Object[]{category, revenue, 0.0});
                pieDataset.setValue(category, revenue);
            }

            if (rows.isEmpty()) {
                JLabel noDataLabel = new JLabel("Không có dữ liệu doanh thu.", SwingConstants.CENTER);
                noDataLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                revenuePanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                // Tính tỷ lệ phần trăm và hiển thị bảng
                for (Object[] row : rows) {
                    double percentage = ((Double) row[1] / totalRevenue) * 100;
                    row[2] = String.format("%.2f%%", percentage);
                    model.addRow(row);
                }
                // Tạo và hiển thị biểu đồ tròn
                JFreeChart pieChart = ChartFactory.createPieChart(
                    "Tỷ lệ doanh thu theo danh mục",
                    pieDataset,
                    true, true, false
                );
                pieChart.getPlot().setBackgroundPaint(new Color(255, 245, 238));
                ChartPanel pieChartPanel = new ChartPanel(pieChart);
                revenuePanel.add(pieChartPanel, BorderLayout.NORTH);
                revenuePanel.add(scrollPane, BorderLayout.CENTER);
            }

            revenuePanel.revalidate();
            revenuePanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Lỗi khi tải dữ liệu doanh thu.", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            revenuePanel.add(errorLabel, BorderLayout.CENTER);
            revenuePanel.revalidate();
            revenuePanel.repaint();
        }
    }

    // Khởi tạo bảng cho tab "Sản phẩm bán chạy"
    private void initTopProductsTable() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SP", "Tên sản phẩm", "Số lượng bán", "Doanh thu"}, 0
        );
        topProductsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(topProductsTable);
        topProductsPanel.add(scrollPane, BorderLayout.CENTER);
        updateTopProductsTable();
    }

    // Cập nhật tab "Sản phẩm bán chạy" với bảng và biểu đồ cột (Bar Chart)
    private void updateTopProductsTable() {
        topProductsPanel.removeAll();
        String dateCondition = getDateCondition(timeFilterComboBox.getSelectedItem().toString());
        if (dateCondition == null) {
            JLabel errorLabel = new JLabel("Lỗi định dạng ngày. Vui lòng nhập đúng (yyyy-MM-dd).", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            topProductsPanel.add(errorLabel, BorderLayout.CENTER);
            topProductsPanel.revalidate();
            topProductsPanel.repaint();
            return;
        }

        try {
            // Truy vấn top 10 sản phẩm bán chạy
            String query = "SELECT sp.MaSanPham, sp.TenSanPham, SUM(ct.SoLuong) as quantity, " +
                "SUM(ct.SoLuong * ct.DonGia) as revenue " +
                "FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.SoHoaDon = hd.SoHoaDon " +
                "JOIN SanPham sp ON ct.MaSanPham = sp.MaSanPham " +
                "WHERE " + dateCondition +
                " GROUP BY sp.MaSanPham, sp.TenSanPham " +
                "ORDER BY revenue DESC LIMIT 10";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Xử lý dữ liệu để hiển thị bảng và biểu đồ cột
            DefaultTableModel model = (DefaultTableModel) topProductsTable.getModel();
            model.setRowCount(0);
            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int productId = rs.getInt("MaSanPham");
                String productName = rs.getString("TenSanPham");
                int quantity = rs.getInt("quantity");
                double revenue = rs.getDouble("revenue");
                model.addRow(new Object[]{productId, productName, quantity, String.format("%,.0f VNĐ", revenue)});
                barDataset.addValue(revenue, "Doanh thu", productName);
            }

            if (!hasData) {
                JLabel noDataLabel = new JLabel("Không có sản phẩm bán chạy.", SwingConstants.CENTER);
                noDataLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                topProductsPanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                // Tạo và hiển thị biểu đồ cột với kích thước lớn hơn
                JFreeChart barChart = ChartFactory.createBarChart(
                    "Doanh thu sản phẩm bán chạy",
                    "Tên sản phẩm",
                    "Doanh thu (VNĐ)",
                    barDataset
                );
                barChart.getPlot().setBackgroundPaint(new Color(255, 245, 238));
                barChart.getPlot().setOutlineVisible(false);
                ChartPanel barChartPanel = new ChartPanel(barChart);
                barChartPanel.setPreferredSize(new Dimension(400, 250));
                topProductsPanel.add(barChartPanel, BorderLayout.NORTH);
                topProductsPanel.add(new JScrollPane(topProductsTable), BorderLayout.CENTER);
            }

            topProductsPanel.revalidate();
            topProductsPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Lỗi khi tải dữ liệu sản phẩm.", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            topProductsPanel.removeAll();
            topProductsPanel.add(errorLabel, BorderLayout.CENTER);
            topProductsPanel.revalidate();
            topProductsPanel.repaint();
        }
    }

    // Khởi tạo bảng cho tab "Đơn hàng gần đây"
    private void initRecentOrdersTable() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã đơn", "Khách hàng", "Ngày tạo", "Tổng tiền", "Trạng thái"}, 0
        );
        recentOrdersTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(recentOrdersTable);
        recentOrdersPanel.add(scrollPane, BorderLayout.CENTER);
        updateRecentOrdersTable();
    }

    // Cập nhật tab "Đơn hàng gần đây" với bảng dữ liệu
    private void updateRecentOrdersTable() {
        DefaultTableModel model = (DefaultTableModel) recentOrdersTable.getModel();
        model.setRowCount(0);

        String dateCondition = getDateCondition(timeFilterComboBox.getSelectedItem().toString());
        if (dateCondition == null) {
            JLabel errorLabel = new JLabel("Lỗi định dạng ngày. Vui lòng nhập đúng (yyyy-MM-dd).", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            recentOrdersPanel.removeAll();
            recentOrdersPanel.add(errorLabel, BorderLayout.CENTER);
            recentOrdersPanel.revalidate();
            recentOrdersPanel.repaint();
            return;
        }

        try {
            // Truy vấn 10 đơn hàng gần đây nhất
            String query = "SELECT hd.SoHoaDon as MaDonHang, kh.TenKhachHang, hd.NgayLapHoaDon, " +
                "SUM(ct.SoLuong * ct.DonGia) as total, 'Hoàn thành' as TrangThai " +
                "FROM HoaDon hd LEFT JOIN KhachHang kh ON hd.MaKhachHang = kh.MaKhachHang " +
                "JOIN ChiTietHoaDon ct ON hd.SoHoaDon = ct.SoHoaDon " +
                "WHERE " + dateCondition +
                " GROUP BY hd.SoHoaDon, kh.TenKhachHang, hd.NgayLapHoaDon " +
                "ORDER BY hd.NgayLapHoaDon DESC LIMIT 10";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Hiển thị dữ liệu đơn hàng trong bảng
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("MaDonHang"),
                    rs.getString("TenKhachHang") != null ? rs.getString("TenKhachHang") : "Khách lẻ",
                    rs.getDate("NgayLapHoaDon"),
                    String.format("%,.0f VNĐ", rs.getDouble("total")),
                    rs.getString("TrangThai")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Xuất dữ liệu thống kê ra file Excel
    private void exportToExcel() {
        // Tạo hộp thoại chọn nơi lưu file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
        fileChooser.setSelectedFile(new File("ThongKe.xls")); // Đổi từ .xlsx thành .xls
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                // Sử dụng HSSFWorkbook để tạo file .xls
                HSSFWorkbook workbook = new HSSFWorkbook();
                Sheet sheet = workbook.createSheet("Thống kê");

                // Xuất tiêu đề
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Top sản phẩm bán chạy");

                // Xuất tiêu đề cột
                Row headerRow = sheet.createRow(1);
                for (int j = 0; j < topProductsTable.getColumnCount(); j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(topProductsTable.getColumnName(j));
                }

                // Xuất dữ liệu từ bảng "Sản phẩm bán chạy"
                for (int i = 0; i < topProductsTable.getRowCount(); i++) {
                    Row row = sheet.createRow(2 + i);
                    for (int j = 0; j < topProductsTable.getColumnCount(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = topProductsTable.getValueAt(i, j);
                        if (value == null) {
                            cell.setCellValue("");
                        } else if (j == 3) { // Cột "Doanh thu"
                            // Xử lý cột Doanh thu: Loại bỏ " VNĐ" và định dạng số
                            String revenueStr = value.toString().replaceAll("[^0-9]", "");
                            try {
                                double revenue = Double.parseDouble(revenueStr);
                                cell.setCellValue(revenue);
                            } catch (NumberFormatException e) {
                                cell.setCellValue(value.toString());
                            }
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                // Tự động điều chỉnh kích thước cột
                for (int j = 0; j < topProductsTable.getColumnCount(); j++) {
                    sheet.autoSizeColumn(j);
                }

                // Lưu file Excel
                FileOutputStream fileOut = new FileOutputStream(fileToSave);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();
                JOptionPane.showMessageDialog(this, "Xuất Excel thành công! File được lưu tại: " + fileToSave.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}