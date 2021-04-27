package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Member member = context.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(member) && !member.hasPermission(Permission.ADMINISTRATOR))
        {
            context.replyError(context.getLocalized("general.no_perms"));
            return;
        }
        Guild guild = event.getGuild();
        ArrayList<Long> allowedRoles = permissionCheckerManager.getAllowedRoles(guild.getIdLong());
        if(allowedRoles == null || allowedRoles.isEmpty())
        {
            context.replyWarning(context.getLocalized("commands.listmods.no_roles_found"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        Color firstColor = null;
        ArrayList<Role> allowedRolesAsObject = new ArrayList<>();
        for(Long roleID : allowedRoles)
        {
            Role role = guild.getRoleById(roleID);
            if(role == null)
            {
                permissionCheckerManager.removeAllowedRole(guild.getIdLong(), roleID);
                continue;
            }
            allowedRolesAsObject.add(role);
        }
        if(allowedRolesAsObject.size() > 1)
        {
            allowedRolesAsObject = allowedRolesAsObject.stream().sorted(Comparator.comparingInt(Role::getPosition)).collect(Collectors.toCollection(ArrayList::new));
            Collections.reverse(allowedRolesAsObject);
        }
        for(Role r : allowedRolesAsObject)
        {
            if(firstColor == null) firstColor = r.getColor();
            sb.append(r.getAsMention()).append(", ");
        }
        String description = sb.toString();
        description = description.substring(0, description.length()-2);
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(firstColor == null ? Color.green : firstColor)
                .setDescription(context.getLocalized("commands.listmods.all_mod_roles")+":\n"+description);
        context.reply(builder.build());

    }
}
