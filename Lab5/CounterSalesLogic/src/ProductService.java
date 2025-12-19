import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class ProductService {

    // Hằng số nghiệp vụ
    private static final BigDecimal MIN_PROFIT_MARGIN_PERCENT = new BigDecimal("30.00");
    private static final BigDecimal MAX_CURRENCY_VALUE = new BigDecimal("999999.99");
    private static final Pattern PRODUCT_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");

    // --- Logic Product Number ---

    public Product findProduct(String productNumber) {
        // 1. Kiểm tra định dạng 10 chữ số
        if (productNumber == null || !PRODUCT_NUMBER_PATTERN.matcher(productNumber).matches()) {
            throw new IllegalArgumentException("Product Number must be exactly 10 digits.");
        }

        // 2. Logic tìm kiếm (Mô phỏng)
        if ("1234567890".equals(productNumber)) {
            return new Product(productNumber, false); // Không chịu thuế (Sử dụng cho test PN-EP-01)
        } else if ("1111111111".equals(productNumber)) {
            return new Product(productNumber, true); // Chịu thuế (Sử dụng cho test PN-EP-02)
        } else if ("0000000001".equals(productNumber)) {
            return new Product(productNumber, false); // Có số 0 ở đầu (Sử dụng cho test PN-EP-03)
        } else {
            // Không tìm thấy (Sử dụng cho test PN-EP-04 và các trường hợp khác)
            throw new ItemNotFoundException("Item not found or similar message should be displayed to the user.");
        }
    }

    // --- Logic Profit Margin ---

    public BigDecimal calculateNewPriceFromMargin(BigDecimal profitMarginPercent, BigDecimal wac) {
        // Kiểm tra null cho tham số đầu vào (cần thêm nếu không dùng BigDecimal)
        if (profitMarginPercent == null || wac == null) {
             throw new IllegalArgumentException("Profit margin and WAC cannot be null.");
        }

        // 1. Kiểm tra Ngưỡng Lợi nhuận (ít nhất 30%)
        if (profitMarginPercent.compareTo(MIN_PROFIT_MARGIN_PERCENT) < 0) {
            throw new BusinessRuleException("Selling must make at least 30% profit.");
        }

        // 2. Tính toán New Price
        // New Price = ((Margin / 100) + 1) * WAC
        
        // Chia Margin cho 100
        BigDecimal marginFactor = profitMarginPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        // Thêm 1
        BigDecimal multiplier = marginFactor.add(BigDecimal.ONE);
        
        // Nhân với WAC
        BigDecimal newPrice = multiplier.multiply(wac);
        
        // Làm tròn đến 2 chữ số thập phân cho tiền tệ
        newPrice = newPrice.setScale(2, RoundingMode.HALF_UP); 

        // 3. Kiểm tra Phạm vi Tiền tệ ($0 - 999,999.99)
        // newPrice < 0 HOẶC newPrice > MAX_CURRENCY_VALUE
        if (newPrice.compareTo(BigDecimal.ZERO) < 0 || newPrice.compareTo(MAX_CURRENCY_VALUE) > 0) {
            throw new BusinessRuleException("Acceptable range of currency is $0 - 999,999.99.");
        }

        return newPrice;
    }
}