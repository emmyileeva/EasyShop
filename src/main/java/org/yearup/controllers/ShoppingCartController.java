package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.QuantityUpdateDto;
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
            // if user is not found, throw an exception
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            // Fetch the user's shopping cart using their userId
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);

            // Return the shopping cart as the response body (automatically serialized to JSON)
            return cart;

        } catch (Exception e) {
            e.printStackTrace();
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
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
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
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add product to cart.");
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("/products/{productId}")
    public void updateCartItem(@PathVariable int productId,
                               @RequestBody QuantityUpdateDto dto,
                               Principal principal) {
        try {
            // Get the username of the currently logged-in user
            String userName = principal.getName();

            // Retrieve the user from the database
            User user = userDao.getByUserName(userName);
            // If user is not found, throw an exception
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            // Check if the product exists in the user's cart before updating
            if (!shoppingCartDao.existsInCart(userId, productId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in shopping cart.");
            }

            // Call DAO to update the quantity of the product in the cart
            shoppingCartDao.updateQuantity(userId, productId, dto.getQuantity());
        } catch (ResponseStatusException e) {
            throw e; // rethrow known exception
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update cart item.");
        }
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping
    public void clearCart(Principal principal) {
        try {
            // Get logged in user
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            // If user is not found, throw an exception
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            // Clear the cart
            shoppingCartDao.clearCart(userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to clear cart.");
        }
    }

}
