package data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameDataService {
    private final PlayerRepository playerRepository;
    private boolean enabled;

    public GameDataService() {
        this.playerRepository = new PlayerRepository(new DatabaseConnection());
        this.enabled = initialize();
    }

    private boolean initialize() {
        try {
            playerRepository.createTableIfNotExists();
            return true;
        } catch (SQLException e) {
            System.err.println("Database tidak aktif: " + e.getMessage());
            return false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PlayerProfile loadOrCreatePlayer(String username) {
        if (!enabled) return null;
        try {
            PlayerProfile existing = playerRepository.findByUsername(username);
            if (existing != null) return existing;

            int id = playerRepository.insertPlayer(username);
            return new PlayerProfile(id, username, 0, 1, null);
        } catch (SQLException e) {
            System.err.println("Gagal load/create player: " + e.getMessage());
            return null;
        }
    }

    public void saveProgress(int playerId, int score, int level) {
        if (!enabled || playerId <= 0) return;
        try {
            playerRepository.updateProgress(playerId, score, level);
        } catch (SQLException e) {
            System.err.println("Gagal menyimpan progress: " + e.getMessage());
        }
    }

    public List<LeaderboardEntry> getTopScores(int limit) {
        if (!enabled) return new ArrayList<>();
        try {
            return playerRepository.findTopScores(limit);
        } catch (SQLException e) {
            System.err.println("Gagal mengambil leaderboard: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
