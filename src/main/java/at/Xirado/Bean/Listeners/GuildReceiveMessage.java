package at.Xirado.Bean.Listeners;

import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;


public class GuildReceiveMessage extends ListenerAdapter
{

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		Member member = e.getMember();
		String message = e.getMessage().getContentRaw();
		String Prefix = DiscordBot.instance.prefixManager.getPrefix(e.getGuild().getIdLong());
		String[] args = message.split(" ");
		boolean isbot = e.getAuthor().isBot();
		if(!isbot && args.length == 1 && e.getMessage().getMentionedUsers().contains(DiscordBot.instance.jda.getSelfUser()))
		{
			e.getChannel().sendMessage("Hey, I'm Bean! \nType `"+Prefix+"help` to get started!").queue();
			return;
		}
		if (!isbot && !e.getMessage().isWebhookMessage() && message.startsWith(Prefix))
		{

			DiscordBot.instance.commandManager.handleCommand(e);
        }
		if(!e.getMessage().isWebhookMessage() && !isbot)
		{
        	if(!member.hasPermission(Permission.MESSAGE_MANAGE))
        	{
				for(String s : DiscordBot.instance.blacklistManager.getBlacklistedWords(e.getGuild().getIdLong()))
				{
					if(StringUtils.containsIgnoreCase(message, s))
					{
						e.getMessage().delete().queue(
								(response1) ->
								{
									e.getChannel().sendMessage(Util.BadWord(member, e.getGuild())).queue(
											(response) ->
											{
												if(DiscordBot.instance.logChannelManager.getLogChannel(e.getGuild().getIdLong()) != null)
												{
													DiscordBot.instance.logChannelManager.getLogChannel(e.getGuild().getIdLong()).sendMessage(
															new EmbedBuilder()
																	.setColor(Color.orange)
																	.setAuthor("Blacklisted word used", null, e.getGuild().getIconUrl())
																	.setFooter("UserID: "+e.getMember().getIdLong())
																	.addField("User", e.getMember().getAsMention(), true)
																	.addField("Channel", e.getChannel().getAsMention(), true)
																	.addField("Message", message, true)
																	.setTimestamp(Instant.now())
																	.build()
													).queue(null, Throwable::printStackTrace);
												}
												response.delete().queueAfter(5, TimeUnit.SECONDS);
											}
									);
								});
					}
				}
        	}
		}
	}
}
