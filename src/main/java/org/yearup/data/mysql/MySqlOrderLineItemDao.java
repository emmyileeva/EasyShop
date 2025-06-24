package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderLineItemDao;
import org.yearup.models.OrderLineItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class MySqlOrderLineItemDao extends MySqlDaoBase implements OrderLineItemDao {

    public MySqlOrderLineItemDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void create(OrderLineItem item) {
        String sql = """
            INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getSalesPrice());
            stmt.setInt(4, item.getQuantity());
            stmt.setBigDecimal(5, item.getDiscount());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert order line item.", e);
        }
    }
}

