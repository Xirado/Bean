package at.Xirado.Bean.Listeners;

import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMessageReactionAdd extends ListenerAdapter
{

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent e)
	{
		Util.runAsync(new Runnable() {

			@Override
			public void run() {
				try
				{
					if(e.getMember().getUser().isBot()) return;
					Guild g = e.getGuild();
					Long id = e.getMessageIdLong();
					TextChannel logchannel = Util.getLogChannel(g);
					ReactionEmote reactionemote = e.getReactionEmote();
					String reacted = reactionemote.getAsReactionCode();
					if(!reactionemote.isEmoji())
						reacted = reactionemote.getEmote().getId();
					Member bot = g.getMember(DiscordBot.instance.jda.getSelfUser());
					Role r = DiscordBot.instance.reactionRoleManager.getRoleIfAvailable(id, reacted);
					if(r != null)
					{
						g.addRoleToMember(e.getMember(), r).queue();
					}
				} catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
		});
	}
}
