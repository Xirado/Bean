package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;

public class Settings extends Command
{

	public Settings()
	{
		super("settings", "change guild-specific settings", "settings (subargument)");
		setRequiredPermissions(Permission.MANAGE_SERVER);
		setCommandCategory(CommandCategory.ADMIN);

	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{
		String[] args = context.getArguments().toStringArray();
		Member m = context.getMember();
		Guild g = event.getGuild();
		String Prefix = Bean.instance.prefixManager.getPrefix(g.getIdLong());
		User u = event.getAuthor();
		Member bot = g.getMember(Bean.instance.jda.getSelfUser());
		TextChannel channel = event.getChannel();
		if (args.length == 0)
		{
			EmbedBuilder builder = new EmbedBuilder()
					.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
					.setTitle("» Settings | All Commands")
					.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
					.setDescription("`" + Prefix + "settings setLogChannel [#Channel]`\n"
							+ "Updates the channel where events gets logged\n\n"
							+ "`" + Prefix + "settings setPrefix [Prefix]`\n"
							+ "Updates the Prefix")
					.setTimestamp(Instant.now())
					.setFooter("Settings - All")
					.setColor(Color.cyan);
			channel.sendMessage(builder.build()).queue();
			return;
		}
		if (args[0].equalsIgnoreCase("setLogChannel"))
		{
			if (event.getMessage().getMentionedChannels().size() != 1)
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
						.setTitle("Invalid usage!")
						.setDescription("`" + Prefix + "settings setLogChannel #Channel`")
						.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
						.setTimestamp(Instant.now())
						.setFooter("Invalid usage")
						.setColor(Color.red);
				channel.sendMessage(builder.build()).queue();
				return;
			}
			TextChannel newchannel = event.getMessage().getMentionedChannels().get(0);
			if (newchannel.getGuild().getIdLong() != g.getIdLong())
			{
				return;
			}
			Bean.instance.logChannelManager.setLogChannel(g.getIdLong(), newchannel.getIdLong());
			EmbedBuilder builder = new EmbedBuilder()
					.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
					.setTitle("» Logs | Channel changed")
					.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
					.setDescription("The Logchannel has been changed to " + newchannel.getAsMention() + "!")
					.setTimestamp(Instant.now())
					.setFooter("Logchannel changed")
					.setColor(Color.green);
			channel.sendMessage(builder.build()).queue();
			return;
		} else if (args[0].equalsIgnoreCase("setPrefix"))
		{
			if (args.length != 2)
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
						.setTitle("Invalid usage!")
						.setDescription("`" + Prefix + "settings setPrefix [Prefix]`")
						.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
						.setTimestamp(Instant.now())
						.setFooter("Invalid usage")
						.setColor(Color.red);
				channel.sendMessage(builder.build()).queue();
				return;
			}
			String newprefix = args[1];
			synchronized (this)
			{
				Bean.instance.prefixManager.setPrefix(g.getIdLong(), newprefix);
			}

			EmbedBuilder builder = new EmbedBuilder()
					.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
					.setTitle("Changed Prefix!")
					.setDescription("`" + newprefix + "` is the new Prefix!")
					.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
					.setTimestamp(Instant.now())
					.setFooter("Changed Prefix")
					.setColor(Color.green);
			channel.sendMessage(builder.build()).queue();
			return;
		} else if (args[0].equalsIgnoreCase("setDefaultRole"))
		{
			if (args.length != 2)
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
						.setTitle("Invalid usage!")
						.setDescription("`" + Prefix + "settings setDefaultRole @Role`")
						.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
						.setTimestamp(Instant.now())
						.setFooter("Invalid usage")
						.setColor(Color.red);
				channel.sendMessage(builder.build()).queue();
				return;
			}
			Role role = event.getMessage().getMentionedRoles().get(0);
			if (!bot.canInteract(role))
			{
				EmbedBuilder builder = new EmbedBuilder()
						.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
						.setTitle("Error")
						.setDescription("This role is higher than me!")
						.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
						.setTimestamp(Instant.now())
						.setFooter("Interact Error")
						.setColor(Color.red);
				channel.sendMessage(builder.build()).queue();
				return;
			}

			EmbedBuilder builder = new EmbedBuilder()
					.setAuthor(u.getAsTag(), null, u.getAvatarUrl())
					.setTitle("Default-Role has been set!")
					.setDescription("Every new member that joins will get the role\n" + role.getAsMention() + " automatically!")
					.setThumbnail(Bean.instance.jda.getSelfUser().getAvatarUrl())
					.setTimestamp(Instant.now())
					.setFooter("Updated Default-Role")
					.setColor(Color.green);
			channel.sendMessage(builder.build()).queue();
			return;
		}
	}
}
