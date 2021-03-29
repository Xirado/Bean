package at.xirado.bean.listeners;

import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildMessageReactionAdd extends ListenerAdapter
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GuildMessageReactionAdd.class);

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent e)
	{
		try
		{
			if(e.getMember().getUser().isBot()) return;
			Guild g = e.getGuild();
			long id = e.getMessageIdLong();
			ReactionEmote reactionemote = e.getReactionEmote();
			String reacted = reactionemote.isEmoji() ? reactionemote.getAsReactionCode() : reactionemote.getEmote().getId();
			Role r = DiscordBot.instance.reactionRoleManager.getRoleIfAvailable(id, reacted);
			if(r != null)
			{
				g.addRoleToMember(e.getMember(), r).queue();
			}
		} catch (Exception e2)
		{
			LOGGER.error("An error occured whilst executing reaction role event!", e2);
		}
	}
}
