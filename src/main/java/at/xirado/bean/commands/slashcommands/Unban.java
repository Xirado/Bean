package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;

public class Unban extends SlashCommand
{

    public Unban()
    {
        setCommandData(new CommandData("unban", "unbans a user")
            .addOption(new OptionData(OptionType.USER, "user", "the user to unban (you can use IDs)").setRequired(true))
        );
        setRequiredUserPermissions(Permission.BAN_MEMBERS);
        setRequiredBotPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild g = event.getGuild();
        if(g == null) return;
        User target = event.getOption("user").getAsUser();
        if(target == null) return;
        g.retrieveBan(target).queue(
                (ban) ->
                {
                    g.unban(target).queue(
                            (success) ->
                            {
                                String qry = "UPDATE modcases SET active = 0 WHERE guildID = ? AND targetID = ? AND caseType = ? AND active = 1";
                                Connection connection = Database.getConnectionFromPool();
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
                                ctx.reply(SlashCommandContext.SUCCESS+" "+ctx.getLocalized("commands.unban.has_been_unbanned", target.getAsTag())).setEphemeral(true).queue();
                                TextChannel logchannel = Bean.getInstance().logChannelManager.getLogChannel(g.getIdLong());
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(Color.green)
                                        .setTitle(ctx.getLocalized("commands.unban.unban"))
                                        .setThumbnail(target.getEffectiveAvatarUrl())
                                        .addField(ctx.getLocalized("commands.target"), target.getAsMention()+" ("+target.getAsTag()+")" , true)
                                        .addField("Moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true);
                                if(logchannel != null)
                                {
                                    logchannel.sendMessage(builder.build()).queue();
                                }

                            },
                            (error) ->
                                    ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue()
                    );
                },
                new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_BAN, (ex) -> ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("commands.unban.not_banned")).setEphemeral(true).queue())
                    .handle(EnumSet.allOf(ErrorResponse.class), (ex) -> ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue())
        );
    }
}
