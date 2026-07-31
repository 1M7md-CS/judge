package app.dao;

import app.core.Database;
import app.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class UserDAO {

    public static User login(String username, String password) {

        String sql = """
                SELECT *
                FROM users
                WHERE username = ?
                AND password_hash = ?
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash(password));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static User getUserById(int id) {

        String sql = """
                SELECT *
                FROM users
                WHERE user_id = ?
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT *
                FROM users
                ORDER BY score DESC
                """;

        try (Connection con = Database.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(map(rs));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return users;
    }

    public static boolean usernameExists(String username) {

        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE username = ?
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static boolean addUser(String username, String password) {

        if (usernameExists(username)) {
            return false;
        }

        String sql = """
                INSERT INTO users(username,password_hash)
                VALUES(?,?)
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash(password));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static boolean updateUser(int id, String username, String password) {

        String sql = """
                UPDATE users
                SET username=?,
                    password_hash=?
                WHERE user_id=?
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash(password));
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static boolean removeUser(int id) {

        String sql = """
                DELETE FROM users
                WHERE user_id=?
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static void incrementScore(int userId, int points) {

        String sql = """
                UPDATE users
                SET score = score + ?
                WHERE user_id = ?
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, points);
            ps.setInt(2, userId);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    private static String hash(String password) {

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getInt("score"),
                rs.getString("role")
        );
    }
}
