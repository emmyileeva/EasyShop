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
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    private ProductDao productDao;

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();

        String sql = "SELECT * FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                int productId = row.getInt("product_id");
                int quantity = row.getInt("quantity");
                double discountPercent = row.getDouble("discount_percent");

                // Fetch the full product details using ProductDao
                Product product = productDao.getById(productId);

                // Create ShoppingCartItem and calculate line total
                ShoppingCartItem item = new ShoppingCartItem(product, quantity, discountPercent);

                cart.add(item);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error reading shopping cart for user ID: " + userId, e);
        }

        return cart;
    }
}
