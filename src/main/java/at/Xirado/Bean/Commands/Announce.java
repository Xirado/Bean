package at.Xirado.Bean.Commands;


import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class Announce extends Command
{

	public Announce(JDA jda)
	{
		super(jda);
		this.invoke = "announce";
		this.neededPermissions = new Permission[]{Permission.MESSAGE_MENTION_EVERYONE};
		this.description = "Creates an announcement in a channel";
		this.usage = "announce #Channel [Text]";
		this.commandType = CommandType.ADMIN;
	}

	@Override
	public void executeCommand(CommandEvent e)
	{

		String[] args = e.getArguments().getArguments();
		e.getMessage().delete().queue();
		Guild g = e.getGuild();
		TextChannel c = e.getChannel();
		if(args.length < 2)
		{
			e.replyErrorUsage();
			return;
		}
		TextChannel targetChannel = e.getGuild().getTextChannelById(args[0].replaceAll("[^0-9]", ""));
		if(targetChannel == null)
		{
			e.replyError("Invalid channel!");
			return;
		}
		String message = e.getArguments().getAsString(1);
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
