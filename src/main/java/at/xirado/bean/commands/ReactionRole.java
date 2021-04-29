package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ReactionRole extends Command
{

	public ReactionRole()
	{
		super("rr", "Adds reaction roles to messages", "rr");
		setRequiredPermissions(Permission.ADMINISTRATOR);
		setCommandCategory(CommandCategory.ADMIN);
		setAliases("reactionrole", "reactionroles");
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{
		context.reply("Please use /reactionrole! (Reinvite me from https://bean.bz/ if slash-commands are not available.");
	}
}
