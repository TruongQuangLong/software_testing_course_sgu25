// Product.java
public class Product {
    private String productNumber;
    private boolean isTaxable; // Thuộc tính chịu thuế (Taxable: Y/N)

    public Product(String productNumber, boolean isTaxable) {
        this.productNumber = productNumber;
        this.isTaxable = isTaxable;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public boolean isTaxable() {
        return isTaxable;
    }
}