package model;

public class Product {
    private int id;
    private String name;
    private String type;
    private String description;
    private double importPrice;
    private double salePrice;
    private int quantity;
    private String status;

    public Product() {
    }

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Product(int id, String name, String type, String description, double importPrice,
                   double salePrice, int quantity, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.importPrice = importPrice;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public int getProductId() {
        return getId();
    }

    public String getProductName() {
        return getName();
    }

    public String getProductType() {
        return getType();
    }

    public double getPurchasePrice() {
        return getImportPrice();
    }

    public double getSellingPrice() {
        return getSalePrice();
    }

}