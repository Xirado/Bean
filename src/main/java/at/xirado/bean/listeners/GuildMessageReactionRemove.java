package at.xirado.bean.listeners;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildMessageReactionRemove extends ListenerAdapter
{

	private static final Logger LOGGER = LoggerFactory.getLogger(GuildMessageReactionRemove.class);

	@Override
	public void onGuildMessageReactionRemoveAll(GuildMessageReactionRemoveAllEvent e)
	{
		try
		{
			long messageid = e.getMessageIdLong();
			Bean.instance.reactionRoleManager.removeAllReactionRoles(messageid);
		} catch (Exception exception)
		{
			LOGGER.error("An error occured while executing GuildMessageReactionRemoveAllEvent!", exception);
		}
	}

	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e)
	{
		e.getGuild().retrieveMemberById(e.getUserId()).queue(
				(member) ->
				{
					try
					{
						User user = member.getUser();
						if(user.isBot()) return;
						ReactionEmote reactionemote = e.getReactionEmote();
						String reacted = reactionemote.isEmoji() ? reactionemote.getAsReactionCode() : reactionemote.getEmote().getId();
						Role r = Bean.instance.reactionRoleManager.getRoleIfAvailable(e.getMessageIdLong(), reacted);
						if(r != null)
						{
							e.getGuild().removeRoleFromMember(member, r).queue();
						}
					}catch (Exception ex)
					{
						LOGGER.error("Could not remove role from member!", ex);
					}
				},
				(error) -> {}
		);
	}
}
