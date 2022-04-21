package com.jumbodinosaurs.mongoloidbot.eventHandlers;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.CommandManager;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IOwnerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;

public class EventListener extends ListenerAdapter
{
    private static final String allowedChannelName = "mongoloid-bot";
    
    public static void sendMessage(String message)
    {
        Main.jdaController.getJda()
                .getGuildById(Main.GUILD_ID)
                .getTextChannelById(Main.BOT_CHANNEL_ID)
                          .sendMessage(message)
                          .complete();
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        if (event.getChannel().getName().equals(allowedChannelName))
        {
            try
            {
                String message = event.getMessage().getContentRaw();
                System.out.println("Command: " + message);

                if (message == null || message.length() <= 0)
                {
                    return;
                }

                Command command = CommandManager.filterCommand(event.getMessage().getContentRaw(), true);

                if (command == null)
                {
                    return;
                }

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


                MessageResponse response = command.getExecutedMessage();


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
            catch(WaveringParametersException e)
            {
                event.getChannel().sendMessage(e.getMessage()).complete();
            }
        }
    }


    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event)
    {
        VoiceChannel voiceChannelJoined = event.getChannelJoined();
        System.out.println("Voice Channel Joined: " + voiceChannelJoined.getName());

        Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
        event.getGuild().addRoleToMember(event.getMember(), ashTrayRole).complete();

        super.onGuildVoiceJoin(event);
    }


    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event)
    {
        VoiceChannel voiceChannelLeft = event.getChannelLeft();
        System.out.println("Voice Channel Left: " + voiceChannelLeft.getName());

        Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
        event.getGuild().removeRoleFromMember(event.getMember(), ashTrayRole).complete();

        super.onGuildVoiceLeave(event);
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event)
    {
        VoiceChannel voiceChannelJoined = event.getChannelJoined();
        VoiceChannel voiceChannelLeft = event.getChannelLeft();
        System.out.println("User Moved from " + voiceChannelLeft.getName() + " to " + voiceChannelJoined.getName());

        Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
        event.getGuild().addRoleToMember(event.getMember(), ashTrayRole).complete();

        super.onGuildVoiceMove(event);
    }
    
}
