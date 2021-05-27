package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.IOException;


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
		final Message.Attachment attachment = event.getMessage().getAttachments().size() > 0 ? event.getMessage().getAttachments().get(0) : null;
		event.getMessage().delete().queue();
		Member member = context.getMember();
		User author = event.getAuthor();
		TextChannel channel = event.getChannel();
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
			boolean upload = false;
			if(attachment != null && attachment.isImage())
			{
				builder.setImage("attachment://image."+attachment.getFileExtension());
				upload = true;
			}
			if(!member.hasPermission(Permission.ADMINISTRATOR))
				builder.setFooter("Submitted by "+author.getAsTag());
			if(upload)
			{
				attachment.retrieveInputStream().thenAcceptAsync(
						inputStream ->
						{
							try
							{
								byte[] data = IOUtils.toByteArray(inputStream);
								channel.sendFile(data, "image."+attachment.getFileExtension()).embed(builder.build()).queue();
							} catch (IOException ignored) {}
						}
						, Bean.getInstance().scheduledExecutorService
				);
				return;
			}
			channel.sendMessage(builder.build()).queue();
		}
		
	}

}
