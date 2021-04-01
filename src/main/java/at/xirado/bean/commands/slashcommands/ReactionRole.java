package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.misc.ReactionHelper;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionRole extends SlashCommand
{

	public ReactionRole()
	{
		setCommandData(new CommandUpdateAction.CommandData("reactionrole", "Sets up Reaction-Roles")
				.addSubcommand(new CommandUpdateAction.SubcommandData("create", "create Reaction-Roles")
						.addOption(new CommandUpdateAction.OptionData(Command.OptionType.CHANNEL, "channel", "the channel where the message is in")
							.setRequired(true)
						)
						.addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "message_id", "the id of the message you want to have the Reaction-Role on")
							.setRequired(true)
						)
						.addOption(new CommandUpdateAction.OptionData(Command.OptionType.ROLE, "role", "the role that gets added upon reacting to the message")
							.setRequired(true)
						)
						.addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "emote", "the emote/emoji used for the Reaction-Role")
							.setRequired(true)
						)
				)
				.addSubcommand(new CommandUpdateAction.SubcommandData("remove", "remove Reaction-Roles")
						.addOption(new CommandUpdateAction.OptionData(Command.OptionType.CHANNEL, "channel", "the channel where the message is in")
							.setRequired(true)
						)
						.addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "message_id", "the id of the message you want to have Reaction-Roles removed on")
							.setRequired(true)
						)
				)
		);
		setNeededUserPermissions(Arrays.asList(Permission.ADMINISTRATOR));
		setNeededBotPermissions(Arrays.asList(Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE));
	}

	@Override
	public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
	{

		Guild guild = event.getGuild();
		Member bot = guild.getSelfMember();

		String subcommand = event.getSubcommandName();
		if(subcommand == null){
			ctx.reply("Invalid subcommand!").setEphemeral(true).queue();
			return;
		}
		if(subcommand.equalsIgnoreCase("remove"))
		{
			TextChannel channel = (TextChannel) event.getOption("channel").getAsChannel();
			if(channel == null)
			{
				ctx.reply(CommandContext.ERROR+" This channel does not exist!").setEphemeral(true).queue();
				return;
			}
			long messageID = 0;
			try
			{
				messageID = Long.parseLong(event.getOption("message_id").getAsString());
			} catch (Exception e)
			{
				ctx.reply(CommandContext.ERROR+" This is not a valid message-id!").setEphemeral(true).queue();
			}
			channel.retrieveMessageById(messageID).queue(
					(message) ->
					{
						ReactionHelper.removeAllReactions(message.getIdLong());
						message.clearReactions().queue(s -> {}, e -> {});
						ctx.reply(CommandContext.SUCCESS+" Reaction-Roles have been removed successfully!").setEphemeral(true).queue();
					},
					(error) -> ctx.reply(CommandContext.ERROR+" This message does not exist!").setEphemeral(true).queue());
		}else if(subcommand.equalsIgnoreCase("create"))
		{
			TextChannel channel = (TextChannel) event.getOption("channel").getAsChannel();
			if(channel == null)
			{
				ctx.reply(CommandContext.ERROR+" This channel does not exist!").setEphemeral(true).queue();
				return;
			}
			long messageID = 0;
			try
			{
				messageID = Long.parseLong(event.getOption("message_id").getAsString());
			} catch (Exception e)
			{
				ctx.reply(CommandContext.ERROR+" This is not a valid message-id!").setEphemeral(true).queue();
			}
			channel.retrieveMessageById(messageID).queue(
					(message) ->
					{
						Role role = event.getOption("role").getAsRole();
						if(!bot.canInteract(role))
						{
							ctx.reply(CommandContext.ERROR+" I cannot interact with this role!").setEphemeral(true).queue();
							return;
						}
						String emoticon = event.getOption("emote").getAsString();
						String emote = emoticon;
						Pattern pattern = Message.MentionType.EMOTE.getPattern();
						Matcher matcher = pattern.matcher(emoticon);
						if(matcher.matches())
						{
							emoticon = "emote:"+matcher.group(2);
							emote = matcher.group(2);
						}
						String finalEmote = emote;
						message.addReaction(emoticon).queue(
								(success) ->
								{
									ReactionHelper.addReaction(message.getIdLong(), finalEmote, role.getIdLong());
									ctx.reply(CommandContext.SUCCESS+" The Reaction-Role has been successfully created!").setEphemeral(true).queue();
								},
								new ErrorHandler()
								.handle(ErrorResponse.UNKNOWN_EMOJI, (e) -> ctx.reply(CommandContext.ERROR+" The emote you entered is not valid!").setEphemeral(true).queue())
								.handle(EnumSet.allOf(ErrorResponse.class), (e) -> ctx.reply(CommandContext.ERROR+" An error occured! "+e.getMessage()).setEphemeral(true).queue())
						);
					},
					(error) -> ctx.reply(CommandContext.ERROR+" This message does not exist!").setEphemeral(true).queue());
		}
	}
}
