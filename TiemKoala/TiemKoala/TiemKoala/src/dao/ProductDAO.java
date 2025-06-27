package dao;

import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private Connection conn;

    public ProductDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy tất cả sản phẩm
    public ArrayList<Product> getAllProducts() throws Exception {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Product product = new Product(
                rs.getInt("MaSanPham"),
                rs.getString("TenSanPham"),
                rs.getString("LoaiSanPham"),
                rs.getString("MoTa"),
                rs.getDouble("GiaNhap"),
                rs.getDouble("GiaBan"),
                rs.getInt("SoLuong"),
                rs.getString("TrangThai")
            );
            list.add(product);
        }
        rs.close();
        stmt.close();
        return list;
    }

    // Thêm một sản phẩm mới
    public boolean insertProduct(Product product) throws Exception {
        String sql = "INSERT INTO SanPham(TenSanPham, LoaiSanPham, MoTa, GiaNhap, GiaBan, SoLuong, TrangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, product.getProductName());
        stmt.setString(2, product.getProductType());
        stmt.setString(3, product.getDescription());
        stmt.setDouble(4, product.getPurchasePrice());
        stmt.setDouble(5, product.getSellingPrice());
        stmt.setInt(6, product.getQuantity());
        stmt.setString(7, product.getStatus());
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Cập nhật thông tin sản phẩm
    public boolean updateProduct(Product product) throws Exception {
        String sql = "UPDATE SanPham SET TenSanPham=?, LoaiSanPham=?, MoTa=?, GiaNhap=?, GiaBan=?, SoLuong=?, TrangThai=? WHERE MaSanPham=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, product.getProductName());
        stmt.setString(2, product.getProductType());
        stmt.setString(3, product.getDescription());
        stmt.setDouble(4, product.getPurchasePrice());
        stmt.setDouble(5, product.getSellingPrice());
        stmt.setInt(6, product.getQuantity());
        stmt.setString(7, product.getStatus());
        stmt.setInt(8, product.getProductId());
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Xóa sản phẩm
    public boolean deleteProduct(int productId) throws Exception {
        String sql = "DELETE FROM SanPham WHERE MaSanPham = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, productId);
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }


    // Tìm kiếm sản phẩm theo tên
    public ArrayList<Product> searchByName(String name) throws Exception {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE TenSanPham LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + name + "%");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Product product = new Product(
                rs.getInt("MaSanPham"),
                rs.getString("TenSanPham"),
                rs.getString("LoaiSanPham"),
                rs.getString("MoTa"),
                rs.getDouble("GiaNhap"),
                rs.getDouble("GiaBan"),
                rs.getInt("SoLuong"),
                rs.getString("TrangThai")
            );
            list.add(product);
        }
        rs.close();
        stmt.close();
        return list;
    }
    public ArrayList<String> getProductTypes() throws SQLException {
        ArrayList<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT LoaiSanPham FROM SanPham WHERE LoaiSanPham IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                types.add(rs.getString("LoaiSanPham"));
            }
        }
        return types;
    }

    // Thêm loại sản phẩm mới (nếu cần lưu vào cơ sở dữ liệu)
    public boolean insertProductType(String productType) throws SQLException {
        if (productType == null || productType.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại sản phẩm không được null hoặc rỗng");
        }

        // Giả sử bạn có một bảng riêng để lưu loại sản phẩm (ProductTypes)
        // Nếu không, bạn có thể chỉ cần thêm trực tiếp vào bảng SanPham
        String sql = "INSERT INTO SanPham(LoaiSanPham) VALUES (?) ON DUPLICATE KEY UPDATE LoaiSanPham = LoaiSanPham";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productType);
            return stmt.executeUpdate() > 0;
        }
    }
}