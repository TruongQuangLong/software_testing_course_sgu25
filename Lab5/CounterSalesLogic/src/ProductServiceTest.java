import java.math.BigDecimal;
import java.util.function.Supplier;

// ProductServiceTest.java
public class ProductServiceTest {

    public static void main(String[] args) {
        ProductService service = new ProductService();

        System.out.println("==================================================");
        System.out.println("--- Product Number Tests (EP & BV) ---");
        System.out.println("==================================================");
        
        // ----------------------------------------------------
        // I. Product Number (PN) - Kiểm thử Miền Tương Đương (EP) & Biên (BV)
        // ----------------------------------------------------
        
        // 1. Miền Hợp lệ (Valid Equivalence Partition)
        testFindProduct(service, "1234567890", true, false, null, "PN-EP-01 (Non-Taxable)");
        testFindProduct(service, "1111111111", true, true, null, "PN-EP-02 (Taxable)");
        testFindProduct(service, "0000000001", true, false, null, "PN-EP-03 (Leading Zeros)");
        
        // 2. Miền Không hợp lệ - Item Not Found (EP)
        testFindProduct(service, "9999999999", false, false, ItemNotFoundException.class, "PN-EP-04 (Not Found)");
        
        // 3. Miền Không hợp lệ - Định dạng/Độ dài (EP & BV)
        // BV: Độ dài < 10
        testFindProduct(service, "123456789", false, false, IllegalArgumentException.class, "PN-BV-01 (Length 9)");
        // BV: Độ dài > 10
        testFindProduct(service, "12345678901", false, false, IllegalArgumentException.class, "PN-BV-02 (Length 11)");
        // EP: Độ dài bằng 0 (Rỗng)
        testFindProduct(service, "", false, false, IllegalArgumentException.class, "PN-EP-05 (Empty String)");
        // EP: Null
        testFindProduct(service, null, false, false, IllegalArgumentException.class, "PN-EP-06 (Null)");
        // EP: Chứa ký tự không phải số
        testFindProduct(service, "12345A7890", false, false, IllegalArgumentException.class, "PN-EP-07 (Non-Digit)");


        System.out.println("\n==================================================");
        System.out.println("--- Profit Margin Tests (EP & BV) ---");
        System.out.println("==================================================");

        // ----------------------------------------------------
        // II. Profit Margin (PM) - Kiểm thử Biên (BV) & Miền Tương Đương (EP)
        // ----------------------------------------------------
        
        // 1. Miền Hợp lệ (Valid Equivalence Partition)
        // BV: Ngưỡng dưới 30.00%
        testCalculatePrice(service, "30.00", "100.00", true, new BigDecimal("130.00"), null, "PM-BV-01 (Margin 30.00%)");
        // EP: Lớn hơn 30%
        testCalculatePrice(service, "50.00", "100.00", true, new BigDecimal("150.00"), null, "PM-EP-01 (Margin 50.00%)");
        
        // 2. Miền Không hợp lệ - Ngưỡng Lợi nhuận (BV)
        // BV: Dưới ngưỡng 30.00%
        testCalculatePrice(service, "29.99", "100.00", false, null, BusinessRuleException.class, "PM-BV-02 (Margin 29.99%)");
        
        // 3. Miền Không hợp lệ - Phạm vi Tiền tệ (BV)
        
        // BV: Vượt giới hạn Max $999,999.99 (New Price = $1,000,000.00)
        testCalculatePrice(service, "100.00", "500000.00", false, null, BusinessRuleException.class, "PM-BV-03 (Max Currency Boundary)");
        
        // BV: Đạt giới hạn Max $999,999.99
        // (1 + 100/100) * 499999.995 = 999999.99
        testCalculatePrice(service, "100.00", "499999.995", true, new BigDecimal("999999.99"), null, "PM-BV-04 (Max Currency Limit)");
        
        // BV: Ngưỡng dưới $0.00 (New Price = $0.00)
        testCalculatePrice(service, "30.00", "0.00", true, new BigDecimal("0.00"), null, "PM-BV-05 (Min Currency Limit)");
        
        // EP: WAC = 0, Margin > 30% (Kết quả New Price = 0.00)
        testCalculatePrice(service, "50.00", "0.00", true, new BigDecimal("0.00"), null, "PM-EP-02 (WAC Zero)");
    }
    
    // --- Helper for Product Number ---
    private static void testFindProduct(ProductService service, String number, boolean expectSuccess, boolean expectTaxable, Class<? extends Exception> expectedException, String testId) {
        System.out.printf("[%s] Product %s: ", testId, number);
        try {
            Product p = service.findProduct(number);
            if (!expectSuccess) {
                System.out.printf("FAILED (Expected Exception %s, got Success)\n", expectedException.getSimpleName());
            } else {
                String taxIndicator = p.isTaxable() ? "*" : "";
                if (p.isTaxable() == expectTaxable) {
                     System.out.printf("PASS (Found, Taxable: %s%s)\n", p.isTaxable(), taxIndicator);
                } else {
                    System.out.printf("FAILED (Found, Expected Taxable: %s, Actual: %s)\n", expectTaxable, p.isTaxable());
                }
            }
        } catch (Exception e) {
            if (expectSuccess) {
                System.out.printf("FAILED (Expected Success, got Exception %s: %s)\n", e.getClass().getSimpleName(), e.getMessage());
            } else if (expectedException != null && expectedException.isInstance(e)) {
                System.out.printf("PASS (Expected Exception %s)\n", e.getClass().getSimpleName());
            } else {
                 System.out.printf("FAILED (Expected Exception %s, got %s)\n", expectedException != null ? expectedException.getSimpleName() : "None", e.getClass().getSimpleName());
            }
        }
    }

    // --- Helper for Profit Margin ---
    private static void testCalculatePrice(ProductService service, String margin, String wac, boolean expectSuccess, BigDecimal expectedPrice, Class<? extends Exception> expectedException, String testId) {
        BigDecimal wacDecimal = wac != null ? new BigDecimal(wac) : null;
        BigDecimal marginDecimal = margin != null ? new BigDecimal(margin) : null;
        
        // Xử lý hiển thị cho trường hợp margin/wac là null
        String marginDisplay = marginDecimal != null ? String.format("%.2f%%", marginDecimal) : "null";
        String wacDisplay = wacDecimal != null ? String.format("$%.2f", wacDecimal) : "null";
        
        System.out.printf("[%s] Margin %s, WAC %s: ", testId, marginDisplay, wacDisplay);
        try {
            BigDecimal newPrice = service.calculateNewPriceFromMargin(marginDecimal, wacDecimal);
            if (!expectSuccess) {
                System.out.printf("FAILED (Expected Exception %s, got Price: $%.2f)\n", expectedException != null ? expectedException.getSimpleName() : "None", newPrice);
            } else if (newPrice.compareTo(expectedPrice) == 0) {
                 System.out.printf("PASS (New Price: $%.2f)\n", newPrice);
            } else {
                 System.out.printf("FAILED (Expected Price: $%.2f, Actual: $%.2f)\n", expectedPrice, newPrice);
            }
        } catch (Exception e) {
            if (expectSuccess) {
                System.out.printf("FAILED (Expected Price, got Exception %s: %s)\n", e.getClass().getSimpleName(), e.getMessage());
            } else if (expectedException != null && expectedException.isInstance(e)) {
                 System.out.printf("PASS (Expected Exception %s)\n", e.getClass().getSimpleName());
            } else {
                 System.out.printf("FAILED (Expected Exception %s, got %s)\n", expectedException != null ? expectedException.getSimpleName() : "None", e.getClass().getSimpleName());
            }
        }
    }
}