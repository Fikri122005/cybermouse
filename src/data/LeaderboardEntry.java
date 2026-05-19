package data;

public class LeaderboardEntry {
    public final String username;
    public final int score;
    public final int level;
    public final String playedAt;

    public LeaderboardEntry(String username, int score, int level, String playedAt) {
        this.username = username;
        this.score = score;
        this.level = level;
        this.playedAt = playedAt;
    }
}
