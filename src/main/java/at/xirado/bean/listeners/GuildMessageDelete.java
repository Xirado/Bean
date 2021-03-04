package at.xirado.bean.listeners;

import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageDelete extends ListenerAdapter
{

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e)
	{
		DiscordBot.instance.reactionRoleManager.removeAllReactionRoles(e.getMessageIdLong());
	}
}
