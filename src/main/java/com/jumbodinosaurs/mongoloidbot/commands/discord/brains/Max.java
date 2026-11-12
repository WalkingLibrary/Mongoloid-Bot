package com.jumbodinosaurs.mongoloidbot.commands.discord.brains;
import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.brains.MaxVoice;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
public class Max extends CommandWithParameters implements IDiscordChatEventable, IAdminCommand {
    private MessageReceivedEvent event;
    @Override public MessageResponse getExecutedMessage() {
        if (getParameters() == null || getParameters().size() != 1) return new MessageResponse(getHelpMessage());
        try { return new MessageResponse(MaxVoice.command(getParameters().get(0).getParameter(), event)); }
        catch (Exception failure) {
            String detail = failure.getClass().getSimpleName();
            // Only expose known configuration names, never arbitrary upstream exception text.
            if (failure instanceof IllegalStateException && failure.getMessage() != null
                    && failure.getMessage().matches("(?:OPENAI_API_KEY|OPENAI_CHAT_MODEL|ELEVENLABS_API_KEY|ELEVENLABS_VOICE_ID|ELEVENLABS_MODEL_ID) is required")) {
                detail = failure.getMessage() + " in the bot environment or active appsettings JSON file. Set it and restart MongoBot";
            }
            com.jumbodinosaurs.devlib.log.LogManager.consoleLogger.warn("[VOICE] Command failed: " + detail);
            return new MessageResponse("Voice command failed: " + detail + ". Check configuration, FFmpeg, and Discord permissions.");
        }
    }
    @Override public String getHelpMessage() { return "Usage: ~Max join|leave|start|stop (administrator only)"; }
    @Override public MessageReceivedEvent getMessageReceivedEvent() { return event; }
    @Override public void setMessageReceivedEvent(MessageReceivedEvent event) { this.event = event; }
}
