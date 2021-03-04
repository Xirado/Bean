package at.xirado.bean.listeners;

import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageReactionRemove extends ListenerAdapter
{

	@Override
	public void onGuildMessageReactionRemoveAll(GuildMessageReactionRemoveAllEvent e)
	{

		Util.doAsynchronously(new Runnable() {
			@Override
			public void run() {
				Long messageid = e.getMessageIdLong();
				DiscordBot.instance.reactionRoleManager.removeAllReactionRoles(messageid);
			}
		});
	}
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e)
	{
		Util.doAsynchronously(new Runnable() {
			@Override
			public void run() {
				try
				{
					if(e.getMember().getUser().isBot()) return;
					Guild g = e.getGuild();
					long id = e.getMessageIdLong();
					TextChannel logchannel = Util.getLogChannel(g);
					ReactionEmote reactionemote = e.getReactionEmote();
					String reacted = reactionemote.getAsReactionCode();
					if(!reactionemote.isEmoji())
						reacted = reactionemote.getEmote().getId();
					Member bot = g.getMember(DiscordBot.instance.jda.getSelfUser());
					Role r = DiscordBot.instance.reactionRoleManager.getRoleIfAvailable(id, reacted);
					if(r != null)
					{
						g.removeRoleFromMember(e.getMember(), r).queue();
					}
					//EMD
					/*if(e.getMember().getUser().isBot()) return;
					Guild g = e.getGuild();
					Long id = e.getMessageIdLong();
					TextChannel logchannel = Util.getLogChannel(g);
					ReactionEmote reactionemote = e.getReactionEmote();
					Member bot = g.getMember(Main.jda.getSelfUser());
					boolean REACTCUSTOM = !reactionemote.isEmoji();
					if(Storage.Reactions.containsKey(id))
					{

						for(Reaction r : Storage.Reactions.get(id))
						{
							String RREmote = r.getEmote();
							Long roleid = r.getRoleID();
							boolean RRCustom = r.isCustom();
							if(RRCustom && REACTCUSTOM)
							{
								Emote emote;
								try
								{
									emote = g.getEmoteById(RREmote);
								} catch (Exception e2)
								{
									if(logchannel != null)
									{
										logchannel.sendMessage(Util.SimpleEmbed(Color.red, "One of your emotes used in a Reaction role in "+e.getChannel().getAsMention()+" is no longer available!")).queue();
									}
									return;
								}
								Long emoteid = reactionemote.getEmote().getIdLong();
								Long RREmoteid = emote.getIdLong();
								if(!emoteid.equals(RREmoteid))
								{
									continue;
								}
								final Role role;
								try
								{
									role = g.getRoleById(roleid);
								} catch (Exception e2)
								{
									if(logchannel != null)
									{
										logchannel.sendMessage(Util.SimpleEmbed(Color.red, "One of your roles used in a Reaction role in "+e.getChannel().getAsMention()+" is no longer available!")).queue();
									}
									return;
								}
								if(bot.canInteract(role) && bot.hasPermission(Permission.MANAGE_ROLES))
								{
									g.removeRoleFromMember(e.getMember(), role).queue();
								}
							}else if(!RRCustom && !REACTCUSTOM)
							{
								if(r.getEmote().equalsIgnoreCase(reactionemote.getEmoji()))
								{
									final Role role;
									try
									{
										role = g.getRoleById(roleid);
									} catch (Exception e2)
									{
										if(logchannel != null)
										{
											logchannel.sendMessage(Util.SimpleEmbed(Color.red, "One of your roles used in a Reaction role in "+e.getChannel().getAsMention()+" is no longer available!")).queue();
										}

										return;
									}
									if(bot.canInteract(role) && bot.hasPermission(Permission.MANAGE_ROLES))
									{
										g.removeRoleFromMember(e.getMember(), role).queue();
									}
								}
							}
						}
					}*/
				} catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
		});


	}
}
