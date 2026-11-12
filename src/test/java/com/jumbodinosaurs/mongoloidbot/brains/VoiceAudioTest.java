package com.jumbodinosaurs.mongoloidbot.brains;
import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.*;
import java.util.concurrent.*;
public class VoiceAudioTest {
    @Test public void combinedReceivingIsEnabledBeforeJdaRegistersHandler() {
        java.util.concurrent.atomic.AtomicBoolean registered = new java.util.concurrent.atomic.AtomicBoolean();
        net.dv8tion.jda.api.managers.AudioManager manager = (net.dv8tion.jda.api.managers.AudioManager)
            java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{net.dv8tion.jda.api.managers.AudioManager.class}, (proxy, method, args) -> {
                    if (method.getName().equals("setReceivingHandler")) {
                        net.dv8tion.jda.api.audio.AudioReceiveHandler receiver = (net.dv8tion.jda.api.audio.AudioReceiveHandler)args[0];
                        assertTrue("JDA must see combined capability during registration",receiver.canReceiveCombined());
                        registered.set(true);
                    }
                    return null;
                });
        try (VoiceAudio audio = new VoiceAudio(w -> fail())) {
            assertFalse(audio.canReceiveCombined());
            audio.attach(manager); assertTrue(registered.get());
        }
    }
    @Test public void wavHasCorrectHeaderAndSampleOrder() {
        byte[] wav = VoiceAudio.wav(new byte[]{0x12,0x34,(byte)0xff,(byte)0xfe});
        ByteBuffer b = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(48, wav.length); assertEquals(48000,b.getInt(24)); assertEquals(2,b.getShort(22));
        assertEquals(0x1234,b.getShort(44)); assertEquals(-2,b.getShort(46));
    }
    @Test public void padsFinalFrameAndStopSuppressesPlayback() {
        try (VoiceAudio audio = new VoiceAudio(w -> fail())) {
            audio.start(); audio.play(new byte[]{1,2,3,4});
            ByteBuffer frame = audio.provide20MsAudio(); assertEquals(3840,frame.remaining());
            assertEquals(1,frame.get()); frame.position(4); assertEquals(0,frame.get());
            assertFalse(audio.canProvide()); audio.stop(); audio.play(new byte[3840]); assertFalse(audio.canProvide());
        }
    }
    @Test public void finishesWithoutFurtherCallbacksAndIgnoresAudioWhileProcessing() throws Exception {
        CountDownLatch received = new CountDownLatch(1), release = new CountDownLatch(1);
        try (VoiceAudio audio = new VoiceAudio(w -> { received.countDown(); try { release.await(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); } })) {
            audio.start(); audio.accept(speechFrame());
            assertTrue(received.await(3,TimeUnit.SECONDS));
            assertEquals(VoiceAudio.State.PROCESSING,audio.getState()); assertFalse(audio.canReceiveCombined());
            audio.accept(speechFrame()); assertEquals(VoiceAudio.State.PROCESSING,audio.getState());
        } finally { release.countDown(); }
    }
    @Test public void silenceDoesNotRecordAndRecordingCapsAtFifteenSeconds() throws Exception {
        BlockingQueue<byte[]> result = new LinkedBlockingQueue<>();
        try (VoiceAudio audio = new VoiceAudio(result::add)) {
            audio.start(); audio.accept(new byte[3840]); assertEquals(VoiceAudio.State.LISTENING,audio.getState());
            for (int i=0;i<750;i++) audio.accept(speechFrame());
            byte[] wav = result.poll(3,TimeUnit.SECONDS); assertNotNull(wav); assertEquals(44+48000*4*15,wav.length);
        }
    }
    @Test public void daveNativesLoadOnThisPlatform() { moe.kyokobot.libdave.NativeDaveFactory.ensureAvailable(); }
    private static byte[] speechFrame() {
        byte[] data = new byte[3840]; for (int i=0;i<data.length;i+=2) {data[i]=0x10;data[i+1]=0;} return data;
    }
}
