package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private ProductDao productDao;

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }

    // Retrieves the shopping cart for a specific user by their user ID
    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();

        String sql = "SELECT * FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet row = statement.executeQuery();

            while (row.next()) {
                int productId = row.getInt("product_id");
                int quantity = row.getInt("quantity");
                double discountPercent = row.getDouble("discount_percent");

                // Fetch the full product details using ProductDao
                Product product = productDao.getById(productId);

                // Create ShoppingCartItem and calculate line total
                ShoppingCartItem item = new ShoppingCartItem(product, quantity, discountPercent);

                cart.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading shopping cart for user ID: " + userId, e);
        }

        return cart;
    }

    // Method to check whether a product exists in the user's cart
    @Override
    public boolean existsInCart(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking cart existence.", e);
        }

        return false;
    }

    // Method to add a new product with quantity 1 and 0% discount to the user's cart
    @Override
    public void addProduct(int userId, int productId) {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity, discount_percent) VALUES (?, ?, 1, 0.0)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding product to cart.", e);
        }
    }

    // Method to update the quantity of an existing product in the user's cart
    @Override
    public void updateQuantity(int userId, int productId, int newQuantity) {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newQuantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating cart quantity.", e);
        }
    }

    // Method to clear all products from the user's shopping cart
    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error clearing shopping cart for user ID: " + userId, e);
        }
    }


}
