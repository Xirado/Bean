package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Help extends Command
{

	public Help()
	{
		super("help", "shows a list of all commands", "help (Category)");
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{
		String[] args = context.getArguments().toStringArray();
		User user = event.getAuthor();
		Guild guild = event.getGuild();
		String Prefix = Bean.instance.prefixManager.getPrefix(guild.getIdLong());
		TextChannel channel = event.getChannel();
		if(args.length != 1)
		{
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(0x551a8b)
					.setAuthor(user.getAsTag(), null, user.getAvatarUrl())
					.setTitle("Bean command list")
					.setTimestamp(Instant.now());
			for(CommandCategory type : CommandCategory.values())
			{
				if(type == CommandCategory.NONE)
					continue;
				String name = StringUtils.capitalize(type.toString().toLowerCase());
				String header = type.getEmoji()+" "+name;
				String command = "`"+Prefix+"help "+name+"`";

				builder.addField(header, command, true);
			}
			builder.addField("Modules", "`"+Prefix+"help Modules`", true);
			builder.addField("⏬ Support? ⏬", "[Click here](https://discord.gg/SxjyBuD)", false);
			channel.sendMessage(builder.build()).queue();
			return;
		}
		String subarg = args[0];
		if(subarg.equalsIgnoreCase("None"))
			return;
		if(subarg.equalsIgnoreCase("modules"))
		{
			List<Command> modules = Bean.getInstance().commandHandler.getGuildCommands(guild.getIdLong());
			modules = modules.stream().filter((c) -> c.getCommandCategory() != CommandCategory.NONE).collect(Collectors.toCollection(ArrayList::new));
			if(modules.size() == 0)
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setColor(0x551a8b)
						.setTimestamp(Instant.now())
						.setDescription("\uD83D\uDEAB No modules have been enabled! \uD83D\uDEAB")
						.setAuthor(user.getAsTag(), null, user.getAvatarUrl());
				channel.sendMessage(builder.build()).queue();
				return;
			}
			StringBuilder sb = new StringBuilder();
			for(Command module : modules)
			{
				sb.append("`").append(Prefix).append(module.getUsage()).append("`\n").append(module.getDescription()).append("\n");
			}
			String tostring = sb.toString().trim();
			EmbedBuilder embed = new EmbedBuilder()
					.setColor(0x551a8b)
					.setAuthor(user.getAsTag(), null, user.getAvatarUrl())
					.setTitle("Enabled Modules")
					.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
					.setDescription(tostring)
					.setTimestamp(Instant.now());
			channel.sendMessage(embed.build()).queue();
			return;
		}
		CommandCategory type;
		try {
			type = CommandCategory.valueOf(subarg.toUpperCase());
		} catch (IllegalArgumentException illegalArgumentException) {
			return;
		}
		String name = StringUtils.capitalize(type.toString().toLowerCase());
		ArrayList<Command> commands = Bean.getInstance().commandHandler.getRegisteredCommands(guild.getIdLong()).stream().filter(command -> command.getCommandCategory() == type).collect(Collectors.toCollection(ArrayList::new));
		if(commands.size() == 0)
		{
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(0x551a8b)
					.setTitle("Help - "+name)
					.setDescription("I haven't found any commands")
					.setTimestamp(Instant.now());
			channel.sendMessage(builder.build()).queue();
			return;
		}
		StringBuilder sb = new StringBuilder();
		for(Command command : commands)
		{
			sb.append("`").append(Prefix).append(command.getUsage()).append("` - ").append(command.getDescription()).append("\n");
		}
		String toString = sb.toString().trim();
		EmbedBuilder embed = new EmbedBuilder()
				.setColor(0x551a8b)
				.setTitle("Help - "+name)
				.setTimestamp(Instant.now());
		String description = type.getNotes();
		if(description.length() > 0)
			embed.addField("", description, false);
		embed.addField("", toString,false);
		channel.sendMessage(embed.build()).queue();
	}



}
