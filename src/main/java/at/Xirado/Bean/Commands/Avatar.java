package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

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
	public void executeCommand(CommandEvent e)
	{
		String[] args = e.getArguments().getArguments();
		if(args.length < 1)
		{
			EmbedBuilder b = new EmbedBuilder()
					.setImage(e.getAuthor().getEffectiveAvatarUrl())
					.setColor(Color.magenta)
					.setTimestamp(Instant.now())
					.setTitle(e.getAuthor().getAsTag()+"'s avatar");
			e.getChannel().sendMessage(b.build()).queue();
			return;
		}
		String ID = args[0].replaceAll("[^0-9]", "");
		DiscordBot.instance.jda.retrieveUserById(ID).queue(
				(target) ->
				{
					EmbedBuilder b = new EmbedBuilder()
							.setImage(target.getEffectiveAvatarUrl())
							.setColor(Color.magenta)
							.setTimestamp(Instant.now())
							.setTitle(target.getAsTag()+"'s avatar");
					e.getChannel().sendMessage(b.build()).queue();
				},
				(error) ->
						e.replyError("You provided an invalid user-id.")
		);
	}
}
