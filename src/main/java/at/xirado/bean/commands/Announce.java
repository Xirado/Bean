package at.xirado.bean.commands;


import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class Announce extends Command
{

	public Announce(JDA jda)
	{
		super(jda);
		this.invoke = "announce";
		this.neededPermissions = Arrays.asList(Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_MANAGE);
		this.description = "Creates an announcement in a channel";
		this.usage = "announce #Channel [Text]";
		this.commandType = CommandType.ADMIN;
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{

		String[] args = context.getArguments().toStringArray();
		event.getMessage().delete().queue();
		Guild g = event.getGuild();
		TextChannel c = event.getChannel();
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
