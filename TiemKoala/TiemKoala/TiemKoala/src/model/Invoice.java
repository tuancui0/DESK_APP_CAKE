package model;

import java.util.Date;

public class Invoice {
    private int id;
    private Date date;
    private Customer customer;
    private double total;

    public Invoice(int id, Date date, Customer customer, double total) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getTotal() {
        return total;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
