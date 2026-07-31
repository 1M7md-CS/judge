package app.model;

public record Problem(int id, String title, int points) {

    @Override
    public String toString() {
        return title;
    }
}
