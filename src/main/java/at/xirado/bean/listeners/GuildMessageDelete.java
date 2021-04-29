package at.xirado.bean.listeners;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageDelete extends ListenerAdapter
{

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e)
	{
		Bean.instance.reactionRoleManager.removeAllReactionRoles(e.getMessageIdLong());
	}
}
