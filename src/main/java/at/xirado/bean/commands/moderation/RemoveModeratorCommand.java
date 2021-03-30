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

public class RemoveModeratorCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(RemoveModeratorCommand.class);

    public RemoveModeratorCommand(JDA jda)
    {
        super(jda);
        this.invoke = "removemod";
        this.aliases = Arrays.asList("removemoderator");
        this.commandType = CommandType.ADMIN;
        this.usage = "removemod [@role/id]";
        this.description = "removes the ability of a role to use moderator-commands";
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
            event.replyError("ID may not be empty!");
            return;
        }
        Role role = guild.getRoleById(roleID);
        if(role == null)
        {
            event.replyError("Invalid role!");
            return;
        }
        if(!permissionCheckerManager.isAllowedRole(guild.getIdLong(), role.getIdLong()))
        {
            event.replyWarning("This role is already not allowed to use moderator-commands!");
            return;
        }
        boolean success = permissionCheckerManager.removeAllowedRole(guild.getIdLong(), role.getIdLong());
        if(success)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(role.getColor())
                    .setDescription(role.getAsMention() + " is now no longer able to use moderator-commands!");
            event.reply(builder.build());
            logger.debug("Removed moderator role "+role.getIdLong()+" (@"+role.getName()+") from guild "+guild.getIdLong()+" ("+guild.getName()+")");

        }else
        {
            event.replyError("An error occured!");

        }

    }
}
