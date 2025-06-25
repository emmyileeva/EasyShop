package org.yearup.controllers;

import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineItemDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.OrderLineItem;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/orders")
@PreAuthorize("isAuthenticated()") // Require user to be logged in
public class OrdersController {

    private final OrderDao orderDao;
    private final ShoppingCartDao shoppingCartDao;
    private final OrderLineItemDao orderLineItemDao;
    private final UserDao userDao;

    // Constructor injection for the DAOs
    public OrdersController(OrderDao orderDao, ShoppingCartDao shoppingCartDao, OrderLineItemDao orderLineItemDao, UserDao userDao) {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
        this.orderLineItemDao = orderLineItemDao;
        this.userDao = userDao;
    }

    // Endpoint to handle checkout process
    @PostMapping
    public void checkout(Principal principal) {
        try {
            // Get the currently logged-in user's username
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);

            // If user is not found, throw an error
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            // Retrieve the shopping cart for the user
            var cart = shoppingCartDao.getByUserId(userId);

            // If cart is empty or doesn't exist, throw an error
            if (cart == null || cart.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty.");
            }
            // Create a new order
            Order order = new Order();
            order.setUserId(userId);
            order.setDate(LocalDate.now());
            order.setAddress("123 Main St");
            order.setCity("Sample City");
            order.setState("CA");
            order.setZip("12345");
            order.setShippingAmount(new BigDecimal("5.99"));

            // Insert the order into the database and get the generated order ID
            Order createdOrder = orderDao.create(order);
            int orderId = createdOrder.getOrderId();

            System.out.println("Created order with ID: " + orderId);

            // Insert order line items for each item in the shopping cart
            for (ShoppingCartItem item : cart.getItems().values()) {
                OrderLineItem lineItem = new OrderLineItem();
                lineItem.setOrderId(orderId);
                lineItem.setProductId(item.getProduct().getProductId());
                lineItem.setSalesPrice(item.getProduct().getPrice());
                lineItem.setQuantity(item.getQuantity());
                lineItem.setDiscount(BigDecimal.ZERO);

                orderLineItemDao.create(lineItem);
            }
            // Clear the shopping cart after the order is created
            shoppingCartDao.clearCart(userId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Checkout failed.");
        }
    }
}
