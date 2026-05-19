package entity;

import java.awt.Color;
import java.awt.Graphics2D;

public class Cloud {

    public double x;
    public int y;
    public double speed;
    private final double wrapAfter;

    public Cloud(int x, int y, double speed) {
        this(x, y, speed, 7200);
    }

    public Cloud(int x, int y, double speed, double wrapAfter) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.wrapAfter = wrapAfter;
    }

    public void update() {
        x += speed;
        if (x > wrapAfter) {
            x = -280;
        }
    }

    public void draw(Graphics2D g2, int cameraX) {
        int screenX = (int) x - (int) (cameraX * 0.78);

        g2.setColor(new Color(255, 255, 255, 72));
        g2.fillOval(screenX, y, 180, 60);
        g2.fillOval(screenX + 40, y - 30, 120, 80);
        g2.fillOval(screenX + 100, y + 10, 100, 40);
    }
}
