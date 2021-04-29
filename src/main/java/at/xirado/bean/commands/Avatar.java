package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;

public class Avatar extends Command
{

	public Avatar()
	{
		super("avatar", "Gets the avatar of a user", "avatar [@user/id]");
		setCommandCategory(CommandCategory.UTILITIES);
		setAliases("getavatar");
	}

	@Override
	public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
	{
		String[] args = context.getArguments().toStringArray();
		if(args.length < 1)
		{
			context.reply(getAvatarEmbed(event.getAuthor(), context));
			return;
		}
		String ID = args[0].replaceAll("[^0-9]", "");
		if(ID.length() == 0)
		{
			context.replyError(context.getLocalized("commands.id_empty"));
			return;
		}
		Bean.instance.jda.retrieveUserById(ID).queue(
				(target) ->
				{
					context.reply(getAvatarEmbed(target, context));
				},
				(error) -> context.reply(getAvatarEmbed(event.getAuthor(), context)));
	}



	private MessageEmbed getAvatarEmbed(User user, CommandContext e)
	{
		return new EmbedBuilder()
				.setImage(user.getEffectiveAvatarUrl()+"?size=512")
				.setColor(Color.magenta)
				.setTimestamp(Instant.now())
				.setAuthor(e.getLocalized("commands.avatar_title", user.getAsTag()), null, user.getEffectiveAvatarUrl())
				.build();
	}

}
