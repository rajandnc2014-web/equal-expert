import java.math.BigDecimal;
import java.util.List;

public class CartState {
    public final List<String> contents;
    public final BigDecimal subtotal;
    public final BigDecimal tax;
    public final BigDecimal total;

    public CartState(List<String> contents, BigDecimal subtotal, BigDecimal tax, BigDecimal total){
        this.contents = contents;
        this.subtotal = subtotal;
        this.tax= tax;
        this.total = total;
    }
}
