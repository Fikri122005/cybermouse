package game;

import data.LeaderboardEntry;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Title / start screen layout: responsive logo, primary actions, leaderboard.
 */
public class MenuScreen {

    public static final int CMD_START = 0;
    public static final int CMD_LOAD = 1;
    public static final int CMD_EXIT = 2;
    public static final int CMD_COUNT = 3;

    public void draw(
            Graphics2D g2,
            int screenWidth,
            int screenHeight,
            BufferedImage background,
            BufferedImage logo,
            BufferedImage laviraLogo,
            List<LeaderboardEntry> leaderboard,
            int commandIndex) {

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (background != null) {
            g2.drawImage(background, 0, 0, screenWidth, screenHeight, null);
        } else {
            g2.setColor(new Color(35, 8, 8));
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }

        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Title with integrated logo: replacing 'O' in MOUSE and adding Lavira logo next to 'C'
        int fontSize = scaleTitleFont(screenWidth);
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        
        String part1 = "CYBER M";
        String part2 = "USE";
        int w1 = fm.stringWidth(part1);
        int wO = fm.stringWidth("O");
        int w2 = fm.stringWidth(part2);
        
        int fontAscent = fm.getAscent();
        int laviraH = (int) (fontAscent * 2.0); // Target height is double the letter size
        int laviraW = laviraH; // Fallback
        if (laviraLogo != null) {
            laviraW = (int) (laviraH * ((double) laviraLogo.getWidth() / laviraLogo.getHeight()));
        }
        int gap = 5; // Gap between Lavira logo and text
        
        int textW = w1 + wO + w2;
        int textX = (screenWidth - textW) / 2;
        int titleY = screenHeight / 4; // Reset to original position

        // Shadow
        g2.setColor(Color.RED);
        g2.drawString(part1, textX + 4, titleY + 4);
        g2.drawString(part2, textX + w1 + wO + 4, titleY + 4);
        
        // Main Text
        g2.setColor(Color.WHITE);
        g2.drawString(part1, textX, titleY);
        g2.drawString(part2, textX + w1 + wO, titleY);

        if (logo != null) {
            // Draw logo where 'O' was
            int logoH = fontAscent; // Match the height of letters like 'M'
            int logoW = (int) (logoH * ((double) logo.getWidth() / logo.getHeight()));
            int lx = textX + w1 + (wO - logoW) / 2;
            int ly = titleY - fontAscent + 4; // Nudged down slightly for better alignment
            
            g2.drawImage(logo, lx, ly, logoW, logoH, null);
        } else {
            g2.setColor(Color.RED);
            g2.drawString("O", textX + w1 + 4, titleY + 4);
            g2.setColor(Color.WHITE);
            g2.drawString("O", textX + w1, titleY);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 34));
        int menuY = titleY + 110;
        drawMenuRow(g2, screenWidth, "Start Game", menuY, commandIndex == CMD_START);
        drawMenuRow(g2, screenWidth, "Load Game", menuY + 64, commandIndex == CMD_LOAD);
        drawMenuRow(g2, screenWidth, "Exit", menuY + 128, commandIndex == CMD_EXIT);

        drawLeaderboard(g2, screenWidth, screenHeight, leaderboard);
    }

    private int scaleTitleFont(int screenWidth) {
        return Math.max(52, Math.min(96, screenWidth / 12));
    }

    private void drawMenuRow(Graphics2D g2, int screenWidth, String label, int y, boolean selected) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(label)) / 2;
        if (selected) {
            g2.setColor(new Color(255, 220, 80));
            g2.drawString(">", x - 36, y);
        }
        g2.setColor(selected ? Color.WHITE : new Color(210, 210, 210));
        g2.drawString(label, x, y);
    }

    private void drawLeaderboard(Graphics2D g2, int screenWidth, int screenHeight, List<LeaderboardEntry> leaderboard) {
        int panelX = Math.min(36, screenWidth / 32);
        int panelY = screenHeight - 210;
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(panelX - 8, panelY - 28, Math.min(420, screenWidth / 2 + 40), 200, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString("Top 5 Leaderboard", panelX, panelY);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        if (leaderboard == null || leaderboard.isEmpty()) {
            g2.drawString("Belum ada data score.", panelX, panelY + 32);
            return;
        }

        int y = panelY + 32;
        int rank = 1;
        for (LeaderboardEntry entry : leaderboard) {
            if (rank > 5) {
                break;
            }
            String row = rank + ". " + entry.username + " — " + entry.score + " pts (Lv " + entry.level + ")";
            g2.drawString(row, panelX, y);
            y += 26;
            rank++;
        }
    }
}
