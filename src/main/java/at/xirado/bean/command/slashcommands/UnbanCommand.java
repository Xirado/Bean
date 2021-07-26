package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
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

public class UnbanCommand extends SlashCommand
{

    public UnbanCommand()
    {
        setCommandData(new CommandData("unban", "unbans a user")
                .addOptions(new OptionData(OptionType.USER, "user", "the user to unban (you can use IDs)").setRequired(true))
        );
        setRequiredUserPermissions(Permission.BAN_MEMBERS);
        setRequiredBotPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild g = event.getGuild();
        if (g == null) return;
        User target = event.getOption("user").getAsUser();
        g.retrieveBan(target)
                .queue(
                        (ban) ->
                                g.unban(target)
                                        .flatMap((x) -> event.replyEmbeds(EmbedUtil.successEmbed(target.getAsTag()+" has been unbanned!")))
                                        .queue(s -> {},
                                                new ErrorHandler()
                                                    .handle(ErrorResponse.MISSING_PERMISSIONS, e -> event.replyEmbeds(EmbedUtil.noEntryEmbed("I do not have the permission to unban users!")).queue())
                                        )
                        , new ErrorHandler().handle(ErrorResponse.UNKNOWN_BAN, (e) -> event.replyEmbeds(EmbedUtil.errorEmbed("This user is not banned!")).queue())
                );
    }
}
