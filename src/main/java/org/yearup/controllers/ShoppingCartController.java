package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()") // Only allow logged-in users
@CrossOrigin
public class ShoppingCartController {
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    // constructor injection for the DAOs
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter

    // add a GET method to retrieve the current user's shopping cart
    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        try {
            // get the currently logged in username
            String userName = principal.getName();

            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // Fetch the user's shopping cart using their userId
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);

            // Return the shopping cart as the response body (automatically serialized to JSON)
            return cart;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{productId}")
    public void addToCart(@PathVariable int productId, Principal principal) {
        try {
            // Get the logged-in user's username and their user ID
            String username = principal.getName();
            User user = userDao.getByUserName(username);
            int userId = user.getId();

            // Check if product already exists in cart
            boolean exists = shoppingCartDao.existsInCart(userId, productId);

            if (exists) {
                // Get current cart
                ShoppingCart cart = shoppingCartDao.getByUserId(userId);
                int currentQty = cart.get(productId).getQuantity();

                // Increment quantity by 1
                shoppingCartDao.updateQuantity(userId, productId, currentQty + 1);
            } else {
                // Add product to cart with quantity 1
                shoppingCartDao.addProduct(userId, productId);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add product to cart.");
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated


    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart

}
