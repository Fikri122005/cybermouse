package util;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Decodes common RIFF/WAVE payloads that {@link javax.sound.sampled.AudioSystem} often rejects
 * (IEEE float, 8‑bit PCM, odd chunk padding). Does not decode MP3-in-WAV or ADPCM.
 */
public final class WavPcmDecoder {

    private static final int WAVE_FORMAT_PCM = 0x0001;
    private static final int WAVE_FORMAT_IEEE_FLOAT = 0x0003;

    private WavPcmDecoder() {
    }

    public static final class DecodedPcm {
        public final AudioFormat format;
        public final byte[] samples;

        public DecodedPcm(AudioFormat format, byte[] samples) {
            this.format = format;
            this.samples = samples;
        }
    }

    /**
     * @return decoded PCM16 LE mono/stereo, or {@code null} if not a supported WAV
     */
    public static DecodedPcm tryDecode(Path wavFile) {
        if (wavFile == null || !Files.isRegularFile(wavFile)) {
            return null;
        }
        try {
            byte[] file = Files.readAllBytes(wavFile);
            if (file.length < 44) {
                return null;
            }
            if (!chunkId(file, 0, "RIFF") || !chunkId(file, 8, "WAVE")) {
                return null;
            }

            int audioFormat = -1;
            int numChannels = 0;
            int sampleRate = 0;
            int bitsPerSample = 0;
            int dataOffset = -1;
            int dataSize = 0;

            int offset = 12;
            while (offset + 8 <= file.length) {
                String id = readId(file, offset);
                int chunkSize = readLeInt(file, offset + 4);
                int contentStart = offset + 8;
                int next = contentStart + chunkSize + (chunkSize % 2);

                if ("fmt ".equals(id) && chunkSize >= 16) {
                    audioFormat = readLeShort(file, contentStart);
                    numChannels = readLeShort(file, contentStart + 2);
                    sampleRate = readLeInt(file, contentStart + 4);
                    bitsPerSample = readLeShort(file, contentStart + 14);
                } else if ("data".equals(id)) {
                    dataOffset = contentStart;
                    dataSize = chunkSize;
                    break;
                }
                offset = next;
            }

            if (audioFormat < 0 || dataOffset < 0 || dataSize <= 0 || numChannels < 1 || numChannels > 2) {
                return null;
            }
            if (dataOffset + dataSize > file.length) {
                dataSize = Math.max(0, file.length - dataOffset);
            }
            if (dataSize <= 0) {
                return null;
            }

            byte[] pcmData;
            if (audioFormat == WAVE_FORMAT_PCM && bitsPerSample == 16) {
                pcmData = new byte[dataSize];
                System.arraycopy(file, dataOffset, pcmData, 0, dataSize);
            } else if (audioFormat == WAVE_FORMAT_PCM && bitsPerSample == 8) {
                pcmData = convertUnsigned8ToPcm16(file, dataOffset, dataSize, numChannels);
            } else if (audioFormat == WAVE_FORMAT_IEEE_FLOAT && bitsPerSample == 32) {
                pcmData = convertFloat32ToPcm16(file, dataOffset, dataSize, numChannels);
            } else {
                return null;
            }

            AudioFormat out = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate,
                    16,
                    numChannels,
                    numChannels * 2,
                    sampleRate,
                    false);
            return new DecodedPcm(out, pcmData);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static byte[] convertUnsigned8ToPcm16(byte[] file, int dataOffset, int dataSize, int channels) {
        int frameBytes = channels;
        int frames = dataSize / frameBytes;
        ByteArrayOutputStream out = new ByteArrayOutputStream(frames * channels * 2);
        for (int f = 0; f < frames; f++) {
            for (int c = 0; c < channels; c++) {
                int unsigned = file[dataOffset + f * frameBytes + c] & 0xff;
                int signed = unsigned - 128;
                short s16 = (short) (signed << 8);
                out.write(s16 & 0xff);
                out.write((s16 >> 8) & 0xff);
            }
        }
        return out.toByteArray();
    }

    private static byte[] convertFloat32ToPcm16(byte[] file, int dataOffset, int dataSize, int channels) {
        int frames = dataSize / (4 * channels);
        ByteBuffer bb = ByteBuffer.wrap(file, dataOffset, dataSize).order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream out = new ByteArrayOutputStream(frames * channels * 2);
        for (int f = 0; f < frames; f++) {
            for (int c = 0; c < channels; c++) {
                float sample = bb.getFloat();
                if (sample > 1f) {
                    sample = 1f;
                } else if (sample < -1f) {
                    sample = -1f;
                }
                short s = (short) Math.round(sample * 32767.0);
                out.write(s & 0xff);
                out.write((s >> 8) & 0xff);
            }
        }
        return out.toByteArray();
    }

    private static boolean chunkId(byte[] b, int off, String id) {
        return off + 4 <= b.length && readId(b, off).equals(id);
    }

    private static String readId(byte[] b, int off) {
        return new String(b, off, 4, java.nio.charset.StandardCharsets.US_ASCII);
    }

    private static int readLeInt(byte[] b, int off) {
        return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8) | ((b[off + 2] & 0xff) << 16) | ((b[off + 3] & 0xff) << 24);
    }

    private static int readLeShort(byte[] b, int off) {
        return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
    }
}
