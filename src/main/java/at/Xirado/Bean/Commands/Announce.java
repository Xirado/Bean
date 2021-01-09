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
		this.usage = "announce [#Channel/ID] [Text]";
		this.commandType = CommandType.ADMIN;
	}

	@Override
	public void execute(CommandEvent e)
	{

		String[] args = e.getArguments().getArguments();
		e.getMessage().delete().queue(
				(success) ->
				{
					Guild g = e.getGuild();
					TextChannel c = e.getChannel();
					if(args.length >= 2)
					{
						TextChannel targetchannel;
						String s = args[0].replaceAll("[^0-9]", "");
						targetchannel = e.getGuild().getTextChannelById(s);
						if(targetchannel == null)
						{
							c.sendMessage("Invalid channel-id!").queue();
							return;
						}

						String tostring = e.getArguments().getAsString(1);
						EmbedBuilder builder = new EmbedBuilder()
								.setAuthor(g.getName(), null, g.getIconUrl())
								.setDescription(tostring);
						targetchannel.sendMessage(builder.build()).queue(
								(result) ->
								{
									targetchannel.sendMessage("@everyone").queue(null, Util.handle(e.getChannel()));
								}, Util.handle(c)
						);
					}
				}, Util.handle(e.getChannel())
		);
	}
}
