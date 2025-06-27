package dao;

import model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class EmployeeDAO {
    private Connection conn;

    public EmployeeDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy tất cả nhân viên
    public ArrayList<Employee> getAllEmployees() throws Exception {
        ArrayList<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Employee employee = new Employee(
                rs.getInt("MaNhanVien"),
                rs.getString("TenNhanVien"),
                rs.getString("GioiTinh"),
                rs.getDate("NgaySinh"),
                rs.getString("DiaChi"),
                rs.getString("SoDienThoai"),
                rs.getDouble("Luong")
            );
            list.add(employee);
        }
        rs.close();
        stmt.close();
        return list;
    }

    // Thêm một nhân viên mới
    public boolean insertEmployee(Employee employee) throws Exception {
        String sql = "INSERT INTO NhanVien(TenNhanVien, GioiTinh, NgaySinh, DiaChi, SoDienThoai, Luong) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, employee.getEmployeeName());
        stmt.setString(2, employee.getGender());
        stmt.setDate(3, new java.sql.Date(employee.getDateOfBirth().getTime()));
        stmt.setString(4, employee.getAddress());
        stmt.setString(5, employee.getPhone());
        stmt.setDouble(6, employee.getSalary());
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Cập nhật thông tin nhân viên
    public boolean updateEmployee(Employee employee) throws Exception {
        String sql = "UPDATE NhanVien SET TenNhanVien=?, GioiTinh=?, NgaySinh=?, DiaChi=?, SoDienThoai=?, Luong=? WHERE MaNhanVien=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, employee.getEmployeeName());
        stmt.setString(2, employee.getGender());
        stmt.setDate(3, new java.sql.Date(employee.getDateOfBirth().getTime()));
        stmt.setString(4, employee.getAddress());
        stmt.setString(5, employee.getPhone());
        stmt.setDouble(6, employee.getSalary());
        stmt.setInt(7, employee.getEmployeeId());
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Xóa nhân viên
    public boolean deleteEmployee(int employeeId) throws Exception {
        String sql = "DELETE FROM NhanVien WHERE MaNhanVien = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, employeeId);
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Tìm kiếm nhân viên theo tên
    public ArrayList<Employee> searchByName(String name) throws Exception {
        ArrayList<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE TenNhanVien LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + name + "%");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Employee employee = new Employee(
                rs.getInt("MaNhanVien"),
                rs.getString("TenNhanVien"),
                rs.getString("GioiTinh"),
                rs.getDate("NgaySinh"),
                rs.getString("DiaChi"),
                rs.getString("SoDienThoai"),
                rs.getDouble("Luong")
            );
            list.add(employee);
        }
        rs.close();
        stmt.close();
        return list;
    }
    
    // Lấy các loại giới tính duy nhất từ bảng NhanVien
    public ArrayList<String> getGenderTypes() throws SQLException {
        ArrayList<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT GioiTinh FROM NhanVien WHERE GioiTinh IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                types.add(rs.getString("GioiTinh"));
            }
        }
        return types;
    }

    // Thêm loại giới tính mới (chỉ kiểm tra và thông báo, không chèn trực tiếp)
    public boolean insertGenderType(String genderType) throws SQLException {
        if (genderType == null || genderType.trim().isEmpty()) {
            throw new IllegalArgumentException("Giới tính không được null hoặc rỗng");
        }

        // Thay vì chèn trực tiếp, chúng ta có thể kiểm tra xem giới tính đã tồn tại chưa
        String sqlCheck = "SELECT COUNT(*) FROM NhanVien WHERE GioiTinh = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setString(1, genderType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                return false; // Yêu cầu người dùng thêm qua insertEmployee
            }
        }
        return true; // Nếu đã tồn tại, coi như hợp lệ
    }
}