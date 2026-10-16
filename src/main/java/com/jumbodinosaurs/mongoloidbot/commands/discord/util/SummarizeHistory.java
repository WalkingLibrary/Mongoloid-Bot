package com.jumbodinosaurs.mongoloidbot.commands.discord.util;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.brains.OpenAIController;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Summarizes the most recent messages in the current Discord channel. */
public class SummarizeHistory extends CommandWithParameters implements IDiscordChatEventable
{
    private static final int MAX_MESSAGES = 250;
    private static final long COOLDOWN_MILLIS = 30L * 60L * 1000L;
    private static final Map<String, Long> LAST_REQUEST_BY_USER = new ConcurrentHashMap<>();
    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        if (getParameters() == null || getParameters().size() < 1)
        {
            throw new WaveringParametersException("Usage: ~SummarizeHistory amount (1-250)");
        }

        final int requestedAmount;
        try
        {
            requestedAmount = Integer.parseInt(getParameters().get(0).getParameter());
        }
        catch (NumberFormatException e)
        {
            return new MessageResponse("Please enter a whole number between 1 and 250.");
        }

        if (requestedAmount < 1)
        {
            return new MessageResponse("Please enter a whole number between 1 and 250.");
        }

        final int amount = Math.min(requestedAmount, MAX_MESSAGES);
        final String limitNotice = requestedAmount > MAX_MESSAGES
                ? "Only summarizing the most recent 250 messages.\n"
                : "";

        String userId = event.getAuthor().getId();
        long now = System.currentTimeMillis();
        synchronized (LAST_REQUEST_BY_USER)
        {
            Long lastRequest = LAST_REQUEST_BY_USER.get(userId);
            if (lastRequest != null)
            {
                long remainingMillis = COOLDOWN_MILLIS - (now - lastRequest);
                if (remainingMillis > 0)
                {
                    long remainingMinutes = (remainingMillis + 59999L) / 60000L;
                    return new MessageResponse("You can use SummarizeHistory again in "
                            + remainingMinutes + " minute(s).");
                }
            }

            LAST_REQUEST_BY_USER.put(userId, now);
        }

        // Include the command message in the fetch so that the requested number of
        // messages can still be collected after it is removed below. Discord/JDA
        // limits each history request to 100 messages, so retrieve in batches.
        retrieveHistory(event.getChannel().getHistory(), amount + 1, new ArrayList<>(), amount, limitNotice);

        return new MessageResponse(limitNotice + "Collecting the last " + amount + " messages and preparing a summary...");
    }

    private void retrieveHistory(MessageHistory history, int remaining, List<Message> collected,
                                  int amount, String limitNotice)
    {
        int batchSize = Math.min(100, remaining);
        history.retrievePast(batchSize).queue(
                messages ->
                {
                    collected.addAll(messages);
                    int stillNeeded = remaining - messages.size();
                    if (stillNeeded > 0 && !messages.isEmpty())
                    {
                        retrieveHistory(history, stillNeeded, collected, amount, limitNotice);
                    }
                    else
                    {
                        requestSummary(collected, amount, limitNotice);
                    }
                },
                failure -> event.getChannel().sendMessage("I couldn't read this channel's message history.").queue());
    }

    private void requestSummary(List<Message> fetchedMessages, int amount, String limitNotice)
    {
        List<Message> messages = new ArrayList<>();
        for (Message message : fetchedMessages)
        {
            if (!message.getId().equals(event.getMessage().getId()))
            {
                messages.add(message);
            }
        }

        // History is returned newest-first; reverse it so the prompt reads naturally.
        Collections.reverse(messages);
        if (messages.size() > amount)
        {
            messages = new ArrayList<>(messages.subList(messages.size() - amount, messages.size()));
        }

        if (messages.isEmpty())
        {
            event.getChannel().sendMessage("There are no messages available to summarize.").queue();
            return;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Summarize the following Discord conversation into exactly one concise paragraph. ")
                .append("Capture the main topics, decisions, and notable context. Do not use bullets or a title. ")
                .append("Each entry includes its timestamp and author.\n\n");

        for (Message message : messages)
        {
            prompt.append('[')
                    .append(message.getTimeCreated().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .append("] ")
                    .append(message.getAuthor().getName())
                    .append(": ")
                    .append(message.getContentDisplay());

            if (!message.getAttachments().isEmpty())
            {
                prompt.append(" [attachments: ");
                for (int attachmentIndex = 0; attachmentIndex < message.getAttachments().size(); attachmentIndex++)
                {
                    if (attachmentIndex > 0)
                    {
                        prompt.append(", ");
                    }
                    prompt.append(message.getAttachments().get(attachmentIndex).getFileName());
                }
                prompt.append(']');
            }
            prompt.append('\n');
        }

        final String notice = limitNotice;
        event.getChannel().sendTyping().queue();
        OpenAIController.generateResponse(prompt.toString(), summary -> sendSummary(notice, summary));
    }

    private void sendSummary(String limitNotice, String summary)
    {
        String response = limitNotice + summary;
        // Discord messages are limited to 2,000 characters. Keep the generated
        // summary as one paragraph while splitting only when the limit requires it.
        while (response.length() > 2000)
        {
            int splitAt = response.lastIndexOf(' ', 1990);
            if (splitAt < 1)
            {
                splitAt = 1990;
            }
            event.getChannel().sendMessage(response.substring(0, splitAt)).queue();
            response = response.substring(splitAt).trim();
        }
        event.getChannel().sendMessage(response).queue();
    }

    @Override
    public String getHelpMessage()
    {
        return "Summarizes recent channel messages into one paragraph. Usage: ~SummarizeHistory amount (maximum 250)";
    }

    @Override
    public GuildMessageReceivedEvent getGuildMessageReceivedEvent()
    {
        return event;
    }

    @Override
    public void setGuildMessageReceivedEvent(GuildMessageReceivedEvent event)
    {
        this.event = event;
    }
}
