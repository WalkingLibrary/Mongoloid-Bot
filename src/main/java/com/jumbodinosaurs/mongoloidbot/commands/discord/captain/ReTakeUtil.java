package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;

import java.time.LocalDateTime;

public class ReTakeUtil
{

    public static int intervalHoursForReTakeShip = (int) AppSettingsManager.getValue("intervalHoursForReTakeShip");
    public static int intervalHoursForMutiny = (int) AppSettingsManager.getValue("intervalHoursForMutiny");

    public static LocalDateTime GetNextRetakeDateTime(CaptainCandidate captainCandidate)
    {
        if(captainCandidate.isWasLastTakeOverAttemptMutiny())
        {
            return captainCandidate.getLastTakeOverAttempt().minusHours(-intervalHoursForMutiny);
        }
        return captainCandidate.getLastTakeOverAttempt().minusHours(-intervalHoursForReTakeShip);
    }
}
