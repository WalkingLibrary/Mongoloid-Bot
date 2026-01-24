package com.jumbodinosaurs.mongoloidbot.tasks.captain;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ItemDropManager
{

    private static final ConcurrentMap<String, ActiveDrop> dropsByMessageId = new ConcurrentHashMap<>();

    public static void register(ActiveDrop drop)
    {
        dropsByMessageId.put(drop.messageId, drop);
    }

    public static ActiveDrop get(String messageId)
    {
        return dropsByMessageId.get(messageId);
    }

    public static void remove(String messageId)
    {
        dropsByMessageId.remove(messageId);
    }

}
