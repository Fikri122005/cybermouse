package data;

public class PlayerProfile {
    public final int id;
    public final String username;
    public final int score;
    public final int level;
    public final String playedAt;

    public PlayerProfile(int id, String username, int score, int level, String playedAt) {
        this.id = id;
        this.username = username;
        this.score = score;
        this.level = level;
        this.playedAt = playedAt;
    }
}
