package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.Punishments;
import at.xirado.bean.translation.FormattedDuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ModlogCommand extends Command
{

    public ModlogCommand(JDA jda)
    {
        super(jda);
        this.invoke = "modlog";
        this.aliases = Arrays.asList("modlogs", "infraction", "infractions", "moderatorlog", "moderatorlogs");
        this.commandType = CommandType.MODERATION;
        this.description = "Shows the modlog of a user";
        this.usage = "modlog [@user]";
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Member m = context.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            context.replyError(context.getLocalized("general.no_perms"));
            return;
        }
        Guild g = event.getGuild();
        if(event.getMessage().getMentionedUsers().size() == 0)
        {
            context.replyErrorUsage();
            return;
        }
        User user = event.getMessage().getMentionedUsers().get(0);
        List<Case> allCases = Punishments.getModlog(g.getIdLong(), user.getIdLong(), 10);
        if(allCases.isEmpty())
        {
            context.replyWarning(context.getLocalized("commands.modlog.no_modlogs"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(Case modcase : allCases)
        {
            sb.append(modcase.getType().getFriendlyName()).append(" (#").append(modcase.getCaseID()).append(") **").append(modcase.getReason()).append("** • ").append(FormattedDuration.getDuration(modcase.getCreatedAt() / 1000, true, context.getLanguage())).append("\n");
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.orange)
                .setFooter(context.getLocalized("commands.target_id")+": "+user.getIdLong())
                .setTimestamp(Instant.now())
                .setAuthor(context.getLocalized("commands.modlog.for_user", user.getAsTag()), null, user.getEffectiveAvatarUrl())
                .setDescription("**"+context.getLocalized("commands.modlog.last_10_incidents")+":**\n\n"+sb.toString().trim()+"\n\n"+context.getLocalized("commands.modlog.more_infos", DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())));
        context.reply(builder.build());
    }
}
