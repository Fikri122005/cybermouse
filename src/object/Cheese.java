package object;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Cheese {
    public int x, y;
    public int width = 30;
    public int height = 20;
    public boolean active = true;

    public Cheese(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void draw(Graphics2D g2, int cameraX) {
        if (!active) return;
        int screenX = x - cameraX;
        g2.setColor(Color.ORANGE);
        g2.fillRect(screenX, y, width, height);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
