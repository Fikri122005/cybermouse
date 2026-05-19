package game;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import data.GameDataService;
import data.LeaderboardEntry;
import data.PlayerProfile;
import entity.Bird;
import entity.Cloud;
import entity.Player;
import object.Cheese;
import object.Coin;
import object.FinishLine;
import object.Spike;
import tile.TileManager;
import util.AssetPaths;
import util.LogoLoader;

public class GamePanel extends JPanel {
    public final int screenWidth = 1280;
    public final int screenHeight = 720;
    public final int FPS = 60;
    
    public KeyHandler keyH = new KeyHandler(this);
    public AudioManager audioM = new AudioManager();
    private final MenuScreen menuScreen = new MenuScreen();
    private GameLoop gameLoop;
    
    public Player player;
    public TileManager tileM;
    public List<Coin> coins;
    public List<Cheese> cheeses;
    public List<Spike> spikes;
    public List<Bird> birds;
    public List<Cloud> clouds;
    public FinishLine finishLine;
    
    public BufferedImage bgImage;
    public BufferedImage logoImage;
    public BufferedImage laviraLogo;
    private double skyLayerOffset = 0;
    private double nearLayerOffset = 0;
    
    // GAME STATE
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int finishState = 2;
    public final int gameOverState = 3;
    public final int splashState = 4;
    private int splashTimer = 0;
    public int score = 0;
    public int stamina = 0;
    public final int maxStamina = 3; // Change to 3 cheeses for full stamina as requested "saat penuh"
    public int currentLevel = 1;
    
    // Persistence
    private final GameDataService gameDataService;
    private List<LeaderboardEntry> leaderboard = new ArrayList<>();
    private String currentUsername = "Guest";
    private int currentPlayerId = -1;
    private int loadedScore = 0;
    private int loadedLevel = 1;
    private boolean progressSavedForRound = false;
    private boolean scoreSavedToDb = false;
    
    // CAMERA
    public int cameraX = 0;
    
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(new Color(20, 0, 0)); 
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        
        gameDataService = new GameDataService();
        
        getBackgroundImage();
        getLogoImage();
        refreshLeaderboard();
        setupGame();
    }

    public void getBackgroundImage() {
        try {
            bgImage = ImageIO.read(AssetPaths.resolve("assets/background/gov_buildings.png").toFile());
        } catch (IOException e) {
            bgImage = null;
        }
    }
    
    public void getLogoImage() {
        logoImage = LogoLoader.load(); // Gunakan logo Horizon untuk huruf 'O'
        try {
            BufferedImage img = ImageIO.read(util.AssetPaths.resolve("assets/logo/cybermouse.jpeg").toFile());
            if (img != null) {
                img = makeColorTransparent(img, Color.BLACK, 30);
                laviraLogo = img; // Gunakan logo CyberMouse untuk splash screen
            }
        } catch (IOException e) {
            System.err.println("[Image] Gagal memuat logo CyberMouse: " + e.getMessage());
            laviraLogo = null;
        }
    }

    private BufferedImage makeColorTransparent(BufferedImage img, Color target, int tolerance) {
        BufferedImage dimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dimg.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        
        int tr = target.getRed();
        int tg = target.getGreen();
        int tb = target.getBlue();
        
        for (int y = 0; y < dimg.getHeight(); y++) {
            for (int x = 0; x < dimg.getWidth(); x++) {
                int rgb = dimg.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g_ = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                if (Math.abs(r - tr) <= tolerance &&
                    Math.abs(g_ - tg) <= tolerance &&
                    Math.abs(b - tb) <= tolerance) {
                    dimg.setRGB(x, y, 0); // Transparent
                }
            }
        }
        return dimg;
    }
    
    public void setupGame() {
        gameState = splashState;
        audioM.playBGM(); // Play music on the menu screen
        progressSavedForRound = false;
        scoreSavedToDb = false;
        refreshLeaderboard(); // Refresh agar menu selalu tampil data terbaru
    }
    
    public void startNewGame() {
        preparePlayerSession(false);
        initGameLevel();
    }
    
    public void loadSavedGame() {
        preparePlayerSession(true);
        initGameLevel();
    }
    
    public void retryLevel() {
        loadedScore = 0;
        loadedLevel = Math.max(1, currentLevel);
        initGameLevel();
    }
    
    // Called when starting gameplay
    private void initGameLevel() {
        tileM = new TileManager(this);
        player = new Player(this, keyH);
        score = loadedScore;
        stamina = 0;
        currentLevel = loadedLevel;
        cameraX = 0;
        progressSavedForRound = false;
        
        int groundY = screenHeight - 50;

        coins = new ArrayList<>();
        coins.add(new Coin(320, groundY - 140));
        coins.add(new Coin(500, groundY - 200));
        coins.add(new Coin(680, groundY - 260));
        coins.add(new Coin(1180, groundY - 200));
        coins.add(new Coin(2180, 330));
        coins.add(new Coin(2480, 220));
        coins.add(new Coin(2720, 300));
        coins.add(new Coin(3340, 160));
        coins.add(new Coin(4020, 340));
        coins.add(new Coin(4480, 260));
        coins.add(new Coin(5080, groundY - 280));
        coins.add(new Coin(5800, groundY - 240));
        coins.add(new Coin(6200, 250));
        coins.add(new Coin(6600, 330));
        coins.add(new Coin(7200, 280));
        coins.add(new Coin(7800, 150));

        cheeses = new ArrayList<>();
        cheeses.add(new Cheese(1420, groundY - 260));
        cheeses.add(new Cheese(2560, 200));
        cheeses.add(new Cheese(3640, 400));
        cheeses.add(new Cheese(4200, groundY - 150));
        cheeses.add(new Cheese(4800, 300));
        cheeses.add(new Cheese(5950, 420));
        cheeses.add(new Cheese(6800, 200));
        cheeses.add(new Cheese(7500, 150));

        finishLine = new FinishLine(7850, 100);

        spikes = new ArrayList<>();
        spikes.add(new Spike(828, groundY, 82, 30, 5));
        spikes.add(new Spike(1458, groundY, 112, 32, 6));
        spikes.add(new Spike(1972, groundY, 96, 34, 6));
        spikes.add(new Spike(2762, groundY, 92, 32, 5));
        spikes.add(new Spike(4850, groundY, 120, 32, 7));
        spikes.add(new Spike(5650, groundY, 150, 32, 8));
        spikes.add(new Spike(6300, groundY, 100, 32, 6));
        spikes.add(new Spike(7000, groundY, 110, 32, 6));
        spikes.add(new Spike(7450, groundY, 130, 32, 7));

        clouds = new ArrayList<>();
        clouds.add(new Cloud(80, 48, 0.22));
        clouds.add(new Cloud(520, 96, 0.32));
        clouds.add(new Cloud(1100, 72, 0.16));
        clouds.add(new Cloud(1880, 44, 0.26));
        clouds.add(new Cloud(2600, 118, 0.21));
        clouds.add(new Cloud(3400, 60, 0.18));
        clouds.add(new Cloud(4200, 100, 0.24));

        birds = new ArrayList<>();
        birds.add(new Bird(1600, 330));
        birds.add(new Bird(2700, 280));
        birds.add(new Bird(3900, 260));
        birds.add(new Bird(4600, 250));
        birds.add(new Bird(5500, 300));
        birds.add(new Bird(6000, 200));
        birds.add(new Bird(6500, 350));
        birds.add(new Bird(7000, 220));
        birds.add(new Bird(7500, 280));
        birds.add(new Bird(7800, 180));

        audioM.playBGMWithFadeIn(700);
        gameState = playState;
    }
    
    public void startGameThread() {
        gameLoop = new GameLoop(this);
        Thread gameThread = new Thread(gameLoop);
        gameThread.start();
    }
    
    private void preparePlayerSession(boolean loadExistingProgress) {
        String input = JOptionPane.showInputDialog(this, "Masukkan username:", currentUsername);
        if (input != null && !input.trim().isEmpty()) {
            currentUsername = input.trim();
        }
        
        loadedScore = 0;
        loadedLevel = 1;
        currentPlayerId = -1;
        
        PlayerProfile profile = gameDataService.loadOrCreatePlayer(currentUsername);
        if (profile != null) {
            currentPlayerId = profile.id;
            if (loadExistingProgress) {
                loadedScore = profile.score;
                loadedLevel = Math.max(1, profile.level);
            }
        }
        
        if (!loadExistingProgress && currentPlayerId > 0) {
            gameDataService.saveProgress(currentPlayerId, 0, 1);
            refreshLeaderboard();
        }
    }
    
    public void handleGameOver() {
        if (gameState == gameOverState) return;
        audioM.playGameOver();
        audioM.stopBGMWithFadeOut(450);
        gameState = gameOverState;
        persistProgressOnce();
    }
    
    public void handleLevelComplete() {
        if (gameState == finishState) return;
        audioM.playFinish();
        audioM.stopBGMWithFadeOut(500);
        gameState = finishState;
        currentLevel++;
        persistProgressOnce();
    }
    
    private void persistProgressOnce() {
        if (progressSavedForRound) return;
        if (currentPlayerId > 0) {
            gameDataService.saveProgress(currentPlayerId, score, currentLevel);
            scoreSavedToDb = gameDataService.isEnabled();
            System.out.println("[DB] Score tersimpan: user=" + currentUsername + " score=" + score + " level=" + currentLevel);
        } else {
            System.out.println("[DB] Score tidak tersimpan: player belum login.");
            scoreSavedToDb = false;
        }
        progressSavedForRound = true;
        refreshLeaderboard();
    }
    
    private void refreshLeaderboard() {
        leaderboard = gameDataService.getTopScores(5);
    }
    
    public void update() {
        if (gameState == splashState) {
            splashTimer++;
            if (splashTimer >= 180) { // 3 seconds at 60 FPS
                gameState = titleState;
            }
        }
        if (gameState == playState || gameState == finishState || gameState == gameOverState) {
            // Entities update independently of gamestate
            // as we want background clouds to keep moving
            for (Cloud c : clouds) c.update();
            for (Bird b : birds) b.update();
            tileM.update();
            
            // Only update player controls if in play state
            if (gameState == playState) {
                player.update();
            }
            
            // Camera smoothing: softer follow gives more natural motion
            int worldMaxX = tileM.getWorldMaxX();
            int desiredCamera = Math.max(0, player.x - (screenWidth / 2));
            int maxCamera = Math.max(0, worldMaxX - screenWidth);
            desiredCamera = Math.min(desiredCamera, maxCamera);
            cameraX += (int) ((desiredCamera - cameraX) * 0.12);
            
            // Time-based background offsets for animated atmosphere
            skyLayerOffset += 0.20;
            nearLayerOffset += 0.65;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        if (gameState == splashState) {
            drawSplashScreen(g2);
        }
        else if (gameState == titleState) {
            drawTitleScreen(g2);
        }
        else if (gameState == playState || gameState == finishState || gameState == gameOverState) {
            drawParallaxBackground(g2);
            
            // Environment overlays
            for (Cloud c : clouds) c.draw(g2, cameraX);
            
            // Tiles
            tileM.draw(g2, cameraX);
            
            // Objects
            for (Coin c : coins) c.draw(g2, cameraX);
            for (Cheese c : cheeses) c.draw(g2, cameraX);
            if (spikes != null) {
                for (Spike s : spikes) s.draw(g2, cameraX);
            }
            finishLine.draw(g2, cameraX);
            for (Bird b : birds) b.draw(g2, cameraX);
            
            // Player
            player.draw(g2, cameraX);
            
            // UI - Score
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            g2.drawString("Score: " + score, 20, 40);
            
            // UI - Stamina
            g2.setColor(Color.GRAY);
            g2.fillRoundRect(20, 55, 200, 20, 10, 10);
            if (stamina >= maxStamina) {
                g2.setColor(new Color(0, 255, 100)); // Full stamina color
            } else {
                g2.setColor(new Color(255, 200, 0)); // Charging stamina color
            }
            int barWidth = (int) (200 * ((double) stamina / maxStamina));
            g2.fillRoundRect(20, 55, barWidth, 20, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(20, 55, 200, 20, 10, 10);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("STAMINA " + (stamina >= maxStamina ? "(READY)" : ""), 25, 70);
            
            if (gameState == finishState) {
                drawLevelCompleteScreen(g2);
            }
            else if (gameState == gameOverState) {
                drawGameOverScreen(g2);
            }
        }
        
        // Logo pojok kanan atas — tampil di semua layar
        // drawCornerLogo(g2);
        
        g2.dispose();
    }
    
    private void drawSplashScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        if (laviraLogo != null) {
            // Animation logic: Fade in and Scale up for the first 60 frames (1 second)
            double progress = Math.min(1.0, splashTimer / 60.0);
            
            // Smooth step or ease out
            double scale = 0.5 + 0.5 * Math.sin(progress * Math.PI / 2); // Scales from 0.5 to 1.0
            float alpha = (float) progress; // Alpha from 0.0 to 1.0
            
            int baseH = 300;
            int targetH = (int) (baseH * scale);
            int targetW = (int) (targetH * ((double) laviraLogo.getWidth() / laviraLogo.getHeight()));
            
            int x = (screenWidth - targetW) / 2;
            int y = (screenHeight - targetH) / 2 - 50; // Shift up slightly to make room for text
            
            // Set alpha for transparency
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            
            g2.drawImage(laviraLogo, x, y, targetW, targetH, null);
            
            // Draw "CYBER MOUSE" text below the logo with red shadow
            g2.setFont(new Font("Arial", Font.BOLD, 72));
            String text = "CYBER MOUSE";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (screenWidth - fm.stringWidth(text)) / 2;
            
            // Keep text position fixed relative to base height
            int textY = (screenHeight - baseH) / 2 - 50 + baseH + 80;
            
            // Shadow
            g2.setColor(Color.RED);
            g2.drawString(text, tx + 4, textY + 4);
            
            // Main Text
            g2.setColor(Color.WHITE);
            g2.drawString(text, tx, textY);
            
            // Reset composite
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private void drawCornerLogo(Graphics2D g2) {
        if (logoImage == null) return;
        final int LOGO_W = 80;
        final int MARGIN  = 10;
        int lh = (int) (logoImage.getHeight() * (LOGO_W / (double) logoImage.getWidth()));
        int lx = screenWidth - LOGO_W - MARGIN;
        int ly = MARGIN;
        // Semi-transparent background pill agar logo tetap terbaca di atas background apapun
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(lx - 4, ly - 4, LOGO_W + 8, lh + 8, 10, 10);
        g2.drawImage(logoImage, lx, ly, LOGO_W, lh, null);
    }
    
    private void drawParallaxBackground(Graphics2D g2) {
        if (bgImage != null) {
            int farOffset = ((int) (cameraX * 0.20 + skyLayerOffset)) % screenWidth;
            int midOffset = ((int) (cameraX * 0.50)) % screenWidth;
            int nearOffset = ((int) (cameraX * 0.78 + nearLayerOffset)) % screenWidth;
            
            // Far layer
            g2.drawImage(bgImage, -farOffset, 0, screenWidth, screenHeight, null);
            g2.drawImage(bgImage, -farOffset + screenWidth, 0, screenWidth, screenHeight, null);
            
            // Mid layer blend
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.drawImage(bgImage, -midOffset, 0, screenWidth, screenHeight, null);
            g2.drawImage(bgImage, -midOffset + screenWidth, 0, screenWidth, screenHeight, null);
            
            // Near fog strip
            g2.setColor(new Color(25, 0, 0, 95));
            g2.fillRect(0, screenHeight - 230, screenWidth, 230);
            g2.setColor(new Color(255, 70, 70, 40));
            for (int i = -1; i <= 6; i++) {
                int x = i * 260 - nearOffset;
                g2.fillRoundRect(x, screenHeight - 210, 180, 90, 40, 40);
            }
        } else {
            g2.setColor(new Color(60, 10, 10));
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }
    }

    private void drawTitleScreen(Graphics2D g2) {
        menuScreen.draw(g2, screenWidth, screenHeight, bgImage, logoImage, laviraLogo, leaderboard, keyH.commandNum);
    }
    
    private void drawLevelCompleteScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        String part1 = "LEVEL C";
        String part2 = "MPLETE!";
        FontMetrics fm = g2.getFontMetrics();
        
        int w1 = fm.stringWidth(part1);
        int wO = fm.stringWidth("O");
        int w2 = fm.stringWidth(part2);
        int totalW = w1 + wO + w2;
        
        int x = (screenWidth - totalW) / 2;
        
        // Efek animasi: Melayang (Floating) dan bayangan berdenyut (Pulsing shadow)
        long time = System.currentTimeMillis();
        int yOffset = (int) (15 * Math.sin(time / 200.0)); // Gerakan naik turun
        int y = screenHeight / 2 - 50 + yOffset;
        
        // Warna bayangan berdenyut antara merah dan oranye
        int green = (int) (50 + 50 * Math.sin(time / 300.0));
        Color shadowColor = new Color(255, green, 0);
        
        // Shadow
        g2.setColor(shadowColor);
        g2.drawString(part1, x + 4, y + 4);
        g2.drawString(part2, x + w1 + wO + 4, y + 4);
        
        // Main Text
        g2.setColor(Color.WHITE);
        g2.drawString(part1, x, y);
        g2.drawString(part2, x + w1 + wO, y);
        
        // Draw logo where 'O' was
        if (logoImage != null) {
            int logoH = fm.getAscent(); // Match the height of letters
            int logoW = (int) (logoH * ((double) logoImage.getWidth() / logoImage.getHeight()));
            int lx = x + w1 + (wO - logoW) / 2;
            int ly = y - fm.getAscent() + 4; // Align with text
            
            g2.drawImage(logoImage, lx, ly, logoW, logoH, null);
        } else {
            g2.setColor(shadowColor);
            g2.drawString("O", x + w1 + 4, y + 4);
            g2.setColor(Color.WHITE);
            g2.drawString("O", x + w1, y);
        }
        
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String text = "Final Score: " + score;
        fm = g2.getFontMetrics();
        x = (screenWidth - fm.stringWidth(text)) / 2;
        y += 80;
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        if (scoreSavedToDb) {
            text = "✔ Score tersimpan ke database! (" + currentUsername + ")";
            g2.setColor(new Color(100, 255, 120));
        } else {
            text = "⚠ Score tidak tersimpan (database tidak aktif)";
            g2.setColor(new Color(255, 180, 80));
        }
        fm = g2.getFontMetrics();
        x = (screenWidth - fm.stringWidth(text)) / 2;
        y += 60;
        g2.drawString(text, x, y);

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        text = "Tekan ENTER untuk kembali ke menu";
        fm = g2.getFontMetrics();
        x = (screenWidth - fm.stringWidth(text)) / 2;
        y += 52;
        g2.setColor(new Color(230, 230, 160));
        g2.drawString(text, x, y);
    }
    
    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        String text = "GAME OVER!";
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(text)) / 2;
        int y = screenHeight / 2 - 100;
        
        g2.setColor(Color.RED);
        g2.drawString(text, x, y);
        
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        text = "Score: " + score;
        fm = g2.getFontMetrics();
        x = (screenWidth - fm.stringWidth(text)) / 2;
        y += 100;
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
        
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        if (scoreSavedToDb) {
            text = "✔ Score tersimpan ke database! (" + currentUsername + ")";
            g2.setColor(new Color(100, 255, 120));
        } else {
            text = "⚠ Score tidak tersimpan (database tidak aktif)";
            g2.setColor(new Color(255, 180, 80));
        }
        fm = g2.getFontMetrics();
        x = (screenWidth - fm.stringWidth(text)) / 2;
        y += 60;
        g2.drawString(text, x, y);

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        text = "ENTER = Menu  |  R = Retry";
        fm = g2.getFontMetrics();
        x = (screenWidth - fm.stringWidth(text)) / 2;
        y += 52;
        g2.setColor(Color.YELLOW);
        g2.drawString(text, x, y);
    }
}
