import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShoppingCart {
    private final Map<String, Integer> items = new HashMap<>();
    private final Map<String, BigDecimal> prices = new HashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final BigDecimal TAX_RATE = new BigDecimal("0.125");

    // Add a product to the cart
    public void addProduct(String product, int quantity) throws IOException, InterruptedException {
        if (quantity <= 0) return;

        //Retrive product price from the Price API if not already cached
        if (!prices.containsKey(product)) {
            String urlString = String.format("https://equalexperts.github.io/backend-take-home-test-data/%s.json", product);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString))
                    .GET().build();
            HttpResponse<String> httResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (httResponse.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch price for product:" + product);
            }
            JsonObject jsonObject = JsonParser.parseString(httResponse.body()).getAsJsonObject();
            BigDecimal price = jsonObject.get("price").getAsBigDecimal();
            prices.put(product, price);
        }
        // update cart state
        items.put(product, items.getOrDefault(product, 0) + quantity);
    }

    //Helper to round up to two decimal places
    private BigDecimal roundUp(BigDecimal value){
        return value.setScale(2, RoundingMode.CEILING);
    }

    //2. Calculate the state
    public CartState calculateState() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String product = entry.getKey();
            int quantity = entry.getValue();
            BigDecimal unitPrice = prices.getOrDefault(product, BigDecimal.ZERO);
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }
        subtotal = roundUp(subtotal);
        BigDecimal taxPayable = roundUp(subtotal.multiply(TAX_RATE));
        BigDecimal totalPayabale = roundUp(subtotal.add(taxPayable));
        List<String> contents = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            contents.add(String.format("Cart contains %d * %s", entry.getValue(), entry.getKey()));
        }
        return new CartState(contents, subtotal, taxPayable, totalPayabale);
    }


}
