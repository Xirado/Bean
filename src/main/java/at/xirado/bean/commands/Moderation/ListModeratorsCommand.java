package at.xirado.bean.commands.Moderation;

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

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ListModeratorsCommand extends Command
{
    public ListModeratorsCommand(JDA jda)
    {
        super(jda);
        this.invoke = "listmods";
        this.aliases = Arrays.asList("listmoderator", "listmoderators");
        this.commandType = CommandType.ADMIN;
        this.usage = "listmods";
        this.description = "lists all roles that are allowed to use moderator-commands (Administrators excluded)";
        this.neededPermissions = Collections.singletonList(Permission.ADMINISTRATOR);
    }
    @Override
    public void executeCommand(CommandEvent event)
    {
        Member member = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(member) && !member.hasPermission(Permission.ADMINISTRATOR))
        {
            event.replyError("You are not permissed to do this!");
            return;
        }
        Guild guild = event.getGuild();
        ArrayList<Long> allowedRoles = permissionCheckerManager.getAllowedRoles(guild.getIdLong());
        if(allowedRoles == null || allowedRoles.isEmpty())
        {
            event.replyWarning("No mod-roles have been found!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        Color firstColor = null;
        ArrayList<Role> allowedRolesasObject = new ArrayList<>();
        for(Long roleID : allowedRoles)
        {
            Role role = guild.getRoleById(roleID);
            if(role == null)
            {
                permissionCheckerManager.removeAllowedRole(guild.getIdLong(), roleID);
                continue;
            }
            allowedRolesasObject.add(role);
        }
        if(allowedRolesasObject.size() > 1)
        {
            allowedRolesasObject = allowedRolesasObject.stream().sorted(Comparator.comparingInt(Role::getPosition)).collect(Collectors.toCollection(ArrayList::new));
            Collections.reverse(allowedRolesasObject);
        }
        for(Role r : allowedRolesasObject)
        {
            if(firstColor == null) firstColor = r.getColor();
            sb.append(r.getAsMention()).append(", ");
        }
        String description = sb.toString();
        description = description.substring(0, description.length()-2);
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(firstColor == null ? Color.green : firstColor)
                .setDescription("All moderator roles:\n"+description);
        event.reply(builder.build());

    }
}
