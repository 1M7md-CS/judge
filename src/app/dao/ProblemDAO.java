package app.dao;

import app.core.Database;
import app.model.Problem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProblemDAO {

    public static List<Problem> getAllProblems() {

        List<Problem> problems = new ArrayList<>();

        String sql = """
                SELECT *
                FROM problems
                ORDER BY problem_id
                """;

        try (Connection con = Database.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                problems.add(map(rs));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return problems;
    }

    private static Problem map(ResultSet rs) throws SQLException {
        return new Problem(
                rs.getInt("problem_id"),
                rs.getString("title"),
                rs.getInt("points")
        );
    }
}
