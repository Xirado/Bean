package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.BlacklistManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Blacklist extends Command
{

	public Blacklist(JDA jda)
	{
		super(jda);
		this.invoke = "blacklist";
		this.usage = "blacklist [add/remove/list] (word)";
		this.description = "Modifies the list of blacklisted words";
		this.neededPermissions = Arrays.asList(Permission.MESSAGE_MANAGE);
		this.commandType = CommandType.MODERATION;
	}

	@Override
	public void executeCommand(CommandEvent e)
	{
		String[] args = e.getArguments().toStringArray();
		User user = e.getAuthor();
		Guild guild = e.getGuild();
		TextChannel channel = e.getChannel();
		BlacklistManager blMan = DiscordBot.instance.blacklistManager;
		if(args.length < 1)
		{
			e.replyErrorUsage();
			return;
		}
		String subcommand = args[0];
		
		if(subcommand.equalsIgnoreCase("add"))
		{
			if(args.length != 2)
			{
				e.replyErrorUsage();
				return;
			}
			if(blMan.containsBlacklistedWord(guild.getIdLong(), args[1].toUpperCase()))
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setDescription(e.getLocalized("commands.blacklist.already_on_blacklist"))
						.setColor(Color.RED)
						.setTimestamp(Instant.now());
				channel.sendMessage(builder.build()).queue();
				return;
				
			}
			blMan.addBlacklistedWord(guild.getIdLong(), args[1].toUpperCase());
			EmbedBuilder builder = new EmbedBuilder()
				.setDescription(e.getLocalized("commands.blacklist.added"))
				.setColor(Color.GREEN)
				.addField("Moderator", user.getAsMention(), true)
				.addField(e.getLocalized("commands.blacklist.word"), StringUtils.capitalize(args[1].toLowerCase()), true)
				.setTimestamp(Instant.now());
			channel.sendMessage(builder.build()).queue();
		}else if(subcommand.equalsIgnoreCase("remove"))
		{
			if(args.length != 2)
			{
				e.replyErrorUsage();
				return;
			}
			List<String> currentlist = DiscordBot.instance.blacklistManager.getBlacklistedWords(guild.getIdLong());
			if(!blMan.containsBlacklistedWord(guild.getIdLong(), args[1].toUpperCase()))
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setDescription(e.getLocalized("commands.blacklist.not_on_blacklist"))
						.setColor(Color.RED)
						.setTimestamp(Instant.now());
				channel.sendMessage(builder.build()).queue();
				return;

			}
			blMan.removeBlacklistedWord(guild.getIdLong(), args[1].toUpperCase());
			EmbedBuilder builder = new EmbedBuilder()
					.setDescription(e.getLocalized("commands.blacklist.removed"))
					.setColor(Color.GREEN)
					.addField("Moderator", user.getAsMention(), true)
					.addField(e.getLocalized("commands.blacklist.word"), StringUtils.capitalize(args[1].toLowerCase()), true)
					.setTimestamp(Instant.now());
				channel.sendMessage(builder.build()).queue();
				return;
			
		}else if(subcommand.equalsIgnoreCase("list"))
		{
			List<String> currentlist = DiscordBot.instance.blacklistManager.getBlacklistedWords(guild.getIdLong());
			if(currentlist.size() <= 0)
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setDescription(e.getLocalized("commands.blacklist.empty"))
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
								.setFooter(e.getLocalized("commands.blacklist.footer", guild.getName()))
								.setTitle(e.getLocalized("commands.blacklist.header"))
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
