package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.Arrays;

public class PostEmbed extends Command

{

	public PostEmbed(JDA jda)
	{
		super(jda);
		this.invoke = "embed";
		this.description = "Posts an embed";
		this.usage = "embed [Text]";
		this.commandType = CommandType.MODERATION;
		this.neededPermissions = Arrays.asList(Permission.MESSAGE_MANAGE);
	}

	@Override
	public void executeCommand(CommandEvent e)
	{
		String[] args = e.getArguments().toStringArray();
		e.getMessage().delete().complete();
		Member m = e.getMember();
		User u = e.getAuthor();
		TextChannel c = e.getChannel();
		if(args.length >= 1)
		{
			StringBuilder sb = new StringBuilder();
			for (String arg : args)
			{
				sb.append(arg).append(" ");
			}

			String tostring = sb.toString();
			tostring = tostring.substring(0, tostring.length()-1);
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(Color.MAGENTA)
					.setDescription(tostring);
			if(!m.hasPermission(Permission.ADMINISTRATOR))
				builder.setFooter("Submitted by "+u.getAsTag());
			c.sendMessage(builder.build()).queue();
		}
		
	}

}
