package at.Xirado.Bean.Listeners;

import at.Xirado.Bean.Main.DiscordBot;
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
					.setDescription("Hi, I'm Bean! Type `+help` to get started!")
					.setTimestamp(Instant.now())
					.setFooter("Developed by Xirado")
					.build()
			).queue();
		}
	}
}
