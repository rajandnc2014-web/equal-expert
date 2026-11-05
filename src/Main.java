import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        ShoppingCart cart = new ShoppingCart();
        cart.addProduct("cornflakes" , 1);
        cart.addProduct("cornflakes" , 1);
        cart.addProduct("weetabix" , 1);

        CartState cardState = cart.calculateState();

        //print results
        for(String line : cardState.contents){
            System.out.println(line);
        }
        System.out.println("SubTotal = " + cardState.subtotal);
        System.out.println("Tax = " + cardState.tax);
        System.out.println("Total = " + cardState.total);
    }
}
