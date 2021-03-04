package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Ban extends Command
{

	public Ban(JDA jda)
	{
		super(jda);
		this.invoke = "ban";
		this.description = "permanently bans a user from the server";
		this.usage = "ban [@User/ID] (Optional Reason)";
		this.neededPermissions = Arrays.asList(Permission.BAN_MEMBERS);
		this.commandType = CommandType.MODERATION;
	}

	@Override
	public void executeCommand(CommandEvent e)
	{
		String[] args = e.getArguments().toStringArray();
		Member member = e.getMember();
		User user = e.getAuthor();
		Guild guild = e.getGuild();
		TextChannel logchannel = DiscordBot.instance.logChannelManager.getLogChannel(guild.getIdLong());
		TextChannel channel = e.getChannel();
		if(args.length < 1)
		{
			e.replyErrorUsage();
			return;
		}

		String ID = args[0].replaceAll("[^0-9]", "");
		DiscordBot.instance.jda.retrieveUserById(ID).queue(
				(targetUser) ->
				{
					if(guild.isMember(targetUser))
					{
						Member asmember = guild.getMember(targetUser);
						if(!member.canInteract(asmember))
						{
							channel.sendMessage(user.getAsMention()+", you can't ban this user!").queue(response -> response.delete().queueAfter(10, TimeUnit.SECONDS));
							return;
						}
						Member bot = guild.getMember(DiscordBot.instance.jda.getSelfUser());
						if(!bot.canInteract(asmember) || !bot.hasPermission(Permission.BAN_MEMBERS)) {
							channel.sendMessage(user.getAsMention()+", I can't ban this user!").queue(response -> response.delete().queueAfter(10, TimeUnit.SECONDS));
							return;
						}
						

					}
					StringBuilder sb = new StringBuilder();
					if(args.length < 2)
					{
						sb.append("No reason specified.");
					}else
					{
						for(int i = 1; i < args.length; i++)
						{
							sb.append(args[i]).append(" ");
						}
					}
					final String Reason = sb.toString().trim();
					String wholeReason = "banned by "+user.getAsTag()+" for: "+Reason;
					boolean ismember = guild.isMember(targetUser);
					
					guild.ban(targetUser, 0, wholeReason).queue(
							(response) -> {
								if(ismember)
								{
									Util.sendPrivateMessage(targetUser, new MessageBuilder("You have been **permanently** banned from "+guild.getName()+" for **"+Reason+"**").build());
								}
								EmbedBuilder builder = new EmbedBuilder()
										.setColor(Color.decode("#8B0000"))
										.setTimestamp(Instant.now())
										.setFooter("ID: "+targetUser.getIdLong())
										.setTitle("[BAN] "+targetUser.getAsTag())
										.addField("User", targetUser.getAsMention(), true)
										.addField("Moderator", user.getAsMention(), true)
										.addField("Reason", Reason, true)
										.addField("Duration", "∞", true)
										.setThumbnail(targetUser.getEffectiveAvatarUrl());
								if(logchannel != null)
								{
									if(logchannel.getIdLong() == channel.getIdLong())
									{
										channel.sendMessage(builder.build()).queue();
										return;
									}
									channel.sendMessage(builder.build()).queue(null, Util.handle(e.getChannel()));
									logchannel.sendMessage(builder.build()).queue(null, Util.handle(e.getChannel()));
								}else {
									channel.sendMessage(builder.build()).queue(null, Util.handle(e.getChannel()));
								}
								return;
							}
							
							);
				}, (err) -> e.replyError("You provided an invalid user-id!")
			);
	}
}
