package at.xirado.bean.commands;


import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Announce extends Command
{

	public Announce()
	{
		super("announce", "Creates an announcement in a channel", "announce [#Channel] [Message]");
		setRequiredPermissions(Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_MANAGE);
		setRequiredBotPermissions(Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_MANAGE);
		setCommandCategory(CommandCategory.ADMIN);
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{

		String[] args = context.getArguments().toStringArray();
		event.getMessage().delete().queue();
		Guild g = event.getGuild();
		if(args.length < 2)
		{
			context.replyErrorUsage();
			return;
		}
		TextChannel targetChannel = event.getGuild().getTextChannelById(args[0].replaceAll("[^0-9]", ""));
		if(targetChannel == null)
		{
			context.replyError("Invalid channel!");
			return;
		}
		String message = context.getArguments().toString(1);
		targetChannel.sendMessage(
				new EmbedBuilder()
					.setAuthor(g.getName(), null, g.getIconUrl())
					.setDescription(message)
					.build()
		).queue(
				(result) -> {
					targetChannel.sendMessage("@everyone").queue(null, Util.ignoreAllErrors());
				}
		);
	}
}
