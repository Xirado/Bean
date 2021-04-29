package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.commandutil.CommandFlag;
import at.xirado.bean.handlers.BlacklistManager;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class Blacklist extends Command
{

	public Blacklist()
	{
		super("blacklist", "Modifies the list of blacklisted words", "blacklist [add/remove/list] (word)");
		setCommandFlags(CommandFlag.MODERATOR_ONLY);
		setCommandCategory(CommandCategory.MODERATION);
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{
		String[] args = context.getArguments().toStringArray();
		User user = event.getAuthor();
		Guild guild = event.getGuild();
		TextChannel channel = event.getChannel();
		BlacklistManager blMan = Bean.instance.blacklistManager;
		if(args.length < 1)
		{
			context.replyErrorUsage();
			return;
		}
		String subcommand = args[0];
		
		if(subcommand.equalsIgnoreCase("add"))
		{
			if(args.length != 2)
			{
				context.replyErrorUsage();
				return;
			}
			if(blMan.containsBlacklistedWord(guild.getIdLong(), args[1].toUpperCase()))
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setDescription(context.getLocalized("commands.blacklist.already_on_blacklist"))
						.setColor(Color.RED)
						.setTimestamp(Instant.now());
				channel.sendMessage(builder.build()).queue();
				return;
				
			}
			blMan.addBlacklistedWord(guild.getIdLong(), args[1].toUpperCase());
			EmbedBuilder builder = new EmbedBuilder()
				.setDescription(context.getLocalized("commands.blacklist.added"))
				.setColor(Color.GREEN)
				.addField("Moderator", user.getAsMention(), true)
				.addField(context.getLocalized("commands.blacklist.word"), StringUtils.capitalize(args[1].toLowerCase()), true)
				.setTimestamp(Instant.now());
			channel.sendMessage(builder.build()).queue();
		}else if(subcommand.equalsIgnoreCase("remove"))
		{
			if(args.length != 2)
			{
				context.replyErrorUsage();
				return;
			}
			List<String> currentlist = Bean.instance.blacklistManager.getBlacklistedWords(guild.getIdLong());
			if(!blMan.containsBlacklistedWord(guild.getIdLong(), args[1].toUpperCase()))
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setDescription(context.getLocalized("commands.blacklist.not_on_blacklist"))
						.setColor(Color.RED)
						.setTimestamp(Instant.now());
				channel.sendMessage(builder.build()).queue();
				return;

			}
			blMan.removeBlacklistedWord(guild.getIdLong(), args[1].toUpperCase());
			EmbedBuilder builder = new EmbedBuilder()
					.setDescription(context.getLocalized("commands.blacklist.removed"))
					.setColor(Color.GREEN)
					.addField("Moderator", user.getAsMention(), true)
					.addField(context.getLocalized("commands.blacklist.word"), StringUtils.capitalize(args[1].toLowerCase()), true)
					.setTimestamp(Instant.now());
				channel.sendMessage(builder.build()).queue();
				return;
			
		}else if(subcommand.equalsIgnoreCase("list"))
		{
			List<String> currentlist = Bean.instance.blacklistManager.getBlacklistedWords(guild.getIdLong());
			if(currentlist.size() <= 0)
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setDescription(context.getLocalized("commands.blacklist.empty"))
						.setColor(Color.RED)
						.setTimestamp(Instant.now());
					channel.sendMessage(builder.build()).queue();
					return;
			}
			StringBuilder sb = new StringBuilder();
			for(String s : currentlist)
			{
				sb.append(StringUtils.capitalize(s.toLowerCase())).append(", ");
			}
			String tostring = sb.toString();
			final String allwords = tostring.substring(0, tostring.length()-2);
			user.openPrivateChannel().queue(
					(pc)->
					{
						EmbedBuilder builder = new EmbedBuilder()
								.setColor(Color.green)
								.setTimestamp(Instant.now())
								.setFooter(context.getLocalized("commands.blacklist.footer", guild.getName()))
								.setTitle(context.getLocalized("commands.blacklist.header"))
								.setDescription("```"+allwords+"```")
								.setAuthor(guild.getName(), null, guild.getIconUrl());
						pc.sendMessage(builder.build()).queue(
								null,
								Util.handle(channel)
								);
					},
					Util.handle(channel)
			);
		}
		
	}

}
