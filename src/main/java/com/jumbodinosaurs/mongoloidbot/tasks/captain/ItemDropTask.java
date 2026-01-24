package com.jumbodinosaurs.mongoloidbot.tasks.captain;

import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Ability;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.ItemUntil;
import com.jumbodinosaurs.mongoloidbot.models.DiscordANSITextHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ItemDropTask extends ScheduledTask
{

    private static final Random RNG = new Random();

    // tune these
    private static final int minutes = 45;          // check every 60s
    private static final double DROP_CHANCE = 0.05;        // 5% chance per check

    public ItemDropTask(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }

    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.MINUTES;
    }

    @Override
    public int getPeriod()
    {
        return minutes;
    }

    @Override
    public void run()
    {
        try
        {
            double roll = RNG.nextDouble();
            if (roll >= DROP_CHANCE)
            {
                return;
            }

            JDA jda = Main.jdaController.getJda();
            Guild guild = jda.getGuildById(Main.GUILD_ID);
            if (guild == null)
            {
                return;
            }

            TextChannel channel = guild.getTextChannelById(Main.BOT_CHANNEL_ID);
            if (channel == null)
            {
                return;
            }

            Item item = ItemUntil.generateRandomItem();

            Random r = new Random();
            int newIntensity = 200 + r.nextInt(2000 - 200 + 1); // 200..2000 inclusive
            item.setAbility(new Ability(item.getAbility().getType(), newIntensity));

            StringBuilder message = new StringBuilder();
            message.append("**ITEM DROP!**\n");
            message.append(DiscordANSITextHelper.ansiOpen);
            message.append(item.toInventoryDisplay());
            message.append(DiscordANSITextHelper.ansiClose);
            message.append("React with **any emoji** to claim it (first reaction wins).");

            Message sent = channel.sendMessage(message).complete();

            ActiveDrop drop = new ActiveDrop(
                    sent.getId(),
                    channel.getId(),
                    guild.getId(),
                    item
            );

            ItemDropManager.register(drop);

        }
        catch (Exception e)
        {
            LogManager.consoleLogger.error(e.getMessage());
        }
    }
}
