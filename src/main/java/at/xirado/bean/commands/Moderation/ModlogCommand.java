package at.xirado.bean.commands.Moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.language.FormattedDuration;
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
            event.replyError("You are not permissed to do this!");
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
            event.replyError("ID may not be empty!");
            return;
        }
        DiscordBot.getInstance().jda.retrieveUserById(targetID).queue(
                (targetUser) ->
                {
                    List<Case> allCases = Punishments.getModlog(g.getIdLong(), targetUser.getIdLong(), 10);
                    if(allCases.isEmpty())
                    {
                        event.replyWarning("There are no modlogs for this user!");
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for(Case modcase : allCases)
                    {
                        sb.append(modcase.getType().getFriendlyName()).append(" (#").append(modcase.getCaseID()).append(") **").append(modcase.getReason()).append("** • ").append(FormattedDuration.getDuration(modcase.getCreatedAt() / 1000, true)).append("\n");
                    }
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.orange)
                            .setFooter("User-ID: "+targetUser.getIdLong())
                            .setTimestamp(Instant.now())
                            .setAuthor("Modlogs for "+targetUser.getAsTag(), null, targetUser.getEffectiveAvatarUrl())
                            .setDescription("**Last 10 incidents:**\n\n"+sb.toString().trim()+"\n\nUse `"+DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())+"case [CaseID]` for more infos");
                    event.reply(builder.build());
                },
                (error) -> event.replyError("This user does not exist!")
        );
    }
}
