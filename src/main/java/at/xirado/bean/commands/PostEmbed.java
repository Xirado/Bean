package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;

public class PostEmbed extends Command

{

	public PostEmbed()
	{
		super("embed", "Posts an embed", "embed [Text]");
		setCommandCategory(CommandCategory.MODERATION);
		setRequiredPermissions(Permission.MESSAGE_MANAGE);
		setRequiredBotPermissions(Permission.MESSAGE_MANAGE);
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{
		String[] args = context.getArguments().toStringArray();
		event.getMessage().delete().queue();
		Member m = context.getMember();
		User u = event.getAuthor();
		TextChannel c = event.getChannel();
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
