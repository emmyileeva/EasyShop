package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Order create(Order order) {
        String sql = """
                    INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getUserId());
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            stmt.setString(3, order.getAddress());
            stmt.setString(4, order.getCity());
            stmt.setString(5, order.getState());
            stmt.setString(6, order.getZip());
            stmt.setBigDecimal(7, order.getShippingAmount());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                order.setOrderId(keys.getInt(1));
            }

            return order;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert order.", e);
        }
    }
}
