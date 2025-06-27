package dao;

import java.sql.*;
import java.util.*;
import java.util.Date;
import model.*;
import utils.DBConnection;

public class InvoiceDAO {

    public List<Invoice> getInvoicesByDate(Date date) {
        List<Invoice> invoices = new ArrayList<>();

        String sql = "SELECT hd.SoHoaDon, hd.NgayLapHoaDon, kh.MaKhachHang, kh.TenKhachHang, " +
                     "SUM(cthd.SoLuong * cthd.DonGia) AS TongTien " +
                     "FROM HoaDon hd " +
                     "JOIN KhachHang kh ON hd.MaKhachHang = kh.MaKhachHang " +
                     "JOIN ChiTietHoaDon cthd ON hd.SoHoaDon = cthd.SoHoaDon " +
                     "WHERE hd.NgayLapHoaDon = ? " +
                     "GROUP BY hd.SoHoaDon, hd.NgayLapHoaDon, kh.MaKhachHang, kh.TenKhachHang";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int soHoaDon = rs.getInt("SoHoaDon");
                Date ngayLap = rs.getDate("NgayLapHoaDon");
                Customer khach = new Customer();
                khach.setId(rs.getInt("MaKhachHang"));
                khach.setName(rs.getString("TenKhachHang"));
                double tongTien = rs.getDouble("TongTien");

                invoices.add(new Invoice(soHoaDon, ngayLap, khach, tongTien));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return invoices;
    }
}
