package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class ReactionRole extends Command
{

	public ReactionRole(JDA jda)
	{
		super(jda);
		this.invoke = "rr";
		this.description = "Adds reaction roles to messages";
		this.usage = "rr [#TextChannel] [Message-ID] [@Role] [Emoji] || rr clear [#TextChannel] [Message-ID]";
		this.neededPermissions = Arrays.asList(Permission.ADMINISTRATOR);
		this.commandType = CommandType.ADMIN;
		this.aliases = Arrays.asList("reactionrole", "reactionroles", "createreactionrole");
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{
		context.reply("Please use /reactionrole! (Reinvite me from https://bean.bz/ if slash-commands are not available.");
	}
}
