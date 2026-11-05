import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ShoppingCartTest {

    private HttpResponse<String> httpResponseMock;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        HttpClient httpClientMock = mock(HttpClient.class);
        httpResponseMock = mock(HttpResponse.class);

        // Inject the mocked HttpClient into the ShoppingCart
        shoppingCart = new ShoppingCart();
    }

    @Test
    void testAddProductsAndCalculateStateStandardExample() throws Exception {
        // Arrange API responses for cornflakes and weetabix
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body())
                .thenReturn("{\"name\": \"cornflakes\", \"price\": 2.52}")
                .thenReturn("{\"name\": \"weetabix\", \"price\": 9.98}");

//        when(httpClientMock.send(any(), any())).thenReturn();

        // Act: Inputs matching the project example
        // - 2 x cornflakes @ 2.52 each
        // - 1 x weetabix @ 9.98 each
        shoppingCart.addProduct("cornflakes", 1);
        shoppingCart.addProduct("cornflakes", 1);
        shoppingCart.addProduct("weetabix", 1);

        CartState state = shoppingCart.calculateState();

        // Assert: Verify cart contents
        assertEquals(2, state.contents.size());
        assertTrue(state.contents.contains("Cart contains 2 x cornflakes"));
        assertTrue(state.contents.contains("Cart contains 1 x weetabix"));

        // Assert: Verify calculations (Subtotal = 15.02, Tax = 1.88, Total = 16.90)
        assertEquals(0, new BigDecimal("15.02").compareTo(state.subtotal));
        assertEquals(0, new BigDecimal("1.88").compareTo(state.tax));
        assertEquals(0, new BigDecimal("16.90").compareTo(state.total));
    }

    @Test
    void testTaxRoundingUpBehavior() throws Exception {
        // Arrange API response for a custom price that requires tax rounding up
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("{\"name\": \"item\", \"price\": 10.00}");
//        when(httpClientMock.send(any(), any())).thenReturn(httpResponseMock);

        // Act: Subtotal = 10.00. Tax at 12.5% = 1.25 (Exact)
        shoppingCart.addProduct("item", 1);
        CartState state = shoppingCart.calculateState();

        assertEquals(0, new BigDecimal("10.00").compareTo(state.subtotal));
        assertEquals(0, new BigDecimal("1.25").compareTo(state.tax));
        assertEquals(0, new BigDecimal("11.25").compareTo(state.total));
    }

    @Test
    void testApiFailureThrowsException() throws Exception {
        // Arrange API failure (e.g., 404 Not Found)
        when(httpResponseMock.statusCode()).thenReturn(404);
//        when(httpClientMock.send(any(), any())).thenReturn(httpResponseMock);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            shoppingCart.addProduct("invalid-product", 1);
        });

        assertTrue(exception.getMessage().contains("Failed to fetch price"));
    }

    @Test
    void testInvalidQuantityDoesNotModifyCart() throws Exception {
        shoppingCart.addProduct("cornflakes", 0);
        shoppingCart.addProduct("cornflakes", -5);

        CartState state = shoppingCart.calculateState();
        assertTrue(state.contents.isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(state.subtotal));
    }
}