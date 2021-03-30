package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.SQL;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

public class Unban extends SlashCommand
{

    public Unban()
    {
        setCommandData(new CommandUpdateAction.CommandData("unban", "unbans a user")
            .addOption(new CommandUpdateAction.OptionData(Command.OptionType.USER, "user", "the user to unban (you can use IDs)").setRequired(true))
        );
        setNeededUserPermissions(Collections.singletonList(Permission.BAN_MEMBERS));
        setNeededBotPermissions(Collections.singletonList(Permission.BAN_MEMBERS));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        Guild g = event.getGuild();
        if(g == null) return;
        Member m = event.getMember();
        SlashCommandEvent.OptionData optionData = event.getOption("user");
        if(optionData == null)
        {
            ctx.reply(CommandContext.ERROR+" An error occured. Please try again later!").setEphemeral(true).queue();
            return;
        }
        User target = optionData.getAsUser();
        g.retrieveBan(target).queue(
                (ban) ->
                {
                    g.unban(target).queue(
                            (success) ->
                            {
                                String qry = "UPDATE modcases SET active = 0 WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = 1";
                                Connection connection = SQL.getConnectionFromPool();
                                if(connection == null)
                                {
                                    return;
                                }
                                try(var ps = connection.prepareStatement(qry))
                                {
                                    ps.setLong(1, g.getIdLong());
                                    ps.setLong(2, target.getIdLong());
                                    ps.setString(3, "Tempban");
                                    ps.execute();
                                }catch (SQLException e)
                                {
                                    return;
                                } finally
                                {
                                    Util.closeQuietly(connection);
                                }
                                ctx.reply(CommandContext.SUCCESS+" "+target.getAsMention()+" has been unbanned").setEphemeral(true).queue();
                                TextChannel logchannel = DiscordBot.getInstance().logChannelManager.getLogChannel(g.getIdLong());
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(Color.green)
                                        .setTitle("Unban")
                                        .setThumbnail(target.getEffectiveAvatarUrl())
                                        .addField("unbanned", target.getAsMention()+" ("+target.getAsTag()+")" , true)
                                        .addField("moderator", m.getAsMention()+" ("+m.getUser().getAsTag()+")", true);
                                if(logchannel != null)
                                {
                                    logchannel.sendMessage(builder.build()).queue();
                                }

                            },
                            (error) ->
                                    ctx.reply(CommandContext.ERROR+" An error occured. Please try again later!").setEphemeral(true).queue()
                    );
                },
                new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_BAN, (ex) -> ctx.reply(CommandContext.ERROR+" This user is not banned!").setEphemeral(true).queue())
                    .handle(EnumSet.allOf(ErrorResponse.class), (ex) -> ctx.reply(CommandContext.ERROR+" An error occured. Please try again later!").setEphemeral(true).queue())
        );
    }
}
