package entity;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bird {
    public int x, y;
    public double speed = 3.5;
    public int width = 30, height = 20;

    public Bird(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        x -= speed; // moves left
    }

    public void draw(Graphics2D g2, int cameraX) {
        int screenX = x - cameraX;
        if (screenX + width < -100 || screenX > 1500) return; // simple culling
        
        g2.setColor(new Color(20, 20, 20)); // dark bird
        g2.fillOval(screenX, y, width, height);
        g2.setColor(new Color(60, 0, 0)); // red eyes
        g2.fillOval(screenX + 5, y + 5, 4, 4);
        
        g2.setColor(Color.DARK_GRAY); // wing
        g2.fillRect(screenX + 15, y - 5, 10, 15);
    }
    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle(x, y, width, height);
    }
}
