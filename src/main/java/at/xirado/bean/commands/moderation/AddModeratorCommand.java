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

import java.util.Collections;

public class AddModeratorCommand extends Command
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AddModeratorCommand.class);

    public AddModeratorCommand()
    {
        super("addmod", "Allows a certain role to use moderator-commands", "addmod [@role/id]");
        setAliases("addmoderator");
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
        if(permissionCheckerManager.isAllowedRole(guild.getIdLong(), role.getIdLong()))
        {
            context.replyWarning(context.getLocalized("commands.moderator.already_added"));
            return;
        }
        boolean success = permissionCheckerManager.addAllowedRole(guild.getIdLong(), role.getIdLong());
        if(success)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(role.getColor())
                    .setDescription(context.getLocalized("commands.moderator.added", role.getAsMention()));
            event.getChannel().sendMessage(builder.build()).allowedMentions(Collections.emptyList()).queue();

        }else
        {
            context.replyError(context.getLocalized("general.unknown_error_occured"));

        }

    }
}
