package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        if(args.length != 1)
        {
            context.replyErrorUsage();
            return;
        }

        String roleID = args[0].replaceAll("[^0-9]", "");
        if(roleID.length() == 0)
        {
            context.replyError(context.getLocalized("commands.id_empty"));
            return;
        }
        Guild g = event.getGuild();
        Role role = g.getRoleById(roleID);
        if(role == null)
        {
            context.replyError(context.getLocalized("commands.invalid_role"));
            return;
        }
        if(!event.getGuild().getSelfMember().canInteract(role))
        {
            context.replyError(context.getLocalized("commands.cannot_interact_role", role.getAsMention()));
            return;
        }
        DiscordBot.getInstance().mutedRoleManager.setMutedRole(g.getIdLong(), role.getIdLong());
        context.reply(new EmbedBuilder()
            .setColor(Color.green)
            .setDescription(context.getLocalized("commands.set_muted_role", role.getAsMention()))
            .build()
        );
    }
}
