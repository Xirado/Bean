package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.Command;
import at.xirado.bean.objects.MEE6Player;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;

public class TransferFromMee6 extends Command
{
    public TransferFromMee6()
    {
        super("transferfrommee6", "Transfers the MEE6 leaderboard to Bean", "transferfrommee6");
        setCommandCategory(CommandCategory.ADMIN);
        setRequiredPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        if(args.length != 1 || !args[0].equals("confirm"))
        {
            context.replyWarning("This transfers all XP from MEE6 over to Bean (top 100 users).\n" +
                    " (MEE6 levels will not be reset). This action cannot be undone.\n" +
                    "__**Existing xp will be reset for all found users**__\n" +
                    "Are you sure you want to continue?\n" +
                    "Run `"+ Bean.getInstance().prefixManager.getPrefix(event.getGuild().getIdLong())+"transferfrommee6 confirm` to continue.");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = null;
        try
        {
            node = objectMapper.readTree(new URL("https://mee6.xyz/api/plugins/levels/leaderboard/"+event.getGuild().getIdLong()));
            if(node == null)
            {
                context.replyWarning("Could not find any data for this guild! (Can you access the leaderboard?)");
                return;
            }
            MEE6Player[] players = objectMapper.treeToValue(node.at("/players"), MEE6Player[].class);
            if(players == null || players.length == 0)
            {
                context.replyWarning("Could not find any data for this guild! (Can you access the leaderboard?)");
                return;
            }
            Connection connection = Database.getConnectionFromPool();
            if(connection == null)
            {
                context.replyError("An error occured!");
                return;
            }
            long guildID = event.getGuild().getIdLong();
            for(MEE6Player player : players)
            {
                long id = Long.parseLong(player.getId());
                long xp = player.getXp();
                RankingSystem.setXP(connection, guildID, id, xp);
            }
            Util.closeQuietly(connection);
            context.replySuccess("XP has been transferred! ("+players.length+" users)");
        } catch (Exception e)
        {
            if(e instanceof FileNotFoundException)
            {
                context.replyWarning("Could not find any data for this guild! (Can you access the leaderboard?)");
                return;
            }
            e.printStackTrace();
            context.replyError("An error occured!");
        }
    }
}
