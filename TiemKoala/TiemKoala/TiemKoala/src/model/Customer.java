package model;

public class Customer {
    private int id;
    private String name;
    private String address;
    private String gender;
    private String phone;

    public Customer() {
    }

    public Customer(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Customer(int id, String name, String address, String gender, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.gender = gender;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public int getCustomerId() {
        return getId();
    }

    public String getCustomerName() {
        return getName();
    }
}
