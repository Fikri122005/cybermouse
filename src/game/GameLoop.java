package game;

/**
 * Handles the main execution thread of the game, controlling FPS and updates.
 */
public class GameLoop implements Runnable {
    private GamePanel gamePanel;
    private boolean running = false;
    
    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    @Override
    public void run() {
        running = true;
        
        double drawInterval = 1000000000.0 / gamePanel.FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        
        while(running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            
            if (delta >= 1) {
                gamePanel.update();
                gamePanel.repaint();
                delta--;
            }
        }
    }
}
