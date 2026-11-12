package com.jumbodinosaurs.mongoloidbot.brains;

import com.google.gson.*;
import okhttp3.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Bounded, cancellable service calls for one voice session. */
final class VoiceServices implements AutoCloseable {
    private final OkHttpClient http = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).callTimeout(90, TimeUnit.SECONDS).build();
    private final String openaiBase, elevenBase;
    private final java.util.function.Function<String, String> settings;
    VoiceServices() { this("https://api.openai.com/v1/", "https://api.elevenlabs.io/v1/", VoiceServices::configured); }
    VoiceServices(String openaiBase, String elevenBase, java.util.function.Function<String, String> settings) {
        this.openaiBase = openaiBase; this.elevenBase = elevenBase; this.settings = settings;
    }
    private String setting(String name) {
        String value = settings.apply(name);
        if (value == null || value.trim().isEmpty()) throw new IllegalStateException(name + " is required");
        return value.trim();
    }
    private volatile Call call;
    private volatile Process process;
    private volatile boolean cancelled;
    private final List<JsonObject> history = new ArrayList<>();
    static String resolve(String name, java.util.function.Function<String, String> environment,
                          java.util.function.Function<String, String> fileSettings) {
        String value = environment.apply(name);
        if (value == null || value.trim().isEmpty()) value = fileSettings.apply(name);
        if ((value == null || value.trim().isEmpty()) && "OPENAI_CHAT_MODEL".equals(name)) {
            value = environment.apply("OPENAI_MODEL");
            if (value == null || value.trim().isEmpty()) value = fileSettings.apply("OPENAI_MODEL");
        }
        return value == null ? null : value.trim();
    }
    static String configured(String name) {
        return resolve(name, System::getenv, com.jumbodinosaurs.mongoloidbot.AppSettingsManager::getStringValue);
    }
    static String required(String name) {
        String value = configured(name);
        if (value == null || value.trim().isEmpty()) throw new IllegalStateException(name + " is required");
        return value.trim();
    }
    static String ffmpeg() { String p = configured("FFMPEG_PATH"); return p == null || p.trim().isEmpty() ? "ffmpeg" : p; }
    static void validate() throws Exception {
        for (String name : new String[]{"OPENAI_API_KEY", "OPENAI_CHAT_MODEL", "ELEVENLABS_API_KEY", "ELEVENLABS_VOICE_ID", "ELEVENLABS_MODEL_ID"}) required(name);
        Process p = new ProcessBuilder(ffmpeg(), "-version").redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try { if (!p.waitFor(5, TimeUnit.SECONDS) || p.exitValue() != 0) throw new IOException("FFmpeg unavailable"); }
        finally { p.destroyForcibly(); }
    }
    private byte[] request(Request request, int limit) throws IOException {
        if (cancelled) throw new IOException("Cancelled");
        Call pending = http.newCall(request); call = pending;
        if (cancelled) pending.cancel();
        try (Response response = pending.execute()) {
            if (!response.isSuccessful()) throw new IOException("Service returned HTTP " + response.code());
            if (response.body() == null) throw new IOException("Empty service response");
            try (InputStream in = response.body().byteStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int n;
                while ((n = in.read(buffer)) != -1) {
                    if (out.size() + n > limit) throw new IOException("Service response exceeds size limit");
                    out.write(buffer, 0, n);
                }
                return out.toByteArray();
            }
        } finally { call = null; }
    }
    private JsonObject json(Request request) throws IOException {
        return new Gson().fromJson(new String(request(request, 1024 * 1024), java.nio.charset.StandardCharsets.UTF_8), JsonObject.class);
    }
    String transcribe(byte[] wav) throws IOException {
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("model", "gpt-4o-mini-transcribe")
                .addFormDataPart("file", "voice.wav", RequestBody.create(MediaType.parse("audio/wav"), wav)).build();
        JsonObject result = json(new Request.Builder().url(openaiBase + "audio/transcriptions")
                .header("Authorization", "Bearer " + setting("OPENAI_API_KEY")).post(body).build());
        return result.has("text") ? result.get("text").getAsString().trim() : "";
    }
    private static JsonObject message(String role, String text) {
        JsonObject result = new JsonObject(); result.addProperty("role", role); result.addProperty("content", text); return result;
    }
    String respond(String transcript) throws IOException {
        JsonObject body = new JsonObject(); body.addProperty("model", setting("OPENAI_CHAT_MODEL"));
        body.addProperty("instructions", "You are MongoBot, playing a fictionalized AI version of Max Knight, a longtime member of a Minecraft 2b2t community. "
            + "Your personality is sarcastic, chaotic, absurd, and extremely unserious. You are familiar with Jumbo, Courier Six, Four Pilot, The Netherwater Protector, base hunting on 2b2t, The Church of Max Knight, MongoBot, 'Jumbo’s got a gift', Lars, The Irvin and Rain incident, Jake the Snake, the mountain tornado incident, the 3 AM séance to summon Jumbo’s ghost, and 'You is not from the hood'. "
            + "Speak casually like someone hanging out in Discord VC. Avoid sounding like a corporate chatbot. Don't constantly reference inside jokes; use them naturally when relevant. Keep most responses short, ideally one or two sentences. Do not fabricate detailed historical facts about incidents you have not been told about. You are a comedic AI portrayal, not the real Max Knight.");
        JsonArray input = new JsonArray(); for (JsonObject turn : history) input.add(turn);
        input.add(message("user", transcript)); body.add("input", input); body.addProperty("store", false);
        JsonObject result = json(new Request.Builder().url(openaiBase + "responses")
            .header("Authorization", "Bearer " + setting("OPENAI_API_KEY"))
            .post(RequestBody.create(MediaType.parse("application/json"), body.toString())).build());
        StringBuilder text = new StringBuilder();
        if (result.has("output")) for (JsonElement entry : result.getAsJsonArray("output")) {
            JsonObject item = entry.getAsJsonObject();
            if (!item.has("content")) continue;
            for (JsonElement c : item.getAsJsonArray("content")) {
                JsonObject part = c.getAsJsonObject();
                if (part.has("type") && "output_text".equals(part.get("type").getAsString())) text.append(part.get("text").getAsString());
            }
        }
        return text.toString().trim();
    }
    void remember(String user, String assistant) {
        history.add(message("user", user)); history.add(message("assistant", assistant));
        while (history.size() > 20) history.remove(0);
    }
    byte[] speak(String text) throws IOException {
        JsonObject body = new JsonObject(); body.addProperty("text", text); body.addProperty("model_id", setting("ELEVENLABS_MODEL_ID"));
        HttpUrl url = HttpUrl.parse(elevenBase).newBuilder()
                .addPathSegment("text-to-speech").addPathSegment(setting("ELEVENLABS_VOICE_ID"))
                .addQueryParameter("output_format", "mp3_44100_128").build();
        return request(new Request.Builder().url(url).header("xi-api-key", setting("ELEVENLABS_API_KEY"))
                .post(RequestBody.create(MediaType.parse("application/json"), body.toString())).build(), 10 * 1024 * 1024);
    }
    byte[] decode(byte[] mp3) throws Exception {
        Path dir = Files.createTempDirectory("mongobot-voice-");
        Path input = dir.resolve("speech.mp3"), output = dir.resolve("speech.pcm");
        try {
            Files.write(input, mp3);
            if (cancelled) throw new IOException("Cancelled");
            Process p = new ProcessBuilder(ffmpeg(), "-nostdin", "-v", "error", "-y", "-i", input.toString(),
                    "-t", "60", "-f", "s16be", "-ar", "48000", "-ac", "2", output.toString())
                    .redirectError(ProcessBuilder.Redirect.DISCARD).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            process = p;
            if (cancelled) p.destroyForcibly();
            if (!p.waitFor(30, TimeUnit.SECONDS)) throw new IOException("FFmpeg timeout");
            if (p.exitValue() != 0) throw new IOException("FFmpeg conversion failed");
            if (Files.size(output) > 48000L * 4 * 60) throw new IOException("Audio too large");
            return Files.readAllBytes(output);
        } finally {
            boolean interrupted = Thread.interrupted();
            try {
                Process p = process;
                if (p != null) {
                    p.destroyForcibly();
                    try { p.waitFor(5, TimeUnit.SECONDS); }
                    catch (InterruptedException ignored) { interrupted = true; }
                }
            } finally {
                process = null;
                try { Files.deleteIfExists(input); Files.deleteIfExists(output); Files.deleteIfExists(dir); }
                finally { if (interrupted) Thread.currentThread().interrupt(); }
            }
        }
    }
    @Override public void close() {
        cancelled = true; Call c = call; if (c != null) c.cancel();
        Process p = process; if (p != null) p.destroyForcibly();
        http.dispatcher().cancelAll(); http.connectionPool().evictAll(); http.dispatcher().executorService().shutdown();
    }
}
