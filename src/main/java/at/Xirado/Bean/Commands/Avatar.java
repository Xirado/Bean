package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.time.Instant;

public class Avatar extends Command
{

	public Avatar(JDA jda)
	{
		super(jda);
		this.invoke = "avatar";
		this.usage = "avatar [@User/ID]";
		this.description = "Gets the avatar of a user";
		this.commandType = CommandType.FUN;
		this.aliases = new String[]{"getavatar"};
	}

	@Override
	public void execute(CommandEvent e)
	{
		String[] args = e.getArguments().getArguments();
		Member member = e.getMember();
		if(args.length < 1)
		{
			EmbedBuilder b = new EmbedBuilder()
					.setImage(e.getAuthor().getEffectiveAvatarUrl())
					.setColor(Color.magenta)
					.setFooter("Developed by Xirado")
					.setTimestamp(Instant.now())
					.setTitle(e.getAuthor().getAsTag()+"'s avatar");
			e.getChannel().sendMessage(b.build()).queue();
			return;
		}
		String arg0 = args[0];
		String ID = arg0.replaceAll("[^0-9]", "");
		DiscordBot.instance.jda.retrieveUserById(ID).queue(
				(target) ->
				{
					EmbedBuilder b = new EmbedBuilder()
							.setImage(target.getEffectiveAvatarUrl())
							.setColor(Color.magenta)
							.setFooter("Developed by Xirado")
							.setTimestamp(Instant.now())
							.setTitle(target.getAsTag()+"'s avatar");
					e.getChannel().sendMessage(b.build()).queue();

				});
	}
}
