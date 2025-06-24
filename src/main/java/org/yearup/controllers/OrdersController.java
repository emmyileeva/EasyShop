package org.yearup.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ShoppingCartDao;

import java.security.Principal;

@RestController
@RequestMapping("/orders")
@PreAuthorize("isAuthenticated()") // Require user to be logged in
public class OrdersController {

    private final OrderDao orderDao;
    private final ShoppingCartDao shoppingCartDao;

    // Constructor injection for the DAOs
    public OrdersController(OrderDao orderDao, ShoppingCartDao shoppingCartDao) {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
    }

    // Endpoint to handle checkout process
    @PostMapping
    public void checkout(Principal principal) {
        try {
            // Get the current logged-in user's ID
            int userId = Integer.parseInt(principal.getName());

            // Retrieve the shopping cart for the user
            var cart = shoppingCartDao.getByUserId(userId);

            // If cart is empty or doesn't exist, throw an error
            if (cart == null || cart.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty.");
            }

            System.out.println("User ID: " + userId);
            System.out.println("Cart items: " + cart.getItems().size());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Checkout failed.");
        }
    }
}
