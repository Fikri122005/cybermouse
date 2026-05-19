package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerRepository {
    private final DatabaseConnection databaseConnection;

    public PlayerRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "score INT NOT NULL DEFAULT 0," +
                "level INT NOT NULL DEFAULT 1," +
                "played_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public PlayerProfile findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, score, level, played_at FROM players WHERE username = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPlayer(rs);
                }
            }
        }
        return null;
    }

    public int insertPlayer(String username) throws SQLException {
        String sql = "INSERT INTO players (username, score, level) VALUES (?, 0, 1)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Gagal membuat player baru untuk username: " + username);
    }

    public void updateProgress(int playerId, int score, int level) throws SQLException {
        // Gunakan MAX agar hanya score/level TERBAIK yang tersimpan (tidak tertimpa nilai lebih rendah)
        String sql = "UPDATE players SET score = GREATEST(score, ?), level = GREATEST(level, ?), played_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.setInt(2, level);
            ps.setInt(3, playerId);
            ps.executeUpdate();
        }
    }

    public List<LeaderboardEntry> findTopScores(int limit) throws SQLException {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = "SELECT username, score, level, played_at " +
                "FROM players " +
                "ORDER BY score DESC, level DESC, played_at ASC " +
                "LIMIT ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new LeaderboardEntry(
                            rs.getString("username"),
                            rs.getInt("score"),
                            rs.getInt("level"),
                            rs.getString("played_at")
                    ));
                }
            }
        }
        return entries;
    }

    private PlayerProfile mapPlayer(ResultSet rs) throws SQLException {
        return new PlayerProfile(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getInt("score"),
                rs.getInt("level"),
                rs.getString("played_at")
        );
    }
}
