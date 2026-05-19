package game;

import util.AssetPaths;
import util.WavPcmDecoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads and plays PCM audio via {@link Clip}. BGM loops on a background thread so open/start
 * does not block the game loop.
 */
public class AudioManager {

    private static final String BGM_PATH = "assets/sound/sound_background.wav";
    private static final String COIN_PATH = "assets/sound/coin.wav";
    private static final String CHEESE_PATH = "assets/sound/cheese.wav";
    private static final String FINISH_PATH = "assets/sound/finish.wav";
    private static final String GAME_OVER_PATH = "assets/sound/gameover.wav";
    private static final String JUMP_PATH = "assets/sound/sound_jump.wav";
    private static final String SELECT_PATH = "assets/sound/select.wav";

    private final Map<String, SoundData> soundCache = new HashMap<>();
    private final ExecutorService sfxExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SFX-Thread");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService bgmExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BGM-Thread");
        t.setDaemon(true);
        return t;
    });

    private Clip bgmClip;
    private boolean muted = false;
    private float bgmVolume = 0.70f;
    private float sfxVolume = 0.85f;
    private Clip selectClip; // Track selection clip to prevent overlapping
    private final Clip[] jumpClips = new Clip[3];
    private int jumpClipIndex = 0;

    public AudioManager() {
        preloadDefaultSounds();
    }

    public void playBGM() {
        startBgmInternal(false, 0);
    }

    public void playBGMWithFadeIn(int durationMs) {
        startBgmInternal(true, durationMs);
    }

    private void startBgmInternal(boolean fadeIn, int fadeDurationMs) {
        stopBGMInternal();
        if (muted) {
            return;
        }
        bgmExecutor.execute(() -> {
            try {
                SoundData data = loadSoundData(BGM_PATH);
                if (data == null) {
                    System.err.println("[Audio] BGM missing or unreadable: " + AssetPaths.resolve(BGM_PATH));
                    return;
                }
                Clip clip = AudioSystem.getClip();
                clip.open(data.format, data.audioBytes, 0, data.audioBytes.length);
                float startVol = fadeIn ? 0f : bgmVolume;
                applyVolume(clip, startVol);
                synchronized (AudioManager.this) {
                    bgmClip = clip;
                }
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                if (fadeIn && fadeDurationMs > 0) {
                    fadeClipVolume(clip, 0.0f, bgmVolume, fadeDurationMs);
                } else if (!fadeIn) {
                    applyVolume(clip, bgmVolume);
                }
            } catch (Exception e) {
                System.err.println("[Audio] BGM error: " + e.getMessage());
            }
        });
    }

    public synchronized void stopBGM() {
        stopBGMInternal();
    }

    private void stopBGMInternal() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.close();
            bgmClip = null;
        }
    }

    public void stopBGMWithFadeOut(int durationMs) {
        Clip clipToFade;
        synchronized (this) {
            clipToFade = bgmClip;
        }
        if (clipToFade == null) {
            return;
        }
        if (durationMs <= 0) {
            stopBGM();
            return;
        }
        fadeClipVolume(clipToFade, bgmVolume, 0.0f, durationMs);
        Thread t = new Thread(() -> {
            sleep(durationMs + 40);
            synchronized (AudioManager.this) {
                if (clipToFade == bgmClip) {
                    stopBGMInternal();
                }
            }
        }, "BGM-FadeOut");
        t.setDaemon(true);
        t.start();
    }

    public synchronized void pauseBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    public synchronized void resumeBGM() {
        if (bgmClip != null && !muted) {
            bgmClip.start();
        }
    }

    public void playCoin() {
        playSfx(COIN_PATH, "coin");
    }

    public void playCheese() {
        playSfx(CHEESE_PATH, "cheese");
    }

    public void playFinish() {
        playSfx(FINISH_PATH, "finish");
    }

    public void playGameOver() {
        playSfx(GAME_OVER_PATH, "game over");
    }

    public void playJump() {
        synchronized (jumpClips) {
            Clip clip = jumpClips[jumpClipIndex];
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                clip.start();
            } else {
                // Fallback to normal play if not preloaded (using thread)
                playSfx(JUMP_PATH, "jump");
            }
            jumpClipIndex = (jumpClipIndex + 1) % jumpClips.length;
        }
    }

    public void playSelect() {
        stopSelectSound();
        playSelectSfx();
    }

    private synchronized void stopSelectSound() {
        if (selectClip != null) {
            if (selectClip.isRunning()) {
                selectClip.stop();
            }
            selectClip.close();
            selectClip = null;
        }
    }

    private void playSelectSfx() {
        if (muted) return;
        sfxExecutor.execute(() -> {
            try {
                SoundData data = loadSoundData(SELECT_PATH);
                if (data == null) return;
                
                Clip clip = AudioSystem.getClip();
                clip.open(data.format, data.audioBytes, 0, data.audioBytes.length);
                
                float variation = 0.96f + (new java.util.Random().nextFloat() * 0.08f);
                applyVolume(clip, sfxVolume * variation);
                
                synchronized (this) {
                    selectClip = clip;
                }
                
                clip.start();
                
                // Batasi durasi hanya setengah detik (500ms)
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        synchronized (this) {
                            if (selectClip == clip) {
                                stopSelectSound();
                            }
                        }
                    } catch (InterruptedException ignored) {}
                }).start();
            } catch (Exception e) {
                System.err.println("[Audio] Select SFX error: " + e.getMessage());
            }
        });
    }

    public void setBgmVolume(float volume) {
        bgmVolume = clamp01(volume);
        synchronized (this) {
            if (!muted && bgmClip != null) {
                applyVolume(bgmClip, bgmVolume);
            }
        }
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp01(volume);
        synchronized (jumpClips) {
            for (Clip clip : jumpClips) {
                if (clip != null && clip.isOpen()) {
                    applyVolume(clip, sfxVolume);
                }
            }
        }
    }

    public float getBgmVolume() {
        return bgmVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public boolean isMuted() {
        return muted;
    }

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            pauseBGM();
        } else {
            synchronized (this) {
                if (bgmClip != null) {
                    applyVolume(bgmClip, bgmVolume);
                    resumeBGM();
                }
            }
        }
    }

    private void playSfx(String filePath, String label) {
        if (muted) {
            return;
        }
        sfxExecutor.execute(() -> {
            try {
                SoundData data = loadSoundData(filePath);
                if (data == null) {
                    System.err.println("[Audio] SFX missing (" + label + "): " + AssetPaths.resolve(filePath));
                    return;
                }
                Clip clip = AudioSystem.getClip();
                clip.open(data.format, data.audioBytes, 0, data.audioBytes.length);
                
                // Variasikan volume sedikit (misal antara -5% sampai +5%) agar tidak monoton
                float variation = 0.94f + (new java.util.Random().nextFloat() * 0.12f);
                applyVolume(clip, sfxVolume * variation);
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (Exception e) {
                System.err.println("[Audio] SFX error (" + label + "): " + e.getMessage());
            }
        });
    }

    private void preloadDefaultSounds() {
        sfxExecutor.execute(() -> {
            loadSoundData(BGM_PATH);
            loadSoundData(COIN_PATH);
            loadSoundData(CHEESE_PATH);
            loadSoundData(FINISH_PATH);
            loadSoundData(GAME_OVER_PATH);
            loadSoundData(JUMP_PATH);
            loadSoundData(SELECT_PATH);
            
            // Pre-open jump clips for zero-latency
            try {
                SoundData data = loadSoundData(JUMP_PATH);
                if (data != null) {
                    for (int i = 0; i < jumpClips.length; i++) {
                        jumpClips[i] = AudioSystem.getClip();
                        jumpClips[i].open(data.format, data.audioBytes, 0, data.audioBytes.length);
                        applyVolume(jumpClips[i], sfxVolume);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Audio] Failed to pre-open jump clips: " + e.getMessage());
            }
        });
    }

    private SoundData loadSoundData(String relativePath) {
        synchronized (soundCache) {
            if (soundCache.containsKey(relativePath)) {
                return soundCache.get(relativePath);
            }
        }

        Path path = AssetPaths.resolve(relativePath);
        if (!Files.exists(path)) {
            synchronized (soundCache) {
                soundCache.put(relativePath, null);
            }
            return null;
        }

        SoundData data = decodeSoundFile(path);
        synchronized (soundCache) {
            soundCache.put(relativePath, data);
        }
        return data;
    }

    /**
     * Tries Java Sound API first, then a manual WAV decoder (float / 8‑bit PCM) for files
     * that Windows / Java cannot open as-is.
     */
    private SoundData decodeSoundFile(Path path) {
        AudioInputStream raw = null;
        try {
            raw = AudioSystem.getAudioInputStream(path.toFile());
            AudioFormat sourceFormat = raw.getFormat();
            boolean needsPcm16 = sourceFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
                    || sourceFormat.getSampleSizeInBits() != 16;

            if (needsPcm16) {
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.getSampleRate(),
                        16,
                        sourceFormat.getChannels(),
                        sourceFormat.getChannels() * 2,
                        sourceFormat.getSampleRate(),
                        false);
                try (AudioInputStream pcm = AudioSystem.getAudioInputStream(targetFormat, raw)) {
                    raw = null;
                    return readSoundData(pcm);
                }
            } else {
                try {
                    return readSoundData(raw);
                } finally {
                    raw.close();
                    raw = null;
                }
            }
        } catch (Exception e) {
            if (raw != null) {
                try {
                    raw.close();
                } catch (Exception ignored) {
                }
            }
            System.err.println("[Audio] AudioSystem gagal untuk " + path.getFileName() + ": " + e.getMessage());
        }

        WavPcmDecoder.DecodedPcm decoded = WavPcmDecoder.tryDecode(path);
        if (decoded != null) {
            return new SoundData(decoded.format, decoded.samples);
        }

        System.err.println("[Audio] File tidak didukung: " + path.toAbsolutePath());
        System.err.println("[Audio] Ekspor ulang sebagai WAV PCM 16-bit (Audacity: Export → WAV). "
                + "Jangan mengganti nama .mp3 menjadi .wav — isi file harus benar-benar WAV.");
        return null;
    }

    private static SoundData readSoundData(AudioInputStream in) throws java.io.IOException {
        AudioFormat dataFormat = in.getFormat();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new SoundData(dataFormat, out.toByteArray());
        }
    }

    private void applyVolume(Clip clip, float volume01) {
        if (clip == null) {
            return;
        }
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (volume01 <= 0f) {
            gain.setValue(gain.getMinimum());
            return;
        }

        float dB = (float) (20.0 * Math.log10(volume01));
        dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
        gain.setValue(dB);
    }

    private void fadeClipVolume(Clip clip, float from, float to, int durationMs) {
        Thread fadeThread = new Thread(() -> {
            int steps = 20;
            float delta = (to - from) / steps;
            for (int i = 0; i <= steps; i++) {
                if (clip == null || !clip.isOpen()) {
                    return;
                }
                float value = clamp01(from + (delta * i));
                applyVolume(clip, value);
                sleep(Math.max(10, durationMs / steps));
            }
        }, "Audio-Fade");
        fadeThread.setDaemon(true);
        fadeThread.start();
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static class SoundData {
        private final AudioFormat format;
        private final byte[] audioBytes;

        private SoundData(AudioFormat format, byte[] audioBytes) {
            this.format = format;
            this.audioBytes = audioBytes;
        }
    }
}
