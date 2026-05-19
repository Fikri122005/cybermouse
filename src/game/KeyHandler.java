package game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    GamePanel gp;
    public boolean upPressed, leftPressed, rightPressed;
    
    // Command Navigation
    public int commandNum = 0;
    
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_M) {
            gp.audioM.toggleMute();
            return;
        }
        
        // TITLE STATE
        if (gp.gameState == gp.titleState) {
            int lastCmd = MenuScreen.CMD_COUNT - 1;
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.audioM.playSelect();
                commandNum--;
                if (commandNum < 0) {
                    commandNum = lastCmd;
                }
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.audioM.playSelect();
                commandNum++;
                if (commandNum > lastCmd) {
                    commandNum = 0;
                }
            }
            if (code == KeyEvent.VK_ENTER) {
                gp.audioM.playSelect();
                if (commandNum == 0) {
                    gp.startNewGame();
                }
                if (commandNum == 1) {
                    gp.loadSavedGame();
                }
                if (commandNum == 2) {
                    System.exit(0); // Exit
                }
            }
        } 
        // PLAY STATE
        else if (gp.gameState == gp.playState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_SPACE) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
        }
        // LEVEL COMPLETE — return to menu
        else if (gp.gameState == gp.finishState) {
            if (code == KeyEvent.VK_ENTER) {
                gp.setupGame();
            }
        }
        // GAME OVER STATE
        else if (gp.gameState == gp.gameOverState) {
            if (code == KeyEvent.VK_ENTER) {
                gp.setupGame();
            }
            if (code == KeyEvent.VK_R) {
                gp.retryLevel();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (gp.gameState == gp.playState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_SPACE) {
                upPressed = false;
            }
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
        }
    }
}
