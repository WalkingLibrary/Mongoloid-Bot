package com.jumbodinosaurs.mongoloidbot.brains;

import com.jumbodinosaurs.devlib.log.LogManager;
import net.dv8tion.jda.api.entities.Guild;

/** Applies the temporary voice nickname, or clears the server nickname. */
public final class VoiceNickname {
    private VoiceNickname() {}

    public static void update(Guild guild, boolean inVoice) {
        try {
            guild.getSelfMember().modifyNickname(inVoice ? "replacement_tonight_" : null).queue(
                    ignored -> LogManager.consoleLogger.info("[VOICE] Nickname " + (inVoice ? "set" : "cleared")),
                    failure -> LogManager.consoleLogger.warn("[VOICE] Nickname update failed: " + failure.getClass().getSimpleName()));
        } catch (RuntimeException failure) {
            LogManager.consoleLogger.warn("[VOICE] Nickname update failed: " + failure.getClass().getSimpleName());
        }
    }
}
