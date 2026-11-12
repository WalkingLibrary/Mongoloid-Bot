package com.jumbodinosaurs.mongoloidbot.brains;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Combined PCM capture and playback. The consumer runs on a dedicated worker. */
public final class VoiceAudio implements AudioReceiveHandler, AudioSendHandler, AutoCloseable {
    public enum State { LISTENING, RECORDING, PROCESSING, SPEAKING }
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private final Consumer<byte[]> recordings;
    private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
    private State state = State.LISTENING;
    private boolean enabled;
    private long lastSpeech, generation;
    private byte[] playback;
    private int position;
    public VoiceAudio(Consumer<byte[]> recordings) {
        this.recordings = recordings;
        worker.scheduleAtFixedRate(this::checkSilence, 100, 100, TimeUnit.MILLISECONDS);
    }
    public synchronized void start() { enabled = true; }
    /** JDA checks combined-receive capability while installing the handler. */
    public void attach(net.dv8tion.jda.api.managers.AudioManager manager) {
        start();
        try {
            manager.setReceivingHandler(this);
            manager.setSendingHandler(this);
            com.jumbodinosaurs.devlib.log.LogManager.consoleLogger.info("[VOICE] Listening started; combined audio enabled");
        } catch (RuntimeException failure) {
            close();
            manager.setReceivingHandler(null);
            manager.setSendingHandler(null);
            throw failure;
        }
    }
    public synchronized void stop() {
        enabled = false; generation++; capture.reset(); playback = null;
        position = 0; state = State.LISTENING;
    }
    public synchronized State getState() { return state; }
    @Override public synchronized boolean canReceiveCombined() {
        return enabled && (state == State.LISTENING || state == State.RECORDING);
    }
    @Override public void handleCombinedAudio(CombinedAudio audio) { accept(audio.getAudioData(1.0)); }
    synchronized void accept(byte[] pcm) {
        if (!canReceiveCombined()) return;
        long now = System.nanoTime();
        double sum = 0;
        for (int i = 0; i + 1 < pcm.length; i += 2) {
            int sample = (short)(((pcm[i] & 255) << 8) | (pcm[i + 1] & 255));
            sum += (double)sample * sample;
        }
        boolean speech = pcm.length >= 2 && Math.sqrt(sum / (pcm.length / 2)) >= 500;
        if (speech) { if (state == State.LISTENING) com.jumbodinosaurs.devlib.log.LogManager.consoleLogger.info("[VOICE] Speech detected"); lastSpeech = now; state = State.RECORDING; }
        if (state != State.RECORDING) return;
        int remaining = 48000 * 4 * 15 - capture.size();
        capture.write(pcm, 0, Math.min(remaining, pcm.length));
        if (capture.size() >= 48000 * 4 * 15) finish();
    }
    private synchronized void checkSilence() {
        if (enabled && state == State.RECORDING && System.nanoTime() - lastSpeech >= 1000000000L) finish();
    }
    private void finish() {
        byte[] pcm = capture.toByteArray(); capture.reset(); state = State.PROCESSING;
        com.jumbodinosaurs.devlib.log.LogManager.consoleLogger.info("[VOICE] Recording completed: " + pcm.length / 192 + " ms");
        final long token = generation;
        worker.execute(() -> {
            synchronized (this) { if (!enabled || generation != token) return; }
            try { recordings.accept(wav(pcm)); }
            finally { synchronized (this) {
                if (generation == token && state == State.PROCESSING) state = State.LISTENING;
            } }
        });
    }
    public synchronized void play(byte[] pcm) {
        if (!enabled || pcm.length == 0) return;
        com.jumbodinosaurs.devlib.log.LogManager.consoleLogger.info("[VOICE] Playback started");
        playback = Arrays.copyOf(pcm, pcm.length); position = 0;
        state = State.SPEAKING;
    }
    @Override public synchronized boolean canProvide() {
        return enabled && playback != null && position < playback.length;
    }
    @Override public synchronized ByteBuffer provide20MsAudio() {
        if (!canProvide()) return null;
        byte[] frame = new byte[3840];
        int count = Math.min(frame.length, playback.length - position);
        System.arraycopy(playback, position, frame, 0, count); position += count;
        if (position == playback.length) {
            playback = null;
            final long token = generation;
            worker.schedule(() -> { synchronized (this) {
                if (generation == token && state == State.SPEAKING) { state = State.LISTENING; com.jumbodinosaurs.devlib.log.LogManager.consoleLogger.info("[VOICE] Playback completed"); }
            } }, 40, TimeUnit.MILLISECONDS);
        }
        return ByteBuffer.wrap(frame);
    }
    public static byte[] wav(byte[] bigEndianPcm) {
        ByteBuffer out = ByteBuffer.allocate(44 + bigEndianPcm.length).order(ByteOrder.LITTLE_ENDIAN);
        out.put(new byte[]{'R','I','F','F'}).putInt(36 + bigEndianPcm.length);
        out.put(new byte[]{'W','A','V','E','f','m','t',' '}).putInt(16);
        out.putShort((short)1).putShort((short)2).putInt(48000).putInt(192000);
        out.putShort((short)4).putShort((short)16).put(new byte[]{'d','a','t','a'}).putInt(bigEndianPcm.length);
        for (int i = 0; i + 1 < bigEndianPcm.length; i += 2) out.put(bigEndianPcm[i+1]).put(bigEndianPcm[i]);
        return out.array();
    }
    @Override public void close() { stop(); worker.shutdownNow(); }
}
