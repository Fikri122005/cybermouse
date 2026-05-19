package tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

public class Platform {

    public enum PlatformType {
        /** Dark stone slab */
        STONE,
        /** Lighter concrete / worn stone */
        CONCRETE,
        /** Mario-style pipe */
        PIPE,
        /** Small tier / stair tread */
        STEP,
        /** Oscillates horizontally */
        MOVING
    }

    private final PlatformType type;
    private final int baseX;
    private final int y;
    private final int width;
    private final int height;

    private int x;
    private int moveRange;
    private double moveSpeed;
    private double movePhase;
    private int moveDeltaX;

    public Platform(PlatformType type, int x, int y, int width, int height) {
        this.type = type;
        this.baseX = x;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Platform withMovement(int moveRange, double moveSpeed, double startPhase) {
        this.moveRange = Math.max(0, moveRange);
        this.moveSpeed = moveSpeed;
        this.movePhase = startPhase;
        return this;
    }

    public void update() {
        int before = x;
        if (type == PlatformType.MOVING && moveRange > 0) {
            movePhase += moveSpeed;
            x = baseX + (int) Math.round(Math.sin(movePhase) * moveRange);
        }
        moveDeltaX = x - before;
    }

    public int getMoveDeltaX() {
        return moveDeltaX;
    }

    public PlatformType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getRight() {
        return x + width;
    }

    public void draw(Graphics2D g2, int cameraX) {
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int screenX = x - cameraX;
        if (type == PlatformType.PIPE) {
            drawPipe(g2, screenX);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
            return;
        }

        if (type == PlatformType.STEP) {
            drawStep(g2, screenX);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
            return;
        }

        if (type == PlatformType.MOVING) {
            drawMoving(g2, screenX);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
            return;
        }

        if (type == PlatformType.CONCRETE) {
            drawConcrete(g2, screenX);
        } else {
            drawStone(g2, screenX);
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
    }

    private void drawStone(Graphics2D g2, int screenX) {
        g2.setColor(new Color(72, 62, 58));
        g2.fillRoundRect(screenX, y, width, height, 8, 8);
        g2.setColor(new Color(110, 95, 88));
        g2.fillRoundRect(screenX + 3, y + 4, width - 6, height / 3, 6, 6);
        g2.setColor(new Color(38, 32, 30));
        g2.drawRoundRect(screenX, y, width, height, 8, 8);
        g2.setColor(new Color(25, 22, 20));
        for (int i = screenX + 18; i < screenX + width - 8; i += 42) {
            g2.drawLine(i, y + height / 2, i + 12, y + height - 6);
        }
    }

    private void drawConcrete(Graphics2D g2, int screenX) {
        g2.setColor(new Color(142, 142, 148));
        g2.fillRoundRect(screenX, y, width, height, 6, 6);
        g2.setColor(new Color(178, 178, 186));
        g2.fillRect(screenX + 4, y + 5, width - 8, Math.max(6, height / 5));
        g2.setColor(new Color(88, 88, 94));
        g2.drawRoundRect(screenX, y, width, height, 6, 6);
        g2.setColor(new Color(95, 95, 102));
        int rows = Math.max(2, width / 70);
        for (int r = 0; r < rows; r++) {
            int gx = screenX + 12 + r * 58;
            g2.drawLine(gx, y + height / 3, gx, y + height - 4);
        }
    }

    private void drawStep(Graphics2D g2, int screenX) {
        g2.setColor(new Color(120, 42, 42));
        g2.fillRoundRect(screenX, y, width, height, 6, 6);
        g2.setColor(new Color(180, 90, 70));
        g2.fillRoundRect(screenX + 2, y + 2, width - 4, Math.max(4, height / 3), 4, 4);
        g2.setColor(new Color(55, 22, 22));
        g2.drawRoundRect(screenX, y, width, height, 6, 6);
    }

    private void drawMoving(Graphics2D g2, int screenX) {
        g2.setColor(new Color(32, 108, 118));
        g2.fillRoundRect(screenX, y, width, height, 12, 12);
        g2.setColor(new Color(160, 235, 245));
        g2.fillRoundRect(screenX + 6, y + 5, width - 12, 7, 6, 6);
        g2.setColor(new Color(210, 250, 255));
        g2.drawRoundRect(screenX, y, width, height, 12, 12);
    }

    private void drawPipe(Graphics2D g2, int screenX) {
        int capHeight = Math.max(14, height / 4);
        g2.setColor(new Color(25, 135, 58));
        g2.fillRect(screenX, y, width, height);
        g2.setColor(new Color(40, 170, 70));
        g2.fillRect(screenX - 10, y, width + 20, capHeight);
        g2.setColor(new Color(12, 95, 38));
        g2.fillRect(screenX + 8, y + capHeight, 10, height - capHeight);
        g2.setColor(new Color(10, 90, 35));
        g2.drawRect(screenX, y, width, height);
        g2.drawRect(screenX - 10, y, width + 20, capHeight);
    }
}
