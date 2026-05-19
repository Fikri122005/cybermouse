package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import game.GamePanel;
import game.KeyHandler;
import object.Spike;
import tile.Platform;
import util.AssetPaths;
import util.CollisionChecker;

public class Player {

    private static final int LANDING_TOLERANCE_PX = 6;

    private final GamePanel gp;
    private final KeyHandler keyH;

    public int x;
    public int y;
    public int width;
    public int height;

    private double posX;
    private double posY;

    private double velX;
    private double velY;
    private final double moveAcceleration = 0.90;
    private final double maxMoveSpeed = 8.0;
    private final double groundFriction = 0.78;
    private final double airDrag = 0.97;
    private final double gravity = 0.52;
    private final double jumpVelocity = -14.2;
    private final double terminalVelocity = 16.0;
    private boolean onGround = false;
    private boolean jumpHeldLastFrame = false;
    private int coyoteFrames = 0;
    private int jumpBufferFrames = 0;
    private final int maxCoyoteFrames = 7;
    private final int maxJumpBufferFrames = 8;

    private int landingEffectFrames = 0;

    /** Hitbox aligned so feet match sprite bottom (sprite 96px tall). */
    /** Hitbox aligned to touch the ground exactly with the cropped sprite. */
    private final int hitboxOffsetX = 10;
    private final int hitboxOffsetY = 0;
    private final int hitboxWidth = 82;
    private final int hitboxHeight = 96;

    public BufferedImage image;

    public boolean isLevelComplete = false;

    private Platform ridingPlatform;
    
    // Animation variables
    private double walkCycle = 0;
    private double bobOffset = 0;
    private double rotation = 0;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private int lastFlip = 1;
    
    private int invincibilityFrames = 0;
    private final int maxInvincibilityFrames = 120; // 2 seconds at 60 FPS

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        setDefaultValues();
        getPlayerImage();
    }

    public void getPlayerImage() {
        try {
            image = ImageIO.read(AssetPaths.resolve("assets/player/cyber_rat.png").toFile());
        } catch (IOException e) {
            try {
                image = ImageIO.read(new File("assets/player/cyber_rat.png"));
            } catch (IOException ignored) {
                image = null;
            }
        }
    }

    public void setDefaultValues() {
        posX = 100;
        posY = 400;
        x = (int) Math.round(posX);
        y = (int) Math.round(posY);
        width = 102; // Adjusted for aspect ratio
        height = 96;
        velX = 0;
        velY = 0;
        onGround = false;
        jumpHeldLastFrame = false;
        coyoteFrames = 0;
        jumpBufferFrames = 0;
        landingEffectFrames = 0;
        isLevelComplete = false;
        ridingPlatform = null;
    }

    public void update() {
        if (isLevelComplete) {
            return;
        }

        List<Platform> platforms = gp.tileM.getPlatforms();

        if (ridingPlatform != null) {
            posX += ridingPlatform.getMoveDeltaX();
        }

        handleHorizontalInput();
        handleJumpInput();
        applyGravity();

        posX += velX;
        resolveHorizontalCollisions(platforms);

        double yBeforeStep = posY;
        posY += velY;
        onGround = false;
        Platform landedOn = resolveVerticalCollisions(platforms, yBeforeStep);

        ridingPlatform = onGround ? landedOn : null;

        clampWorldX();

        x = (int) Math.round(posX);
        y = (int) Math.round(posY);

        if (landingEffectFrames > 0) {
            landingEffectFrames--;
        }

        if (invincibilityFrames > 0) {
            invincibilityFrames--;
        }

        updateAnimation();

        if (y > gp.screenHeight + 160) {
            if (gp.stamina >= gp.maxStamina) {
                gp.stamina = 0;
                invincibilityFrames = maxInvincibilityFrames;
                // Teleport back to safe ground or just up
                posY = gp.screenHeight - 300;
                velY = -10;
                gp.audioM.playCheese(); // Play a sound to indicate save
            } else {
                gp.handleGameOver();
            }
        }

        checkObjectCollisions();
    }

    private void updateAnimation() {
        // Walk animation logic
        if (onGround && Math.abs(velX) > 0.5) {
            // Faster walking = faster bobbing
            walkCycle += Math.abs(velX) * 0.15;
            // Bob only UP from the ground (negative Y)
            bobOffset = -Math.abs(Math.sin(walkCycle)) * 6; 
            rotation = Math.sin(walkCycle) * 0.1; 
            
            // Squash and stretch during walk
            scaleX = 1.0 + Math.abs(Math.sin(walkCycle)) * 0.1;
            scaleY = 1.0 - Math.abs(Math.sin(walkCycle)) * 0.1;
        } else {
            // Reset/Idle smoothing
            walkCycle = 0;
            bobOffset *= 0.8;
            rotation *= 0.8;
            scaleX = scaleX + (1.0 - scaleX) * 0.2;
            scaleY = scaleY + (1.0 - scaleY) * 0.2;
        }

        // Jump/Fall stretch
        if (!onGround) {
            scaleY = 1.0 + Math.abs(velY) * 0.02;
            scaleX = 1.0 - Math.abs(velY) * 0.01;
            rotation = velX * 0.02;
        }

        // Landing squash
        if (landingEffectFrames > 0) {
            scaleY = 0.8;
            scaleX = 1.2;
        }
    }

    private void handleHorizontalInput() {
        int input = 0;
        if (keyH.leftPressed) {
            input--;
        }
        if (keyH.rightPressed) {
            input++;
        }

        if (input != 0) {
            double accel = onGround ? moveAcceleration : moveAcceleration * 0.6;
            velX += input * accel;
        } else {
            velX *= onGround ? groundFriction : airDrag;
            if (Math.abs(velX) < 0.05) {
                velX = 0;
            }
        }

        if (velX > maxMoveSpeed) {
            velX = maxMoveSpeed;
        }
        if (velX < -maxMoveSpeed) {
            velX = -maxMoveSpeed;
        }
    }

    private void handleJumpInput() {
        boolean jumpPressedNow = keyH.upPressed;
        if (jumpPressedNow && !jumpHeldLastFrame) {
            jumpBufferFrames = maxJumpBufferFrames;
        }

        if (jumpBufferFrames > 0) {
            jumpBufferFrames--;
        }

        if (onGround) {
            coyoteFrames = maxCoyoteFrames;
        } else if (coyoteFrames > 0) {
            coyoteFrames--;
        }

        if (jumpBufferFrames > 0 && coyoteFrames > 0) {
            velY = jumpVelocity;
            onGround = false;
            coyoteFrames = 0;
            jumpBufferFrames = 0;
            ridingPlatform = null;
            gp.audioM.playJump();
        }

        if (!jumpPressedNow && velY < 0) {
            velY *= 0.86;
        }

        jumpHeldLastFrame = jumpPressedNow;
    }

    private void applyGravity() {
        velY += gravity;
        if (velY > terminalVelocity) {
            velY = terminalVelocity;
        }
    }

    private void resolveHorizontalCollisions(List<Platform> platforms) {
        Rectangle playerHitbox = getCollisionBoundsAt(posX, posY);
        for (Platform p : platforms) {
            Rectangle platform = p.getBounds();
            if (!CollisionChecker.checkCollision(playerHitbox, platform)) {
                continue;
            }

            if (velX > 0) {
                posX = platform.x - hitboxWidth - hitboxOffsetX;
            } else if (velX < 0) {
                posX = platform.x + platform.width - hitboxOffsetX;
            }
            velX = 0;
            playerHitbox = getCollisionBoundsAt(posX, posY);
        }
    }

    /**
     * Separates vertical motion. Only treat as “landing” if the feet crossed the platform top
     * this frame (prevents snapping when hitting the side while falling).
     *
     * @return platform stood on after landing, or null
     */
    private Platform resolveVerticalCollisions(List<Platform> platforms, double yBeforeStep) {
        Rectangle playerHitbox = getCollisionBoundsAt(posX, posY);
        Rectangle prevHitbox = getCollisionBoundsAt(posX, yBeforeStep);

        int prevBottom = prevHitbox.y + prevHitbox.height;
        int prevTop = prevHitbox.y;

        Platform landedPlatform = null;
        int shallowestTopY = Integer.MAX_VALUE;

        if (velY > 0) {
            for (Platform p : platforms) {
                Rectangle platform = p.getBounds();
                if (!playerHitbox.intersects(platform)) {
                    continue;
                }
                boolean crossedFromAbove = prevBottom <= platform.y + LANDING_TOLERANCE_PX;
                if (!crossedFromAbove) {
                    continue;
                }
                if (platform.y < shallowestTopY) {
                    shallowestTopY = platform.y;
                    landedPlatform = p;
                }
            }

            if (landedPlatform != null) {
                Rectangle plat = landedPlatform.getBounds();
                posY = plat.y - hitboxHeight - hitboxOffsetY;
                boolean hardLanding = velY > 8.0;
                velY = 0;
                onGround = true;
                if (hardLanding) {
                    landingEffectFrames = 8;
                }
                playerHitbox = getCollisionBoundsAt(posX, posY);
            }
        }

        if (!onGround && velY < 0) {
            for (Platform p : platforms) {
                Rectangle platform = p.getBounds();
                if (!playerHitbox.intersects(platform)) {
                    continue;
                }
                int platBottom = platform.y + platform.height;
                boolean crossedFromBelow = prevTop >= platBottom - LANDING_TOLERANCE_PX;
                if (!crossedFromBelow) {
                    continue;
                }
                posY = platBottom - hitboxOffsetY;
                velY = 0;
                playerHitbox = getCollisionBoundsAt(posX, posY);
                break;
            }
        }

        return landedPlatform;
    }

    private void clampWorldX() {
        int worldMaxX = gp.tileM.getWorldMaxX();

        double minX = 0;
        double maxX = Math.max(0, worldMaxX - hitboxWidth - hitboxOffsetX);
        if (posX < minX) {
            posX = minX;
            velX = 0;
        } else if (posX > maxX) {
            posX = maxX;
            velX = 0;
        }
    }

    private void checkObjectCollisions() {
        Rectangle playerBounds = getCollisionBounds();

        if (gp.coins != null) {
            for (object.Coin c : gp.coins) {
                if (c.active && CollisionChecker.checkCollision(playerBounds, c.getBounds())) {
                    c.active = false;
                    gp.score += 10;
                    gp.audioM.playCoin();
                }
            }
        }
        if (gp.cheeses != null) {
            for (object.Cheese c : gp.cheeses) {
                if (c.active && CollisionChecker.checkCollision(playerBounds, c.getBounds())) {
                    c.active = false;
                    if (gp.stamina < gp.maxStamina) {
                        gp.stamina++;
                    }
                    gp.audioM.playCheese();
                }
            }
        }
        if (gp.spikes != null) {
            for (Spike s : gp.spikes) {
                if (CollisionChecker.checkCollision(playerBounds, s.getBounds())) {
                    if (invincibilityFrames > 0) {
                        continue;
                    }
                    if (gp.stamina >= gp.maxStamina) {
                        gp.stamina = 0;
                        invincibilityFrames = maxInvincibilityFrames;
                        gp.audioM.playCheese(); // Play sound to indicate save
                        // Give a little jump to escape the spike
                        velY = -8;
                    } else {
                        gp.handleGameOver();
                        return;
                    }
                }
            }
        }
        if (gp.birds != null) {
            for (Bird b : gp.birds) {
                if (CollisionChecker.checkCollision(playerBounds, b.getBounds())) {
                    if (invincibilityFrames > 0) {
                        continue;
                    }
                    if (gp.stamina >= gp.maxStamina) {
                        gp.stamina = 0;
                        invincibilityFrames = maxInvincibilityFrames;
                        gp.audioM.playCheese(); 
                        velY = -8;
                    } else {
                        gp.handleGameOver();
                        return;
                    }
                }
            }
        }
        if (gp.finishLine != null) {
            if (CollisionChecker.checkCollision(playerBounds, gp.finishLine.getBounds())) {
                isLevelComplete = true;
                gp.handleLevelComplete();
            }
        }
    }

    public void draw(Graphics2D g2, int cameraX) {
        int screenX = x - cameraX;
        int drawY = y;

        if (image != null) {
            // Flip image based on direction
            if (velX < -0.1) lastFlip = -1;
            else if (velX > 0.1) lastFlip = 1;
            
            java.awt.geom.AffineTransform oldAT = g2.getTransform();
            
            // Center of the sprite for rotation and scaling
            double cx = screenX + width / 2.0;
            double cy = drawY + height / 2.0;
            
            g2.translate(cx, cy);
            g2.rotate(rotation * lastFlip);
            g2.scale(scaleX * lastFlip, scaleY);
            
            // Blink if invincible
            if (invincibilityFrames == 0 || (invincibilityFrames / 8) % 2 == 0) {
                g2.drawImage(image, -width / 2, -height / 2 + (int)bobOffset, width, height, null);
            }
            
            g2.setTransform(oldAT);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRect(screenX, drawY, width, height);
        }

        if (landingEffectFrames > 0) {
            g2.setColor(new Color(240, 240, 240, 90));
            g2.fillOval(screenX + 12, y + height - 8, width - 24, 10);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public Rectangle getCollisionBounds() {
        return getCollisionBoundsAt(posX, posY);
    }

    private Rectangle getCollisionBoundsAt(double worldX, double worldY) {
        return new Rectangle(
                (int) Math.round(worldX) + hitboxOffsetX,
                (int) Math.round(worldY) + hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
        );
    }
}
