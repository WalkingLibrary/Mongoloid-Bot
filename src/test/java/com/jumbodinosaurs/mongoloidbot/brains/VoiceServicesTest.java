package com.jumbodinosaurs.mongoloidbot.brains;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import static org.junit.Assert.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
public class VoiceServicesTest {
    @Test public void configurationUsesEnvironmentThenFile() {
        assertEquals("env-key",VoiceServices.resolve("OPENAI_API_KEY", n -> " env-key ", n -> "file-key"));
        assertEquals("file-key",VoiceServices.resolve("OPENAI_API_KEY", n -> " ", n -> " file-key "));
        assertEquals("legacy-model",VoiceServices.resolve("OPENAI_CHAT_MODEL", n -> null,
            n -> "OPENAI_MODEL".equals(n) ? "legacy-model" : null));
        assertEquals("voice-model",VoiceServices.resolve("OPENAI_CHAT_MODEL",
            n -> "OPENAI_MODEL".equals(n) ? "legacy-model" : null,
            n -> "OPENAI_CHAT_MODEL".equals(n) ? "voice-model" : null));
        assertNull(VoiceServices.resolve("ELEVENLABS_API_KEY", n -> null, n -> null));
    }
    @Test public void multipartResponsesAndSpeechContracts() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        AtomicReference<String> path = new AtomicReference<>(), body = new AtomicReference<>();
        AtomicReference<String> reply = new AtomicReference<>("{\"text\":\"hello\"}");
        server.createContext("/", e -> {
            path.set(e.getRequestURI().toString()); body.set(new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            byte[] bytes=reply.get().getBytes(StandardCharsets.UTF_8); e.sendResponseHeaders(200,bytes.length);
            try (java.io.OutputStream out=e.getResponseBody()) {out.write(bytes);}
        }); server.start();
        String base="http://127.0.0.1:"+server.getAddress().getPort()+"/";
        try (VoiceServices client = new VoiceServices(base,base,name -> "test-setting")) {
            assertEquals("hello",client.transcribe(VoiceAudio.wav(new byte[4])));
            assertTrue(body.get().contains("gpt-4o-mini-transcribe")); assertTrue(body.get().contains("voice.wav"));
            reply.set("{\"output\":[{\"type\":\"reasoning\"},{\"content\":[{\"type\":\"output_text\",\"text\":\"Hey.\"}]}]}");
            assertEquals("Hey.",client.respond("hello")); assertTrue(body.get().contains("fictionalized"));
            client.remember("hello","Hey."); client.respond("again"); assertTrue(body.get().contains("Hey."));
            reply.set("mp3-bytes"); assertArrayEquals("mp3-bytes".getBytes(StandardCharsets.UTF_8),client.speak("Hey."));
            assertTrue(path.get().contains("text-to-speech/test-setting?output_format=mp3_44100_128"));
            assertTrue(body.get().contains("model_id"));
            client.close();
            try { client.transcribe(new byte[4]); fail("Expected cancellation"); } catch (java.io.IOException expected) {}
        } finally {server.stop(0);}
    }
    @Test public void ffmpegDecodesSyntheticMp3ToBigEndianPcm() throws Exception {
        org.junit.Assume.assumeTrue(System.getenv("FFMPEG_PATH") != null);
        java.nio.file.Path input = java.nio.file.Files.createTempFile("voice-fixture-", ".mp3");
        try (VoiceServices client = new VoiceServices()) {
            Process p = new ProcessBuilder(VoiceServices.ffmpeg(), "-nostdin", "-v", "error", "-y", "-f", "lavfi", "-i",
                "sine=frequency=440:duration=0.2", input.toString()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            try { assertTrue(p.waitFor(15,java.util.concurrent.TimeUnit.SECONDS)); assertEquals(0,p.exitValue()); }
            finally {p.destroyForcibly();}
            byte[] pcm = client.decode(java.nio.file.Files.readAllBytes(input));
            assertEquals(0,pcm.length % 4); assertTrue(pcm.length >= 48000*4/10);
            java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(pcm).order(java.nio.ByteOrder.BIG_ENDIAN);
            long delta=0; int previous=0;
            while (b.remaining() >= 4) {int sample=b.getShort(); b.getShort(); delta+=Math.abs(sample-previous);previous=sample;}
            assertTrue("Nonempty audio",delta>1000);
            assertTrue("Smooth 440Hz waveform in big-endian order",delta/(pcm.length/4)<2000);
        } finally {java.nio.file.Files.deleteIfExists(input);}
    }
    @Test public void httpErrorsAreReportedWithoutResponseBody() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",e -> { e.sendResponseHeaders(429,-1); e.close(); });server.start();
        String base="http://127.0.0.1:"+server.getAddress().getPort()+"/";
        try (VoiceServices client=new VoiceServices(base,base,n -> "test")) {
            try {client.transcribe(new byte[4]);fail();} catch(java.io.IOException expected) {assertEquals("Service returned HTTP 429",expected.getMessage());}
        } finally {server.stop(0);}
    }
}
