package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao {
    public MySqlProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection()) {
            // CORRECTIE: De 'user_id' is de PK, maar wordt niet gegenereerd (het is een FK).
            // PreparedStatement.RETURN_GENERATED_KEYS is hier dus niet nodig.
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Retrieves a profile by user ID
    @Override
    public Profile getByUserId(int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving profile.", e);
        }
    }

    // Updates an existing profile in the database
    @Override
    public void update(Profile profile, int userId) {
        String sql = """
                    UPDATE profiles SET
                        first_name = ?,
                        last_name = ?,
                        email = ?,
                        phone = ?,
                        address = ?,
                        city = ?,
                        state = ?,
                        zip = ?
                    WHERE user_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, profile.getFirstName());
            stmt.setString(2, profile.getLastName());
            stmt.setString(3, profile.getEmail());
            stmt.setString(4, profile.getPhone());
            stmt.setString(5, profile.getAddress());
            stmt.setString(6, profile.getCity());
            stmt.setString(7, profile.getState());
            stmt.setString(8, profile.getZip());
            stmt.setInt(9, userId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating profile.", e);
        }
    }

    // Maps a row from the ResultSet to a Profile object
    private Profile mapRow(ResultSet rs) throws SQLException {
        Profile profile = new Profile();
        profile.setUserId(rs.getInt("user_id"));
        profile.setFirstName(rs.getString("first_name"));
        profile.setLastName(rs.getString("last_name"));
        profile.setPhone(rs.getString("phone"));
        profile.setEmail(rs.getString("email"));
        profile.setAddress(rs.getString("address"));
        profile.setCity(rs.getString("city"));
        profile.setState(rs.getString("state"));
        profile.setZip(rs.getString("zip"));
        return profile;
    }
}
