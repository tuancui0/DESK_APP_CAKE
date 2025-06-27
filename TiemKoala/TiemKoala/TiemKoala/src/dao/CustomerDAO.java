package dao;

import model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class CustomerDAO {
    private Connection conn;

    public CustomerDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy tất cả khách hàng
    public ArrayList<Customer> getAllCustomers() throws Exception {
        ArrayList<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Customer customer = new Customer(
                rs.getInt("MaKhachHang"),
                rs.getString("TenKhachHang"),
                rs.getString("DiaChi"),
                rs.getString("GioiTinh"),
                rs.getString("SoDienThoai")
            );
            list.add(customer);
        }
        rs.close();
        stmt.close();
        return list;
    }

    // Thêm một khách hàng mới
    public boolean insertCustomer(Customer customer) throws Exception {
        String sql = "INSERT INTO KhachHang(TenKhachHang, DiaChi, GioiTinh, SoDienThoai) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, customer.getCustomerName());
        stmt.setString(2, customer.getAddress());
        stmt.setString(3, customer.getGender());
        stmt.setString(4, customer.getPhone());
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Cập nhật thông tin khách hàng
    public boolean updateCustomer(Customer customer) throws Exception {
        String sql = "UPDATE KhachHang SET TenKhachHang=?, DiaChi=?, GioiTinh=?, SoDienThoai=? WHERE MaKhachHang=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, customer.getCustomerName());
        stmt.setString(2, customer.getAddress());
        stmt.setString(3, customer.getGender());
        stmt.setString(4, customer.getPhone());
        stmt.setInt(5, customer.getCustomerId());
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Xóa khách hàng
    public boolean deleteCustomer(int customerId) throws Exception {
        String sql = "DELETE FROM KhachHang WHERE MaKhachHang = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, customerId);
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // Tìm kiếm khách hàng theo tên
    public ArrayList<Customer> searchByName(String name) throws Exception {
        ArrayList<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang WHERE TenKhachHang LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + name + "%");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Customer customer = new Customer(
                rs.getInt("MaKhachHang"),
                rs.getString("TenKhachHang"),
                rs.getString("DiaChi"),
                rs.getString("GioiTinh"),
                rs.getString("SoDienThoai")
            );
            list.add(customer);
        }
        rs.close();
        stmt.close();
        return list;
    }

	public ArrayList<String> getGenderTypes() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("Nữ");
        list.add("Nam");
        //list.add("Khác");
        return list;
    }
	
}