package org.yearup.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineItemDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

import static org.mockito.Mockito.*;

// Unit tests for the OrdersController class
// This test verifies that the checkout process correctly:
// - Creates an order
// - Adds all shopping cart items as line items
// - Clears the shopping cart after checkout

@WithMockUser // Simulate an authenticated user for Spring Security
public class OrdersControllerTest {

    @Mock
    private OrderDao orderDao; // Mocked dependency for creating orders

    @Mock
    private ShoppingCartDao shoppingCartDao; // Mocked dependency for retrieving/clearing shopping cart

    @Mock
    private OrderLineItemDao orderLineItemDao; // Mocked dependency for inserting order line items

    @Mock
    private Principal mockPrincipal; // Mocked Principal to simulate the current logged-in user

    @InjectMocks
    private OrdersController controller; // The controller we're testing, with mocks injected

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize all @Mock annotations
    }

    @Test
    public void checkout_ShouldCreateOrderAndLineItemsAndClearCart() {
        // Arrange
        int userId = 1; // Simulated user ID for the logged-in user

        // Create a sample product to go in the cart
        Product product = new Product();
        product.setProductId(100);
        product.setPrice(new BigDecimal("49.99"));

        // Create a ShoppingCartItem with quantity 2 and 0% discount
        ShoppingCartItem item = new ShoppingCartItem(product, 2, 0.0);

        // Create a shopping cart and add the item to it
        ShoppingCart cart = new ShoppingCart();
        cart.setItems(Map.of(product.getProductId(), item)); // Manually set cart items

        // Simulate the database returning a new order with orderId 10
        Order newOrder = new Order();
        newOrder.setOrderId(10);

        // Set up the mocks to return the correct data
        when(mockPrincipal.getName()).thenReturn(String.valueOf(userId)); // Simulate logged-in user
        when(shoppingCartDao.getByUserId(userId)).thenReturn(cart); // Return the cart for the user
        when(orderDao.create(any(Order.class))).thenReturn(newOrder); // Return the new order from DB

        // Act
        controller.checkout(mockPrincipal); // Call the method we’re testing

        // Assert
        // Verify that an order was created and passed to the DAO
        verify(orderDao, times(1)).create(any(Order.class));

        // Verify that a line item was created for the cart item
        verify(orderLineItemDao, times(1)).create(any(OrderLineItem.class));

        // Verify that the cart was cleared after checkout
        verify(shoppingCartDao, times(1)).clearCart(userId);
    }
}
