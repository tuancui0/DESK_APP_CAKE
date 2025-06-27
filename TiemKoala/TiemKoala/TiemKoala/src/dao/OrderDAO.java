package dao;

import model.Order;

import java.sql.*;
import java.util.ArrayList;

public class OrderDAO {
    private Connection conn;

    public OrderDAO(Connection conn) {
        this.conn = conn;
    }

    public ArrayList<Order> getAllOrders() throws SQLException {
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM DonHang";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("MaDonHang"));
            order.setOrderDate(rs.getDate("NgayLapDon"));
            order.setEmployeeId(rs.getObject("MaNhanVien") != null ? rs.getInt("MaNhanVien") : null);
            order.setCustomerId(rs.getObject("MaKhachHang") != null ? rs.getInt("MaKhachHang") : null);
            order.setStatus(rs.getString("TrangThai"));
            orders.add(order);
        }

        rs.close();
        stmt.close();
        return orders;
    }

    public ArrayList<Order> searchById(String keywordr) throws SQLException {
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM DonHang WHERE MaDonHang LIKE ? OR MaNhanVien LIKE ? OR MaKhachHang LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, "%" + keywordr + "%");
        stmt.setString(2, "%" + keywordr+ "%");
        stmt.setString(3, "%" + keywordr + "%");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("MaDonHang"));
            order.setOrderDate(rs.getDate("NgayLapDon"));
            order.setEmployeeId(rs.getObject("MaNhanVien") != null ? rs.getInt("MaNhanVien") : null);
            order.setCustomerId(rs.getObject("MaKhachHang") != null ? rs.getInt("MaKhachHang") : null);
            order.setStatus(rs.getString("TrangThai"));
            orders.add(order);
        }

        rs.close();
        stmt.close();
        return orders;
    }

    public boolean insertOrder(Order order) throws SQLException {
        String query = "INSERT INTO DonHang (NgayLapDon, MaNhanVien, MaKhachHang, TrangThai) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setDate(1, order.getOrderDate());
        stmt.setObject(2, order.getEmployeeId(), Types.INTEGER);
        stmt.setObject(3, order.getCustomerId(), Types.INTEGER);
        stmt.setString(4, order.getStatus());
        int rowsAffected = stmt.executeUpdate();
        stmt.close();
        return rowsAffected > 0;
    }

    public boolean updateOrder(Order order) throws SQLException {
        String query = "UPDATE DonHang SET NgayLapDon = ?, MaNhanVien = ?, MaKhachHang = ?, TrangThai = ? WHERE MaDonHang = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setDate(1, order.getOrderDate());
        stmt.setObject(2, order.getEmployeeId(), Types.INTEGER);
        stmt.setObject(3, order.getCustomerId(), Types.INTEGER);
        stmt.setString(4, order.getStatus());
        stmt.setInt(5, order.getOrderId());
        int rowsAffected = stmt.executeUpdate();
        stmt.close();
        return rowsAffected > 0;
    }

    public boolean deleteOrder(int orderId) throws SQLException {
        String query = "DELETE FROM DonHang WHERE MaDonHang = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, orderId);
        int rowsAffected = stmt.executeUpdate();
        stmt.close();
        return rowsAffected > 0;
    }
}