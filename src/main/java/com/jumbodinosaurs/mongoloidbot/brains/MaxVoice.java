package com.jumbodinosaurs.mongoloidbot.brains;

import com.jumbodinosaurs.devlib.log.LogManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

/** Owns the bot's single conversation session. */
public final class MaxVoice {
    private static Guild guild;
    private static AudioChannel channel;
    private static VoiceAudio audio;
    private static VoiceServices services;
    private static final String NOTICE = "MongoBot AI voice chat: combined channel audio is sent to OpenAI for transcription and responses; generated response text is sent to ElevenLabs for synthetic speech. MongoBot is a fictional AI portrayal, not the real Max Knight. Listening starts only with ~Max start and stops with ~Max stop.";
    static { Runtime.getRuntime().addShutdownHook(new Thread(() -> { synchronized (MaxVoice.class) { cleanup(); } }, "max-voice-shutdown")); }
    private MaxVoice() {}
    public static synchronized boolean hasSession() { return guild != null; }
    public static synchronized String command(String action, MessageReceivedEvent event) throws Exception {
        if (guild != null && !guild.equals(event.getGuild())) return "An AI voice session already exists in another server.";
        switch (action.toLowerCase(java.util.Locale.ROOT)) {
            case "join":
                if (event.getMember() == null || event.getMember().getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null)
                    return "Join a voice channel first.";
                if (guild != null) return "Already joined. Use ~Max leave before switching channels.";
                AudioChannel target = event.getMember().getVoiceState().getChannel();
                if (!(target instanceof net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel)) return "Please use a regular voice channel.";
                if (!event.getGuild().getSelfMember().hasPermission(target, net.dv8tion.jda.api.Permission.VOICE_CONNECT, net.dv8tion.jda.api.Permission.VOICE_SPEAK)) return "I need Connect and Speak permissions in that voice channel.";
                target.getGuild().getVoiceChannelById(target.getId()).sendMessage(NOTICE).complete();
                guild = event.getGuild(); channel = target;
                guild.getAudioManager().setSelfDeafened(false);
                guild.getAudioManager().setSelfMuted(false);
                try { guild.getAudioManager().openAudioConnection(target); }
                catch (RuntimeException failure) { guild = null; channel = null; throw failure; }
                return "Connecting to " + target.getName() + ". Use ~Max start to enable AI conversation.";
            case "start":
                if (guild == null || !guild.getAudioManager().isConnected()) return "Use ~Max join and wait for the voice connection first.";
                if (audio != null) return "AI conversation is already active.";
                VoiceServices.validate();
                guild.getVoiceChannelById(channel.getId()).sendMessage(NOTICE).complete();
                final VoiceServices client = new VoiceServices();
                services = client;
                final VoiceAudio[] holder = new VoiceAudio[1];
                VoiceAudio handler = new VoiceAudio(wav -> process(client, holder[0], wav)); holder[0] = handler;
                audio = handler;
                try { handler.attach(guild.getAudioManager()); }
                catch (RuntimeException failure) { cleanup(); throw failure; }
                return "AI voice conversation started. " + NOTICE;
            case "stop": cleanup(); return "AI voice conversation stopped.";
            case "leave":
                cleanup();
                if (guild != null) { Guild previous = guild; guild = null; channel = null; previous.getAudioManager().setAutoReconnect(false); previous.getAudioManager().closeAudioConnection(); }
                return "Disconnected from voice.";
            default: return "Usage: ~Max join|leave|start|stop";
        }
    }
    private static void process(VoiceServices client, VoiceAudio handler, byte[] wav) {
        try {
            long start = System.nanoTime(); String transcript = client.transcribe(wav);
            log("[OPENAI] Transcription completed", start);
            if (transcript.isEmpty()) return;
            start = System.nanoTime(); String response = client.respond(transcript); log("[OPENAI] Response generated", start);
            if (response.isEmpty()) return;
            start = System.nanoTime(); byte[] mp3 = client.speak(response); log("[ELEVENLABS] Audio generated", start);
            byte[] pcm = client.decode(mp3);
            synchronized (MaxVoice.class) {
                if (services != client || audio != handler) return;
                client.remember(transcript, response); handler.play(pcm);
            }
        } catch (Exception failure) {
            synchronized (MaxVoice.class) {
                if (services == client) LogManager.consoleLogger.error("[VOICE] Turn failed: " + failure.getClass().getSimpleName() + ": " + failure.getMessage());
            }
        }
    }
    private static void log(String message, long start) { LogManager.consoleLogger.info(message + " in " + TimeUnitMillis(start) + " ms"); }
    private static long TimeUnitMillis(long start) { return (System.nanoTime() - start) / 1000000; }
    private static void cleanup() {
        if (audio != null) audio.close(); audio = null;
        if (services != null) services.close(); services = null;
        if (guild != null) { guild.getAudioManager().setReceivingHandler(null); guild.getAudioManager().setSendingHandler(null); }
    }
    public static synchronized void connected(GuildVoiceUpdateEvent event) {
        if (guild != null && guild.equals(event.getGuild()) && !channel.equals(event.getChannelJoined())) {
            // An external move invalidates disclosure and the session's channel ownership.
            cleanup(); Guild previous = guild; guild = null; channel = null;
            previous.getAudioManager().closeAudioConnection();
        }
    }
    public static synchronized void disconnected(Guild disconnected) {
        if (guild != null && guild.equals(disconnected)) { cleanup(); guild = null; channel = null; }
    }
    public static synchronized void arrived(GuildVoiceUpdateEvent event) {
        if (guild != null && guild.equals(event.getGuild()) && channel.equals(event.getChannelJoined())) {
            guild.getVoiceChannelById(channel.getId()).sendMessage(event.getMember().getAsMention() + " " + NOTICE).queue();
        }
    }
}
