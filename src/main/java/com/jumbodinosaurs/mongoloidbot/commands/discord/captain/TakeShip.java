package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.NPCUtil;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.battle.BattleTask;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TakeShip extends Command implements IDiscordChatEventable
{
    public static int intervalHoursForReTakeShip = (int) AppSettingsManager.getValue("intervalHoursForReTakeShip");
    public static int intervalHoursForMutiny = (int) AppSettingsManager.getValue("intervalHoursForMutiny");

    public static boolean isPirateWarActive = false;

    private GuildMessageReceivedEvent event;

    public static LocalDateTime GetNextRetakeDateTime(CaptainCandidate captainCandidate)
    {
        if(captainCandidate.isWasLastTakeOverAttemptMutiny())
        {
            return captainCandidate.getLastTakeOverAttempt().minusHours(-intervalHoursForMutiny);
        }
        return captainCandidate.getLastTakeOverAttempt().minusHours(-intervalHoursForReTakeShip);
    }

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        Member member = event.getMember();

        try
        {
            UserAccount currentUser = UserAccount.getUser(member);
            CaptainCandidate captainCandidate = currentUser.getCaptainCandidate(member);

            StringBuilder stringBuilder = new StringBuilder();

            if (!captainCandidate.isActiveCampaign())
            {
                stringBuilder.append("You are not currently an active captain candidate!\n");
                stringBuilder.append("Use **!candidateNow** to start your campaign.");
                return new MessageResponse(stringBuilder.toString());
            }

            if(captainCandidate.isCaptain())
            {
                stringBuilder.append("You are already the Captain!");
                return new MessageResponse(stringBuilder.toString());
            }

            LocalDateTime retakeTime = GetNextRetakeDateTime(captainCandidate);
            if (retakeTime.isAfter(LocalDateTime.now()))
            {
                stringBuilder.append("⏰ You cannot attempt to take the ship yet.\n");
                stringBuilder.append("You may try again **at " + retakeTime.toString() + "**.");
                return new MessageResponse(stringBuilder.toString());
            }

            stringBuilder.append("⚓ **" + member.getEffectiveName() + " attempts to take the ship!** ⚓\n");

            /*
             * Battle Process
             * 1. Get all the current Captain followers
             * 2. Get all the Challenger's Followers
             * 3. Create a Battle Report of each and all until on side is all that remains
             *  */

            ArrayList<Player> defendingPlayers;
            ArrayList<Player> attackingPlayers;

            // Get the current captain candidate that isCaptain()
            CaptainCandidate defenderCandidate = CaptainCandidate.getAllCaptainCandidates()
                    .stream()
                    .filter(CaptainCandidate::isCaptain)
                    .findFirst()
                    .orElse(null);

            if (defenderCandidate == null)
            {
                // No current captain — use NPCs as defenders
                defendingPlayers = Player.getAllNPCPlayers();
            }
            else
            {
                ArrayList<String> defenderSupporters = new ArrayList<>();
                for (String id : defenderCandidate.getSupportersLongIds())
                {
                    defenderSupporters.add(Base64.getEncoder().encodeToString(id.toString().getBytes()));
                }
                defenderSupporters.add(defenderCandidate.getUserAccountId());

                defendingPlayers = Player.getPlayersByUserAccountIds(defenderSupporters);
            }

            ArrayList<String> attackerSupporters = new ArrayList<>();
            for (String id : captainCandidate.getSupportersLongIds())
            {
                attackerSupporters.add(Base64.getEncoder().encodeToString(id.toString().getBytes()));
            }

            attackingPlayers = Player.getPlayersByUserAccountIds(attackerSupporters);
            attackingPlayers.add(currentUser.getPlayer(event.getMember()));



            boolean isDefenderWin = true;
            Player currentDefender = defendingPlayers.remove(0);
            Player currentAttacker = attackingPlayers.remove(0);
            Player winningPlayer = null;
            StringBuilder reportBuilder = new StringBuilder();
            while(currentDefender != null && currentAttacker != null)
            {
                winningPlayer = BattleTask.getBattleWinner(currentDefender, currentAttacker, reportBuilder);

                if(winningPlayer.getPromptName().equals(currentDefender.getPromptName()))
                {
                    //Defenders Win
                    if(attackingPlayers.size() <= 0 )
                    {
                        currentAttacker = null;
                        continue;
                    }
                    currentAttacker = attackingPlayers.remove(0);
                    continue;
                }
                //Defenders LOSE
                if(defendingPlayers.size() <= 0 )
                {
                    isDefenderWin = false;
                    currentDefender = null;
                    continue;
                }
                currentDefender = defendingPlayers.remove(0);
            }



            if(winningPlayer.isNPC())
            {
                //Send Taunt and Battle Report Haha
                NPCUtil.SendNPCChatMessage("captainChannel", "Another victory!", winningPlayer);
                BattleTask.SendBattleReport(reportBuilder, event.getChannel().getId());
                stringBuilder.append(winningPlayer.getPromptName() + " helps hold the Line for the Current Captain!" );
                captainCandidate.setLastTakeOverAttempt(LocalDateTime.now());
                UserAccount.updateCaptainCandidate(captainCandidate);
                return new MessageResponse(stringBuilder.toString());
            }

            TakeShip.isPirateWarActive = false;

            if(isDefenderWin)
            {
                stringBuilder.append(winningPlayer.getPromptName() + " helps hold the Line for the Current Captain!" );
                BattleTask.SendBattleReport(reportBuilder, event.getChannel().getId());
                captainCandidate.setLastTakeOverAttempt(LocalDateTime.now());
                UserAccount.updateCaptainCandidate(captainCandidate);
                return new MessageResponse(stringBuilder.toString());
            }


            //Update Captian Rank and Captain Candidates

            stringBuilder.append(currentUser.getPlayer(event.getMember()).getPromptName() + " Takes the Ship by Force!!!" );
            BattleTask.SendBattleReport(reportBuilder, event.getChannel().getId());

            //Swap Captain Rank
            // 3. Remove Rank and Captain Flag From everyone else
            if (isCurrentCaptainaNPC())
            {
                if ( currentDefender.getInventory() != null &&
                        currentDefender.getInventory().getItems() != null &&
                        !currentDefender.getInventory().getItems().isEmpty() &&
                        currentDefender.getInventory().getItems().get(0) != null) {

                    currentAttacker.setPendingItem(currentDefender.getInventory().getItems().get(0));
                }
            }


            Role captainRole = event.getGuild().getRoleById(CaptainNow.captainID);
            ArrayList<CaptainCandidate> allCandidates = CaptainCandidate.getAllCaptainCandidates();
            for (CaptainCandidate candidate : allCandidates)
            {
                if (candidate.isCaptain())
                {
                    candidate.setCaptain(false);
                    UserAccount.updateCaptainCandidate(candidate);
                }
            }
            for (Member guildMember : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getMembers())
            {
                if (guildMember.getRoles().contains(captainRole))
                {
                    event.getGuild().removeRoleFromMember(guildMember, captainRole).complete();
                }
            }
            captainCandidate.setLastTakeOverAttempt(LocalDateTime.now());
            captainCandidate.setCaptain(true);
            UserAccount.updateCaptainCandidate(captainCandidate);


            // 4. Add Captain role to this user
            event.getGuild().addRoleToMember(member, captainRole).complete();
            return new MessageResponse(stringBuilder.toString());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error accessing the database while processing your takeover attempt.");
        }
        catch (UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error fetching your account data.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Running Task");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Running Task");
        }
    }

    public static boolean isCurrentCaptainaNPC() throws SQLException
    {
        ArrayList<CaptainCandidate> allCandidates = CaptainCandidate.getAllCaptainCandidates();
        for (CaptainCandidate candidate : allCandidates)
        {
            if (candidate.isCaptain())
            {
               return true;
            }
        }
        return false;
    }


    @Override
    public String getHelpMessage()
    {
        return "Attempts to take control of the ship if your campaign and timing allow it.";
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
