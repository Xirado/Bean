package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.ReactionHelper;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;

public class ReactionRole extends Command
{

	public ReactionRole(JDA jda)
	{
		super(jda);
		this.invoke = "rr";
		this.description = "Adds reaction roles to messages";
		this.usage = "rr [#TextChannel] [Message-ID] [@Role] [Emoji] || rr clear [#TextChannel] [Message-ID]";
		this.neededPermissions = Arrays.asList(Permission.ADMINISTRATOR);
		this.commandType = CommandType.ADMIN;
		this.aliases = Arrays.asList("reactionrole", "reactionroles", "createreactionrole");
	}

	@Override
	public void executeCommand(CommandEvent e)
	{
		String[] args = e.getArguments().toStringArray();

		Guild guild = e.getGuild();
		Member bot = e.getSelfMember();
		TextChannel channel = e.getChannel();
		if(!bot.hasPermission(Permission.MANAGE_ROLES))
		{
			channel.sendMessage(Util.SimpleEmbed(Color.red, "I don't have the permission to do this")).queue();
			return;
		}
		if(args.length == 3 && args[0].equalsIgnoreCase("clear"))
		{
			TextChannel targetchannel = null;
			try
			{
				targetchannel = e.getMessage().getMentionedChannels().get(0);
			} catch (Exception e2)
			{
				channel.sendMessage(Util.SimpleEmbed(Color.red, "Invalid Channel!")).queue();
				return;
			}
			if(targetchannel.getGuild().getIdLong() != guild.getIdLong())
			{
				return;
			}
			targetchannel.retrieveMessageById(args[2]).queue(
					(response) ->
					{
						ReactionHelper.removeAllReactions(response.getIdLong());
						response.clearReactions().queue(
								(r)->
								{
									
									channel.sendMessage(Util.SimpleEmbed(Color.green, "All reactions have been removed!")).queue();
									return;
								});
						
					},
					(error) ->
					{
						channel.sendMessage(Util.SimpleEmbed(Color.red, "Invalid Message-ID!")).queue();
						return;
					}
					
					);
		}
		if(args.length != 4)
		{
			e.replyErrorUsage();
			return;
		}
		if(e.getMessage().getMentionedChannels().size() != 1)
		{
			e.replyErrorUsage();
			return;
		}
		if(e.getMessage().getMentionedRoles().size() != 1)
		{
			e.replyErrorUsage();
			return;
		}
		TextChannel targetchannel = e.getMessage().getMentionedChannels().get(0);

		targetchannel.retrieveMessageById(args[1]).queue(
				(result)->
				{
					Role targetrole = null;
					targetrole = e.getMessage().getMentionedRoles().get(0);
					if(!bot.canInteract(targetrole))
					{
						channel.sendMessage(Util.SimpleEmbed(Color.red, "I cannot interact with this role!")).queue();
						return;
					}
					String emoticon = args[3];
					final Role targetRole = targetrole;
					if(e.getMessage().getEmotes().size() == 0)
					{
						result.addReaction(emoticon).queue(
								(success) ->
								{
									ReactionHelper.addReaction(result.getIdLong(),emoticon,targetRole.getIdLong());
								}
						);
					}else
					{
						result.addReaction(e.getMessage().getEmotes().get(0)).queue(
								(success) ->
								{
									ReactionHelper.addReaction(result.getIdLong(),e.getMessage().getEmotes().get(0).getId(),targetRole.getIdLong());
								}
						);
					}
				},
				new ErrorHandler()
						.handle(
								EnumSet.allOf(ErrorResponse.class),
								(ex) -> {
									ex.printStackTrace();
									channel.sendMessage("An error occured!\n```"+ex.getErrorResponse().toString()+"\n"+ex.getMeaning()+"```").queue();
								}
						)
				);
		
	}
}
