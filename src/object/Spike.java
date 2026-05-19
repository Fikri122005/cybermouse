package object;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;

/**
 * Static hazard; touching the hit area triggers game over (handled in {@link entity.Player}).
 */
public class Spike {

    public final int x;
    public final int y;
    public final int width;
    public final int height;
    private final int spikeCount;

    public Spike(int x, int y, int width, int height, int spikeCount) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.spikeCount = Math.max(1, spikeCount);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y - height, width, height);
    }

    public void draw(Graphics2D g2, int cameraX) {
        int screenX = x - cameraX;
        int baseY = y;
        float cell = width / (float) spikeCount;

        Path2D path = new Path2D.Float();
        for (int i = 0; i < spikeCount; i++) {
            float left = screenX + i * cell;
            float mid = left + cell / 2f;
            float right = left + cell;
            path.moveTo(left, baseY);
            path.lineTo(mid, baseY - height);
            path.lineTo(right, baseY);
        }

        g2.setColor(new Color(180, 30, 40));
        g2.fill(path);
        g2.setColor(new Color(60, 10, 15));
        g2.draw(path);
    }
}
