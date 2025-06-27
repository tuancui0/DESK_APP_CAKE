package model;

import java.sql.Date;

public class Order {
    private int orderId;
    private Date orderDate;
    private Integer employeeId;
    private Integer customerId;
    private String status;

    public Order() {}

    public Order(int orderId, Date orderDate, Integer employeeId, Integer customerId, String status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.employeeId = employeeId;
        this.customerId = customerId;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}