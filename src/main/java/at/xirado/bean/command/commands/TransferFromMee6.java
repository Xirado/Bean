package at.xirado.bean.command.commands;

import at.xirado.bean.command.Command;
import at.xirado.bean.command.CommandCategory;
import at.xirado.bean.command.CommandContext;
import at.xirado.bean.data.MEE6Player;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.time.Instant;

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
        if (args.length != 1 || !args[0].equals("confirm"))
        {
            String prefix = context.getGuildData().getPrefix();
            context.replyWarning("This transfers all XP from MEE6 over to Bean.\n" +
                    " (MEE6 levels will not be reset). This action cannot be undone.\n" +
                    "__**Existing xp will be reset for all found users**__\n" +
                    "Are you sure you want to continue?\n" +
                    "Run `" + prefix + "transferfrommee6 confirm` to continue.");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        int page = 0;
        int transferredPlayers = 0;
        long guildID = event.getGuild().getIdLong();
        Message message = event.getChannel().sendMessage("<a:loading:848251224330469419>  Transferring... Please wait.").complete();
        long startTime = System.currentTimeMillis();
        try(var connection = Database.getConnectionFromPool())
        {
            if (connection == null)
            {
                context.replyError(context.getLocalized("general.db_error"));
                return;
            }
            while (true)
            {
                if (System.currentTimeMillis() > startTime + 30000)
                {
                    message.editMessage("Request timed out!").queue();
                    connection.close();
                    return;
                }
                JsonNode node = objectMapper.readTree(new URL("https://mee6.xyz/api/plugins/levels/leaderboard/" + guildID + "?page=" + page));
                MEE6Player[] players = objectMapper.treeToValue(node.at("/players"), MEE6Player[].class);
                if (players == null || players.length == 0)
                {
                    break;
                }
                for (MEE6Player player : players)
                {
                    long id = Long.parseLong(player.getId());
                    long xp = player.getXp();
                    String name = player.getUsername();
                    String discriminator = player.getDiscriminator();
                    RankingSystem.setXP(connection, guildID, id, xp, name, discriminator);
                    transferredPlayers++;
                }
                page++;
            }
        } catch (Exception ex)
        {
            if (ex instanceof FileNotFoundException)
            {
                message.editMessage("Could not find any data for this guild! (Can you access the leaderboard?)").queue();
            } else
            {
                message.editMessage(context.getLocalized("general.unknown_error_occured")).queue();
            }
            return;
        }
        if (transferredPlayers == 0)
        {
            message.editMessage("Could not find any users!").queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.green)
                .setDescription("✅ XP has been transferred for **" + transferredPlayers + "** users!")
                .setTimestamp(Instant.now());
        message.editMessage("Success!").embed(builder.build()).queue();
    }
}
