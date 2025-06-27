package model;

import java.util.Date;

public class Employee {
    private int employeeId; // MaNhanVien
    private String employeeName; // TenNhanVien
    private String gender; // GioiTinh
    private Date dateOfBirth; // NgaySinh
    private String address; // DiaChi
    private String phone; // SoDienThoai
    private double salary; // Luong

    // Constructor mặc định
    public Employee() {
    }

    // Constructor với tham số
    public Employee(int employeeId, String employeeName, String gender, Date dateOfBirth, String address, String phone, double salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.salary = salary;
    }

    // Getters and Setters
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}