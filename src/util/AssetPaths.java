package util;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import main.Main;

/**
 * Resolves asset paths when the working directory is not the project root
 * (IDE runs from {@code bin/}, OneDrive sync paths, etc.).
 */
public final class AssetPaths {

    private static Path baseDir;

    private AssetPaths() {
    }

    public static void init() {
        if (baseDir != null) {
            return;
        }

        Path fromClass = findRootFromMainClass();
        if (fromClass != null) {
            baseDir = fromClass;
            return;
        }

        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        if (hasAssets(cwd)) {
            baseDir = cwd;
            return;
        }

        Path probe = cwd;
        for (int i = 0; i < 10 && probe != null; i++) {
            if (hasAssets(probe)) {
                baseDir = probe;
                return;
            }
            probe = probe.getParent();
        }

        baseDir = cwd;
    }

    /**
     * Walk up from {@code bin/}, {@code out/production/...}, etc. until a folder contains {@code assets/}.
     */
    private static Path findRootFromMainClass() {
        try {
            URL loc = Main.class.getProtectionDomain().getCodeSource().getLocation();
            if (loc == null) {
                return null;
            }
            Path start = Paths.get(loc.toURI()).toAbsolutePath().normalize();
            if (!Files.exists(start)) {
                return null;
            }
            if (Files.isRegularFile(start)) {
                return null;
            }
            Path p = start;
            for (int i = 0; i < 12 && p != null; i++) {
                if (hasAssets(p)) {
                    return p;
                }
                p = p.getParent();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean hasAssets(Path dir) {
        return Files.isDirectory(dir.resolve("assets"));
    }

    /**
     * @param relativePath path like {@code assets/sound/bgm.wav}
     */
    public static Path resolve(String relativePath) {
        init();
        Path raw = Paths.get(relativePath);
        if (raw.isAbsolute()) {
            return raw.normalize();
        }

        Path direct = Paths.get(System.getProperty("user.dir", ".")).resolve(raw).normalize();
        if (Files.exists(direct)) {
            return direct;
        }

        Path fromBase = baseDir.resolve(raw).normalize();
        if (Files.exists(fromBase)) {
            return fromBase;
        }

        return fromBase;
    }

    /**
     * First path that exists on disk, or {@code null}.
     */
    public static Path firstExisting(String... relativePaths) {
        init();
        for (String rel : relativePaths) {
            Path p = resolve(rel);
            if (Files.exists(p)) {
                return p;
            }
        }
        return null;
    }
}
