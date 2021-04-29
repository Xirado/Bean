package at.xirado.bean.listeners;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class GuildMessageReceivedListener extends ListenerAdapter
{

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		User author = e.getAuthor();
		e.getGuild().retrieveMemberById(author.getIdLong()).queue(
				(member) ->
				{
					
					String message = e.getMessage().getContentRaw();
					String prefix = Bean.instance.prefixManager.getPrefix(e.getGuild().getIdLong());
					String[] args = message.split(" ");
					if(!author.isBot() && args.length == 1 && e.getMessage().getMentionedUsers().contains(Bean.instance.jda.getSelfUser()) && e.getMessage().getReferencedMessage() == null)
					{
						e.getMessage().reply("<a:ping:818580038949273621> My prefix is `"+prefix+"`").mentionRepliedUser(false).queue();
						return;
					}
					if (!author.isBot() && !e.getMessage().isWebhookMessage() && message.startsWith(prefix)) {
						Bean.instance.commandHandler.handleCommandFromGuild(e, member);
					}
					if(!e.getMessage().isWebhookMessage() && !author.isBot())
					{
						if(!member.hasPermission(Permission.MESSAGE_MANAGE))
						{
							ArrayList<String> blacklistedWords = Bean.instance.blacklistManager.getBlacklistedWords(e.getGuild().getIdLong());
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
