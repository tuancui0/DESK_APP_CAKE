package dao;

import model.User;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO {
    private Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public ArrayList<User> getAllUsers() throws SQLException {
        ArrayList<User> list = new ArrayList<>();
        String sql = "SELECT * FROM NguoiDung";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                    rs.getString("TenDangNhap"),
                    rs.getString("MatKhau"),
                    rs.getString("VaiTro"),
                    rs.getObject("MaNhanVien") != null ? rs.getInt("MaNhanVien") : null
                );
                list.add(user);
            }
        }
        return list;
    }

    public boolean insertUser(User user) throws SQLException {
        String sql = "INSERT INTO NguoiDung (TenDangNhap, MatKhau, VaiTro, MaNhanVien) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            if (user.getEmployeeId() != null) {
                stmt.setInt(4, user.getEmployeeId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE NguoiDung SET MatKhau = ?, VaiTro = ?, MaNhanVien = ? WHERE TenDangNhap = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getRole());
            if (user.getEmployeeId() != null) {
                stmt.setInt(3, user.getEmployeeId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, user.getUsername());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM NguoiDung WHERE TenDangNhap = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        }
    }

    public ArrayList<User> searchByName(String keyword) throws SQLException {
        ArrayList<User> list = new ArrayList<>();
        String sql = "SELECT * FROM NguoiDung WHERE TenDangNhap LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("TenDangNhap"),
                        rs.getString("MatKhau"),
                        rs.getString("VaiTro"),
                        rs.getObject("MaNhanVien") != null ? rs.getInt("MaNhanVien") : null
                    );
                    list.add(user);
                }
            }
        }
        return list;
    }
}
