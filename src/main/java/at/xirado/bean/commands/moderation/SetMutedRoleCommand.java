package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.Collections;

public class SetMutedRoleCommand extends Command
{
    public SetMutedRoleCommand(JDA jda)
    {
        super(jda);
        this.invoke = "setmutedrole";
        this.commandType = CommandType.ADMIN;
        this.neededPermissions = Collections.singletonList(Permission.ADMINISTRATOR);
        this.description = "Sets the role given to muted members";
        this.usage = "setmutedrole [@Role/ID]";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        String[] args = event.getArguments().toStringArray();
        if(args.length != 1)
        {
            event.replyErrorUsage();
            return;
        }

        String roleID = args[0].replaceAll("[^0-9]", "");
        if(roleID.length() == 0)
        {
            event.replyError(event.getLocalized("commands.id_empty"));
            return;
        }
        Guild g = event.getGuild();
        Role role = g.getRoleById(roleID);
        if(role == null)
        {
            event.replyError(event.getLocalized("commands.invalid_role"));
            return;
        }
        if(!event.getSelfMember().canInteract(role))
        {
            event.replyError(event.getLocalized("commands.cannot_interact_role", role.getAsMention()));
            return;
        }
        DiscordBot.getInstance().mutedRoleManager.setMutedRole(g.getIdLong(), role.getIdLong());
        event.reply(new EmbedBuilder()
            .setColor(Color.green)
            .setDescription(event.getLocalized("commands.set_muted_role", role.getAsMention()))
            .build()
        );
    }
}
