package main;

import javax.swing.JFrame;

import game.GamePanel;
import util.AssetPaths;

/**
 * Main class to bootstrap the Cyber Mouse game.
 */
public class Main {
    public static void main(String[] args) {
        AssetPaths.init();
        JFrame window = new JFrame("Cyber Mouse");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        
        // Size the window to fit the preferred size of its subcomponents
        window.pack();
        
        window.setLocationRelativeTo(null); // Center on screen
        window.setVisible(true);
        
        // Start the game thread
        gamePanel.startGameThread();
    }
}
