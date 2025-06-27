package dao;

import java.sql.*;
import java.util.*;
import model.*;
import utils.DBConnection;

public class InvoiceDetailDAO {

    public List<InvoiceDetail> getDetailsByInvoiceId(int soHoaDon) {
        List<InvoiceDetail> details = new ArrayList<>();

        String sql = "SELECT sp.MaSanPham, sp.TenSanPham, cthd.SoLuong, cthd.DonGia " +
                     "FROM ChiTietHoaDon cthd " +
                     "JOIN SanPham sp ON cthd.MaSanPham = sp.MaSanPham " +
                     "WHERE cthd.SoHoaDon = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, soHoaDon);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("MaSanPham"));
                product.setName(rs.getString("TenSanPham"));
                
                int quantity = rs.getInt("SoLuong");
                double price = rs.getDouble("DonGia");

                details.add(new InvoiceDetail(product, quantity, price));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }
}
