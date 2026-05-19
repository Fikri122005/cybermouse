package tile;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import game.GamePanel;

public class TileManager {

    private final GamePanel gp;
    public final List<Platform> platforms;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        this.platforms = new ArrayList<>();
        loadMap();
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public void loadMap() {
        int groundY = gp.screenHeight - 50;

        // Ground — varied segments and gaps (longer jumps, mixed materials)
        platforms.add(new Platform(Platform.PlatformType.STONE, 0, groundY, 820, 50));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 920, groundY, 520, 50));
        platforms.add(new Platform(Platform.PlatformType.STONE, 1580, groundY, 380, 50));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 2080, groundY, 640, 50));
        platforms.add(new Platform(Platform.PlatformType.STONE, 2860, groundY, 720, 50));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 3720, groundY, 900, 50));
        platforms.add(new Platform(Platform.PlatformType.STONE, 4780, groundY, 800, 50));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 5700, groundY, 1200, 50));
        platforms.add(new Platform(Platform.PlatformType.STONE, 7100, groundY, 1000, 50));

        // Early tutorial stairs
        platforms.add(new Platform(Platform.PlatformType.STEP, 240, groundY - 24, 140, 24));
        platforms.add(new Platform(Platform.PlatformType.STEP, 420, groundY - 72, 130, 22));
        platforms.add(new Platform(Platform.PlatformType.STEP, 600, groundY - 130, 120, 22));
        platforms.add(new Platform(Platform.PlatformType.STONE, 780, groundY - 200, 100, 20));

        // Pipe section
        platforms.add(new Platform(Platform.PlatformType.PIPE, 1120, groundY - 110, 78, 110));
        platforms.add(new Platform(Platform.PlatformType.PIPE, 1380, groundY - 150, 82, 150));
        platforms.add(new Platform(Platform.PlatformType.PIPE, 1680, groundY - 95, 88, 95));

        // Mid-air challenge — mixed heights and widths
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 1950, 420, 90, 22));
        platforms.add(new Platform(Platform.PlatformType.STONE, 2120, 360, 200, 24));
        platforms.add(new Platform(Platform.PlatformType.STEP, 2400, 300, 70, 20));
        platforms.add(new Platform(Platform.PlatformType.STEP, 2520, 250, 70, 20));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 2680, 340, 160, 22));

        // Moving platforms
        platforms.add(new Platform(Platform.PlatformType.MOVING, 2920, 400, 140, 20)
                .withMovement(130, 0.038, 0.0));
        platforms.add(new Platform(Platform.PlatformType.MOVING, 3280, 320, 100, 18)
                .withMovement(100, 0.042, 1.2));

        // High route
        platforms.add(new Platform(Platform.PlatformType.STONE, 3180, 220, 120, 20));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 3380, 180, 90, 20));
        platforms.add(new Platform(Platform.PlatformType.STEP, 3560, 240, 110, 20));

        // Lower route with wide gap
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 3640, 460, 180, 24));
        platforms.add(new Platform(Platform.PlatformType.STONE, 3920, 380, 220, 26));
        platforms.add(new Platform(Platform.PlatformType.MOVING, 4220, 430, 150, 20)
                .withMovement(110, 0.032, 0.8));

        platforms.add(new Platform(Platform.PlatformType.PIPE, 4480, groundY - 280, 95, 280));
        platforms.add(new Platform(Platform.PlatformType.STONE, 4680, 320, 260, 28));

        // Finale tower
        platforms.add(new Platform(Platform.PlatformType.STEP, 4920, groundY - 40, 90, 40));
        platforms.add(new Platform(Platform.PlatformType.STEP, 5040, groundY - 120, 90, 120));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 5160, groundY - 220, 520, 220));
        
        // Extended section challenge
        platforms.add(new Platform(Platform.PlatformType.PIPE, 5800, groundY - 180, 100, 180));
        platforms.add(new Platform(Platform.PlatformType.MOVING, 6100, 300, 120, 20)
                .withMovement(150, 0.045, 0.0));
        platforms.add(new Platform(Platform.PlatformType.STONE, 6400, 220, 200, 24));
        platforms.add(new Platform(Platform.PlatformType.CONCRETE, 6750, 380, 150, 22));
        platforms.add(new Platform(Platform.PlatformType.STEP, 7050, 320, 100, 20));
        platforms.add(new Platform(Platform.PlatformType.MOVING, 7300, 250, 140, 20)
                .withMovement(100, 0.035, 1.5));
        platforms.add(new Platform(Platform.PlatformType.STONE, 7600, 180, 400, 30));
    }

    public void update() {
        for (Platform platform : platforms) {
            platform.update();
        }
    }

    public List<Rectangle> getCollisionRects() {
        List<Rectangle> rects = new ArrayList<>(platforms.size());
        for (Platform platform : platforms) {
            rects.add(platform.getBounds());
        }
        return rects;
    }

    public int getWorldMaxX() {
        int worldMaxX = 0;
        for (Platform platform : platforms) {
            if (platform.getRight() > worldMaxX) {
                worldMaxX = platform.getRight();
            }
        }
        return worldMaxX;
    }

    public void draw(Graphics2D g2, int cameraX) {
        for (Platform platform : platforms) {
            Rectangle r = platform.getBounds();
            int screenX = r.x - cameraX;
            if (screenX + r.width < -120 || screenX > gp.screenWidth + 120) {
                continue;
            }
            platform.draw(g2, cameraX);
        }
    }
}
