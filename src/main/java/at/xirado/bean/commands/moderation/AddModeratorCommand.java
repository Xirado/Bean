package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class AddModeratorCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(AddModeratorCommand.class);
    public AddModeratorCommand(JDA jda)
    {
        super(jda);
        this.invoke = "addmod";
        this.aliases = Arrays.asList("addmoderator");
        this.commandType = CommandType.ADMIN;
        this.usage = "addmod [@role/id]";
        this.description = "Allows a certain role to use mod-commands";
        this.neededPermissions = Arrays.asList(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Member member = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        String[] args = event.getArguments().toStringArray();
        if(args.length != 1)
        {
            event.replyErrorUsage();
            return;
        }
        Guild guild = event.getGuild();
        String roleID = args[0].replaceAll("[^0-9]", "");
        if(roleID.length() == 0)
        {
            event.replyError(event.getLocalized("commands.id_empty"));
            return;
        }
        Role role = guild.getRoleById(roleID);
        if(role == null)
        {
            event.replyError(event.getLocalized("commands.invalid_role"));
            return;
        }
        if(permissionCheckerManager.isAllowedRole(guild.getIdLong(), role.getIdLong()))
        {
            event.replyWarning(event.getLocalized("commands.moderator.already_added"));
            return;
        }
        boolean success = permissionCheckerManager.addAllowedRole(guild.getIdLong(), role.getIdLong());
        if(success)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(role.getColor())
                    .setDescription(event.getLocalized("commands.moderator.added", role.getAsMention()));
            event.reply(builder.build());
            logger.debug("Added moderator role "+role.getIdLong()+" (@"+role.getName()+") to guild "+guild.getIdLong()+" ("+guild.getName()+")");

        }else
        {
            event.replyError(event.getLocalized("general.unknown_error_occured"));

        }

    }
}
