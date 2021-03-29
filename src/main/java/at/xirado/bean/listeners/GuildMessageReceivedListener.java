package at.xirado.bean.listeners;

import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class GuildMessageReceivedListener extends ListenerAdapter
{

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		if(e.getGuild().getIdLong() == 687748771760832551L)
		{
			if(e.getMessage().getContentRaw().startsWith("<@!622849062600704030>") || e.getMessage().getContentRaw().startsWith("<@622849062600704030>"))
			{
				try
				{
					URL url = new URL("https://bean.bz/images/notoracism.png");
					BufferedImage img = ImageIO.read(url);
					File file = new File("image.png"); // change the '.jpg' to whatever extension the image has
					ImageIO.write(img, "png", file); // again, change 'jpg' to the correct extension
					e.getChannel().sendMessage("Ich fühle mich gleich angegriffen wenn jemand was gegen schwarze sagt. BITTE vorsichtig sein").addFile(file).queue();
				} catch (IOException ioException)
				{
					ioException.printStackTrace();
				}

			}
		}
		User author = e.getAuthor();
		e.getGuild().retrieveMemberById(author.getIdLong()).queue(
				(member) ->
				{
					
					String message = e.getMessage().getContentRaw();
					String prefix = DiscordBot.instance.prefixManager.getPrefix(e.getGuild().getIdLong());
					String[] args = message.split(" ");
					if(!author.isBot() && args.length == 1 && e.getMessage().getMentionedUsers().contains(DiscordBot.instance.jda.getSelfUser()) && e.getMessage().getReferencedMessage() == null)
					{
						e.getMessage().reply("<a:ping:818580038949273621> My prefix is `"+prefix+"`").mentionRepliedUser(false).queue();
						return;
					}
					if (!author.isBot() && !e.getMessage().isWebhookMessage() && message.startsWith(prefix)) DiscordBot.instance.commandManager.handleCommand(e, member);
					if(!e.getMessage().isWebhookMessage() && !author.isBot())
					{
						if(!member.hasPermission(Permission.MESSAGE_MANAGE))
						{
							ArrayList<String> blacklistedWords = DiscordBot.instance.blacklistManager.getBlacklistedWords(e.getGuild().getIdLong());
							if(blacklistedWords == null || blacklistedWords.isEmpty()) return;
							for(String s : blacklistedWords)
							{
								if(StringUtils.containsIgnoreCase(message, s))
								{
									e.getMessage().delete().queue(
											(response) ->
													e.getChannel().sendMessage("Hey "+member.getAsMention()+", you can't say that!").queue(s1 -> s1.delete().queueAfter(5, TimeUnit.SECONDS), e1 -> {})
											, error -> {}
									);
									break;
								}
							}
						}
					}
				},
				(error) -> {}
		);
	}
}
