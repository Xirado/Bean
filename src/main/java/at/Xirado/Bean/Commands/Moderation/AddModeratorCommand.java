package at.Xirado.Bean.Commands.Moderation;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Handlers.PermissionCheckerManager;
import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;

public class AddModeratorCommand extends Command
{
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
            event.replyError("ID may not be empty!");
            return;
        }
        Role role = guild.getRoleById(roleID);
        if(role == null)
        {
            event.replyError("Invalid role!");
            return;
        }
        if(permissionCheckerManager.isAllowedRole(guild.getIdLong(), role.getIdLong()))
        {
            event.replyWarning("This role is already allowed to use moderator-commands!");
            return;
        }
        boolean success = permissionCheckerManager.addAllowedRole(guild.getIdLong(), role.getIdLong());
        if(success)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(role.getColor())
                    .setDescription(role.getAsMention() + " can now use moderator-commands!");
            event.reply(builder.build());
            Console.logger.debug("Added moderator role "+role.getIdLong()+" (@"+role.getName()+") to guild "+guild.getIdLong()+" ("+guild.getName()+")");

        }else
        {
            event.replyError("An error occured!");

        }

    }
}
