package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.translation.FormattedDuration;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.Punishments;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

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
        this.usage = "modlog [@user/id]";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Member m = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            event.replyError(event.getLocalized("general.no_perms"));
            return;
        }
        Guild g = event.getGuild();
        String[] args = event.getArguments().toStringArray();
        if(args.length != 1)
        {
            event.replyErrorUsage();
            return;
        }
        long targetID;
        try
        {
            targetID = Long.parseLong(args[0].replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e)
        {
            event.replyError(event.getLocalized("commands.id_empty"));
            return;
        }
        DiscordBot.getInstance().jda.retrieveUserById(targetID).queue(
                (targetUser) ->
                {
                    List<Case> allCases = Punishments.getModlog(g.getIdLong(), targetUser.getIdLong(), 10);
                    if(allCases.isEmpty())
                    {
                        event.replyWarning(event.getLocalized("commands.modlog.no_modlogs"));
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for(Case modcase : allCases)
                    {
                        sb.append(modcase.getType().getFriendlyName()).append(" (#").append(modcase.getCaseID()).append(") **").append(modcase.getReason()).append("** • ").append(FormattedDuration.getDuration(modcase.getCreatedAt() / 1000, true)).append("\n");
                    }
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.orange)
                            .setFooter(event.getLocalized("commands.target_id")+": "+targetUser.getIdLong())
                            .setTimestamp(Instant.now())
                            .setAuthor(event.getLocalized("commands.modlog.for_user", targetUser.getAsTag()), null, targetUser.getEffectiveAvatarUrl())
                            .setDescription("**"+event.getLocalized("commands.modlog.last_10_incidents")+":**\n\n"+sb.toString().trim()+"\n\n"+event.getLocalized("commands.modlog.more_infos", DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())));
                    event.reply(builder.build());
                },
                (error) -> event.replyError(event.getLocalized("commands.user_not_exists"))
        );
    }
}
