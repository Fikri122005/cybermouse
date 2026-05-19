package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Loads campus / title logo trying several common names and extensions.
 */
public final class LogoLoader {

    private static final List<String> CANDIDATES = Arrays.asList(
            "assets/logo/campus_logo.png",
            "assets/logo/campus_logo.jpg",
            "assets/logo/campus_logo.jpeg",
            "assets/logo/logo.png",
            "assets/logo/logo.jpg",
            "assets/logo/logo.jpeg",
            "assets/campus_logo.png",
            "assets/campus_logo.jpg"
    );

    private LogoLoader() {
    }

    public static BufferedImage load() {
        AssetPaths.init();
        for (String rel : CANDIDATES) {
            Path p = AssetPaths.resolve(rel);
            if (!Files.isRegularFile(p)) {
                continue;
            }
            try (InputStream in = Files.newInputStream(p)) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    return img;
                }
                System.err.println("[Image] Unreadable (wrong format?): " + p.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("[Image] " + p.toAbsolutePath() + " — " + e.getMessage());
            }
        }

        System.err.println("[Image] Logo tidak ditemukan. Simpan di: "
                + AssetPaths.resolve("assets/logo").toAbsolutePath()
                + " dengan nama campus_logo.png / campus_logo.jpg / logo.png");
        return null;
    }
}
