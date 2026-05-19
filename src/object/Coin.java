package object;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Coin {
    public int x, y;
    public int size = 20;
    public boolean active = true;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void draw(Graphics2D g2, int cameraX) {
        if (!active) return;
        int screenX = x - cameraX;
        g2.setColor(Color.YELLOW);
        g2.fillOval(screenX, y, size, size);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }
}
