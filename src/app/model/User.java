package app.model;

public record User(int id, String username, String password, int score, String role) {

    @Override
    public String toString() {
        if ("ADMIN".equals(role)) {
            return username;
        }
        return username + " - Score: " + score;
    }
}