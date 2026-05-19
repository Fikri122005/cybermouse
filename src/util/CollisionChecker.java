package util;

import java.awt.Rectangle;

public class CollisionChecker {
    
    // Check collision between two rectangles
    public static boolean checkCollision(Rectangle r1, Rectangle r2) {
        return r1.intersects(r2);
    }
}
