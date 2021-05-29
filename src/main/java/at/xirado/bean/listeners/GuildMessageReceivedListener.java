package at.xirado.bean.listeners;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
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
		if(e.isWebhookMessage() || e.getAuthor().isBot()) return;
		Member member = e.getMember();
		if(member == null) return;
		String message = e.getMessage().getContentRaw();
		String prefix = Bean.instance.prefixManager.getPrefix(e.getGuild().getIdLong());
		String[] args = message.split(" ");
		if(args.length == 1 && e.getMessage().getMentionedUsers().contains(Bean.instance.jda.getSelfUser()) && e.getMessage().getReferencedMessage() == null)
		{
			e.getMessage().reply("<a:ping:818580038949273621> My prefix is `"+prefix+"`").mentionRepliedUser(false).queue();
			return;
		}
		if (message.startsWith(prefix)) {
			Bean.instance.commandHandler.handleCommandFromGuild(e);
			return;
		}
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
}
