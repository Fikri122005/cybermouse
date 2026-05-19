package object;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class FinishLine {
    public int x, y;
    public int width = 40;
    public int height = 200;

    public FinishLine(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2, int cameraX) {
        int screenX = x - cameraX;
        
        // Draw checkered pole
        int squareSize = height / 10;
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) g2.setColor(Color.WHITE);
            else g2.setColor(Color.BLACK);
            g2.fillRect(screenX, y + (i * squareSize), width / 2, squareSize);
            
            if (i % 2 == 0) g2.setColor(Color.BLACK);
            else g2.setColor(Color.WHITE);
            g2.fillRect(screenX + (width / 2), y + (i * squareSize), width / 2, squareSize);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
