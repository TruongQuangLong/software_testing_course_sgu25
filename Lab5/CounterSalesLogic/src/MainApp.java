import java.math.BigDecimal;

// MainApp.java
public class MainApp {
    public static void main(String[] args) {
        ProductService service = new ProductService();

        // --- Demo Product Number Test Cases ---
        System.out.println("--- Product Number Tests ---");
        testProductNumber(service, "1234567890"); // PN-EP-01: Hợp lệ, Không chịu thuế
        testProductNumber(service, "1111111111"); // PN-EP-02: Hợp lệ, Chịu thuế
        testProductNumber(service, "9999999999"); // PN-EP-04: Không tìm thấy
        testProductNumber(service, "123456789");  // PN-BV-01: < 10 chữ số

        // --- Demo Profit Margin Test Cases ---
        System.out.println("\n--- Profit Margin Tests ---");
        testProfitMargin(service, "30.00", "100.00");    // PM-BV-01: Tối thiểu 30%
        testProfitMargin(service, "29.99", "100.00");    // PM-BV-02: Dưới ngưỡng
        testProfitMargin(service, "100.00", "500000.00"); // PM-BV-03: Vượt Max Currency
    }

    private static void testProductNumber(ProductService service, String number) {
        try {
            Product p = service.findProduct(number);
            String taxIndicator = p.isTaxable() ? "*" : "";
            System.out.printf("Product %s: Found (Taxable: %s%s)\n", number, p.isTaxable(), taxIndicator);
        } catch (ItemNotFoundException | IllegalArgumentException e) {
            System.out.printf("Product %s: FAILED - %s\n", number, e.getMessage());
        }
    }

    private static void testProfitMargin(ProductService service, String margin, String wac) {
        BigDecimal wacDecimal = new BigDecimal(wac);
        BigDecimal marginDecimal = new BigDecimal(margin);
        try {
            BigDecimal newPrice = service.calculateNewPriceFromMargin(marginDecimal, wacDecimal);
            System.out.printf("Margin %.2f%%, WAC $%.2f -> New Price: $%.2f (SUCCESS)\n", 
                              marginDecimal, wacDecimal, newPrice);
        } catch (BusinessRuleException e) {
            System.out.printf("Margin %.2f%%, WAC $%.2f -> FAILED - %s\n", 
                              marginDecimal, wacDecimal, e.getMessage());
        }
    }
}