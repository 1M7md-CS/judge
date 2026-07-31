package app.dao;

import app.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubmissionDAO {

    public static void addSubmission(int userId, int problemId, String code, String language, String verdict) {

        String sql = """
                INSERT INTO submissions(code, language, verdict, user_id, problem_id)
                VALUES(?,?,?,?,?)
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, language);
            ps.setString(3, verdict);
            ps.setInt(4, userId);
            ps.setInt(5, problemId);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static boolean hasAccepted(int userId, int problemId) {

        String sql = """
                SELECT COUNT(*)
                FROM submissions
                WHERE user_id = ?
                AND problem_id = ?
                AND verdict = 'ACCEPTED'
                """;

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, problemId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }
}
