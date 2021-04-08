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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionRole extends SlashCommand
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ReactionRole.class);

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
			ctx.reply(ctx.getLocalized("commands.invalid_subcommand")).setEphemeral(true).queue();
			return;
		}
		if(subcommand.equalsIgnoreCase("remove"))
		{
			TextChannel channel = (TextChannel) event.getOption("channel").getAsChannel();
			if(channel == null)
			{
				ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.channel_not_exists")).setEphemeral(true).queue();
				return;
			}
			long messageID = 0;
			try
			{
				messageID = Long.parseLong(event.getOption("message_id").getAsString());
			} catch (Exception e)
			{
				ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.message_invalid")).setEphemeral(true).queue();
				return;
			}
			channel.retrieveMessageById(messageID).queue(
					(message) ->
					{
						ReactionHelper.removeAllReactions(message.getIdLong());
						message.clearReactions().queue(s -> {}, e -> {});
						ctx.reply(CommandContext.SUCCESS+" "+ctx.getLocalized("commands.reactionroles.removed_success")).setEphemeral(true).queue();
					}, new ErrorHandler()
							.handle(ErrorResponse.UNKNOWN_MESSAGE, (err) -> ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.message_not_exists")).setEphemeral(true).queue())
							.handle(EnumSet.allOf(ErrorResponse.class), (err) -> {
								ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
								LOGGER.error("An error occured whilst trying to remove reaction-roles!", err);
							})
			);
		}else if(subcommand.equalsIgnoreCase("create"))
		{
			TextChannel channel = (TextChannel) event.getOption("channel").getAsChannel();
			if(channel == null)
			{
				ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.channel_not_exists")).setEphemeral(true).queue();
				return;
			}
			long messageID = 0;
			try
			{
				messageID = Long.parseLong(event.getOption("message_id").getAsString());
			} catch (Exception e)
			{
				ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.message_invalid")).setEphemeral(true).queue();
				return;
			}
			channel.retrieveMessageById(messageID).queue(
					(message) ->
					{
						Role role = event.getOption("role").getAsRole();
						if(!bot.canInteract(role))
						{
							ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.cannot_interact_role", role.getAsMention())).setEphemeral(true).queue();
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
									ctx.reply(CommandContext.SUCCESS+" "+ctx.getLocalized("commands.reactionroles.added_success")).setEphemeral(true).queue();
								},
								new ErrorHandler()
								.handle(ErrorResponse.UNKNOWN_EMOJI, (e) -> ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.emote_invalid")).setEphemeral(true).queue())
								.handle(EnumSet.allOf(ErrorResponse.class), (e) -> {
									ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
									LOGGER.error("An error occured whilst adding reaction-role!", e);
								})
						);
					},
					(error) -> ctx.reply(CommandContext.ERROR+" "+ctx.getLocalized("commands.message_not_exists")).setEphemeral(true).queue());
		}
	}
}
