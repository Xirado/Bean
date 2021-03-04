package at.xirado.bean.listeners;

import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;

public class GuildJoin extends ListenerAdapter
{

	@Override
	public void onGuildJoin(GuildJoinEvent e)
	{
		DiscordBot.instance.prefixManager.setPrefix(e.getGuild().getIdLong(), "+");
		if(e.getGuild().getSystemChannel() != null)
		{
			e.getGuild().getSystemChannel().sendMessage(
					new EmbedBuilder()
					.setColor(Color.decode("#FEFEFE"))
					.setDescription("Hi, I'm Bean! Type `+help` to get started!\n\nThings to do ASAP:\n1. Choose a channel where i can log everything i do (`+settings setLogChannel [#Channel]`)\n2. Create (or use existing) muted-role, set permissions, and use `+setmutedrole [@Role]`\n" +
							"3. Tell me which of your roles are allowed to use moderator-commands with `+addmod [@Role]`")
					.setTimestamp(Instant.now())
					.build()
			).queue();
		}
	}
}
