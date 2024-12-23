package com.jumbodinosaurs.mongoloidbot.eventHandlers;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.CommandManager;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsController;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IOwnerCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.ToggleJoinVoiceChats;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.util.Random;

public class EventListener extends ListenerAdapter
{
    private static final String allowedChannelName = "mongoloid-bot";


    public static AudioManager joinedVC;

    public static void sendMessage(String message)
    {
        sendMessage(message, Main.BOT_CHANNEL_ID);
    }

    public static void sendMessage(String message, String channelId)
    {
        Main.jdaController.getJda()
                .getGuildById(Main.GUILD_ID)
                .getTextChannelById(channelId)
                .sendMessage(message)
                .complete();
    }



    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        String message = event.getMessage().getContentRaw();

        if (message == null || message.length() <= 0)
        {
            return;
        }
        LogManager.consoleLogger.info("Message: " + message);
        Command command;
        try
        {
            command = CommandManager.filterCommand(event.getMessage().getContentRaw(), true);
        }
        catch (WaveringParametersException e)
        {
            event.getChannel().sendMessage(e.getMessage()).complete();
            return;
        }

        if (command == null)
        {
            //Brains
            //Don't Respond to Self
            if (event.getAuthor().isBot())
            {
                LogManager.consoleLogger.info("Brain Task skipped: Not Responding to Self ");
                return;
            }

            //Global Setting - Should respond at all?
            if (!BrainsController.brainsOptions.isShouldRespond())
            {
                LogManager.consoleLogger.info(
                        "Brain Task skipped: Should Respond: " + BrainsController.brainsOptions.isShouldRespond());
                return;
            }

            boolean botIsMentioned = false;
            for (Member mentionMember :
                    event.getMessage().getMentionedMembers())
            {
                if (mentionMember.getUser().isBot() && mentionMember.getUser().equals(event.getJDA().getSelfUser()))
                {
                    LogManager.consoleLogger.info("Bot was Mentioned");
                    botIsMentioned = true;
                }
            }
            //If the bot was not mentioned and the Reference Message(Replay) is null, Generate chance to Interject
            if (!botIsMentioned && event.getMessage().getReferencedMessage() == null)
            {
                //Generate Random Chance To Respond
                Random random = new Random();
                // Generate a random number between 0 and 1
                double chance = random.nextDouble();

                // If the chance is greater than 0.20 (20%), return early
                if (chance > 0.02)
                {
                    LogManager.consoleLogger.info("Brain Task skipped: Chance was " + (chance * 100) + "%");
                    return;
                }

                if (!event.getChannel().getId().equals(BrainsController.brainsOptions.getChannelIdToRespondIn()))
                {
                    LogManager.consoleLogger.info("Brain Task Random Interject Skipped: Not The Correct Channel");
                    return;
                }
            }


            if (!botIsMentioned && !event.getMessage()
                    .getReferencedMessage()
                    .getAuthor()
                    .isBot())
            {
                LogManager.consoleLogger.info("Brain Task skipped: Not a Reply To Bot Reply of Self");
                return;
            }


            LogManager.consoleLogger.info("Responding Too: " + message);
            BrainsController.respond(event, message);
            return;
        }

        if (event.getChannel().getName().equals(allowedChannelName))
        {
            LogManager.consoleLogger.info("Command: " + message);
            if (command instanceof IDiscordChatEventable)
            {
                ((IDiscordChatEventable) command).setGuildMessageReceivedEvent(event);
            }

            if (event.getMember() == null)
                {
                    return;
                }

                if (command instanceof IAdminCommand)
                {
                    if (!event.getMember().hasPermission(Permission.ADMINISTRATOR))
                    {
                        event.getChannel()
                                .sendMessage("You Don't Have the needed Permissions for that Command")
                                .complete();
                        return;
                    }
                }


                if (command instanceof IOwnerCommand)
                {
                    if (!event.getMember().getId().equals("230481636565843969"))
                    {
                        event.getChannel()
                                .sendMessage("You Don't Have the needed Permissions for that Command")
                                .complete();
                        return;
                    }
                }


            MessageResponse response = null;
            try
            {
                response = command.getExecutedMessage();
            }
            catch (WaveringParametersException e)
            {
                event.getChannel().sendMessage(e.getMessage()).complete();
                return;
            }


            if (response != null)
            {
                event.getChannel().sendMessage(response.getMessage()).complete();
            }

            //check file size
            if (response.getAttachments() != null)
            {
                    for (File file : response.getAttachments())
                    {
                        if (file.length() < 8000000)
                        {
                            event.getChannel().sendFile(file).complete();
                        }
                        else
                        {
                            event.getChannel()
                                 .sendMessage(" File: " + file.getName() + " was to large To Send.")
                                 .complete();
                        }
                    }
                }


        }

    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event)
    {
        try {
            VoiceChannel voiceChannelJoined = event.getChannelJoined();
            LogManager.consoleLogger.info("Voice Channel Joined: " + voiceChannelJoined.getName());

            Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
            event.getGuild().addRoleToMember(event.getMember(), ashTrayRole).complete();
            LogManager.consoleLogger.info("Non Bot User Joined");

            if(ToggleJoinVoiceChats.joinVoiceChats && (joinedVC == null || !joinedVC.isConnected()))
            {
                LogManager.consoleLogger.info("Joining Voice Chat: " + voiceChannelJoined.getName());
                joinedVC = event.getGuild().getAudioManager();
                joinedVC.setAutoReconnect(true);
                joinedVC.setConnectTimeout(1000000);
                joinedVC.openAudioConnection(voiceChannelJoined);

            }
            super.onGuildVoiceJoin(event);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event)
    {
        VoiceChannel voiceChannelLeft = event.getChannelLeft();
        LogManager.consoleLogger.info("Voice Channel Left: " + voiceChannelLeft.getName());

        Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
        event.getGuild().removeRoleFromMember(event.getMember(), ashTrayRole).complete();

        if(joinedVC != null && joinedVC.isConnected())
        {
            if(voiceChannelLeft.getMembers().size() <= 1)
            {
                joinedVC.setAutoReconnect(false);
                joinedVC.closeAudioConnection();
            }
        }

        super.onGuildVoiceLeave(event);
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event)
    {
        VoiceChannel voiceChannelJoined = event.getChannelJoined();
        VoiceChannel voiceChannelLeft = event.getChannelLeft();
        LogManager.consoleLogger.info(
                "User Moved from " + voiceChannelLeft.getName() + " to " + voiceChannelJoined.getName());

        Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
        event.getGuild().addRoleToMember(event.getMember(), ashTrayRole).complete();

        super.onGuildVoiceMove(event);
    }
    
}
