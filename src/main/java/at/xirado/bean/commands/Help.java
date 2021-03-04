package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Help extends Command
{

	public Help(JDA jda)
	{
		super(jda);
		this.invoke = "help";
		this.global = true;

	}

	@Override
	public void executeCommand(CommandEvent e)
	{
		String[] args = e.getArguments().toStringArray();
		Member member = e.getMember();
		User user = e.getAuthor();
		Guild guild = e.getGuild();
		String Prefix = DiscordBot.instance.prefixManager.getPrefix(guild.getIdLong());
		TextChannel channel = e.getChannel();
		if(args.length != 1)
		{
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(Color.decode("#FEFEFE"))
					.setAuthor(user.getAsTag(), null, user.getAvatarUrl())
					.setTitle("BeanBot command list")
					.setTimestamp(Instant.now());
			for(CommandType type : CommandType.values())
			{
				if(type == CommandType.EXCLUDED)
					continue;
				String name = StringUtils.capitalize(type.toString().toLowerCase());
				String aswhole = type.getEmoji()+" "+name;
				String command = "`"+Prefix+"help "+name+"`";

				builder.addField(aswhole, command, true);
			}
			builder.addField("Modules", "`"+Prefix+"help Modules`", true);
			builder.addField("⏬ Support? ⏬", "[Click here](https://discord.gg/SxjyBuD)", false);
			channel.sendMessage(builder.build()).queue();
			return;
		}
		String subarg = args[0];
		if(subarg.equalsIgnoreCase("Excluded"))
			return;
		if(subarg.equalsIgnoreCase("modules"))
		{
			ArrayList<Command> modules = DiscordBot.instance.commandManager.getRegisteredModules(e.getGuild().getIdLong());
			modules = modules.stream().filter((c) -> c.getCommandType() != CommandType.EXCLUDED).collect(Collectors.toCollection(ArrayList::new));
			if(modules.size() == 0)
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setColor(Color.red)
						.setTimestamp(Instant.now())
						.setDescription("\uD83D\uDEAB No modules have been enabled! \uD83D\uDEAB")
						.setAuthor(user.getAsTag(), null, user.getAvatarUrl());
				channel.sendMessage(builder.build()).queue();
				return;
			}
			StringBuilder sb = new StringBuilder();
			for(Command module : modules)
			{
				if(module.usage == null || module.description == null) continue;
				sb.append("`").append(Prefix).append(module.usage).append("`\n").append(module.description).append("\n");
			}
			String tostring = sb.toString().trim();
			EmbedBuilder embed = new EmbedBuilder()
					.setColor(Color.decode("#FEFEFE"))
					.setAuthor(user.getAsTag(), null, user.getAvatarUrl())
					.setTitle("Enabled Modules")
					.setThumbnail(DiscordBot.instance.jda.getSelfUser().getAvatarUrl())
					.setDescription(tostring)
					.setTimestamp(Instant.now());
			channel.sendMessage(embed.build()).queue();
			return;
		}
		CommandType type;
		try {
			type = CommandType.valueOf(subarg.toUpperCase());
		} catch (IllegalArgumentException illegalArgumentException) {
			return;
		}
		String name = StringUtils.capitalize(type.toString().toLowerCase());
		ArrayList<Command> commands = DiscordBot.instance.commandManager.registeredCommands.stream().filter(command -> command.commandType == type && command.accessibleOn(guild.getIdLong())).collect(Collectors.toCollection(ArrayList::new));
		if(commands.size() == 0)
		{
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(Color.decode("#FEFEFE"))
					.setTitle("Help - "+name)
					.setDescription("Oh, there's nothing there (yet?)")
					.setTimestamp(Instant.now());
			channel.sendMessage(builder.build()).queue();
			return;
		}
		StringBuilder sb = new StringBuilder();
		for(Command command : commands)
		{
			sb.append("`").append(Prefix).append(command.getUsage()).append("` - ").append(command.getDescription()).append("\n");
		}
		String tostring = sb.toString().trim();
		EmbedBuilder embed = new EmbedBuilder()
				.setColor(Color.decode("#FEFEFE"))
				.setTitle("Help - "+name)
				.setTimestamp(Instant.now());
		String description = type.getNotes();
		if(description.length() > 0)
			embed.addField("", description, false);
		embed.addField("", tostring,false);
		channel.sendMessage(embed.build()).queue();
		return;
	}



}
