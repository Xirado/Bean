package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.Punishments;
import at.xirado.bean.translation.FormattedDuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ModlogCommand extends SlashCommand
{

    public ModlogCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("modlog", "shows the modlog of a user")
            .addOption(new CommandUpdateAction.OptionData(Command.OptionType.USER, "user", "the user to look for").setRequired(true))
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        Member m = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            ctx.replyError(ctx.getLocalized("general.no_perms")).queue();
            return;
        }
        Guild g = event.getGuild();
        long targetID;
        User user = event.getOption("user").getAsUser();
        if(user == null || g == null) return;
        List<Case> allCases = Punishments.getModlog(g.getIdLong(), user.getIdLong(), 10);
        if(allCases.isEmpty())
        {
            ctx.reply(ctx.getLocalized("commands.modlog.no_modlogs")).queue();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(Case modcase : allCases)
        {
            sb.append(modcase.getType().getFriendlyName()).append(" (#").append(modcase.getCaseID()).append(") **").append(modcase.getReason()).append("** • ").append(FormattedDuration.getDuration(modcase.getCreatedAt() / 1000, true, ctx.getLanguage())).append("\n");
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.orange)
                .setFooter(ctx.getLocalized("commands.target_id")+": "+user.getIdLong())
                .setTimestamp(Instant.now())
                .setAuthor(ctx.getLocalized("commands.modlog.for_user", user.getAsTag()), null, user.getEffectiveAvatarUrl())
                .setDescription("**"+ctx.getLocalized("commands.modlog.last_10_incidents")+":**\n\n"+sb.toString().trim()+"\n\n"+ctx.getLocalized("commands.modlog.more_infos_slash"));
        ctx.reply(builder.build()).queue();
    }
}
