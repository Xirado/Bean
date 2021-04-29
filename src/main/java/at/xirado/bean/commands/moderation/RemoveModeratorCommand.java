package at.xirado.bean.commands.moderation;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveModeratorCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(RemoveModeratorCommand.class);

    public RemoveModeratorCommand()
    {
        super("removemod", "restricts roles from executing moderator-commands", "removemod [@role/id]");
        setAliases("removemoderator");
        setCommandCategory(CommandCategory.ADMIN);
        setRequiredPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        PermissionCheckerManager permissionCheckerManager = Bean.getInstance().permissionCheckerManager;
        String[] args = context.getArguments().toStringArray();
        if(args.length != 1)
        {
            context.replyErrorUsage();
            return;
        }
        Guild guild = event.getGuild();
        String roleID = args[0].replaceAll("[^0-9]", "");
        if(roleID.length() == 0)
        {
            context.replyError(context.getLocalized("commands.id_empty"));
            return;
        }
        Role role = guild.getRoleById(roleID);
        if(role == null)
        {
            context.replyError(context.getLocalized("commands.invalid_role"));
            return;
        }
        if(!permissionCheckerManager.isAllowedRole(guild.getIdLong(), role.getIdLong()))
        {
            context.replyWarning(context.getLocalized("commands.moderator.not_added"));
            return;
        }
        boolean success = permissionCheckerManager.removeAllowedRole(guild.getIdLong(), role.getIdLong());
        if(success)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(role.getColor())
                    .setDescription(context.getLocalized("commands.moderator.removed", role.getAsMention()));
            context.reply(builder.build());
            logger.debug("Removed moderator role "+role.getIdLong()+" (@"+role.getName()+") from guild "+guild.getIdLong()+" ("+guild.getName()+")");

        }else
        {
            context.replyError(context.getLocalized("general.unknown_error_occured"));
        }

    }
}
