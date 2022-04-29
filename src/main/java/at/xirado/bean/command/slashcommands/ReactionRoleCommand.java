package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.ReactionRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionRoleCommand extends SlashCommand
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionRoleCommand.class);

    public ReactionRoleCommand()
    {
        setCommandData(Commands.slash("reactionrole", "Sets up reaction roles.")
                .addSubcommands(new SubcommandData("create", "Creates reaction roles.")
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Channel which contains the message.", true).setChannelTypes(ChannelType.TEXT))
                        .addOption(OptionType.STRING, "message_id", "ID of the message you want to have the reaction role on.", true)
                        .addOption(OptionType.ROLE, "role", "Role that gets added upon reacting to the message.", true)
                        .addOption(OptionType.STRING, "emote", "Emote/Emoji used for reacting.", true)
                )
                .addSubcommands(new SubcommandData("remove", "Removes reaction roles.")
                        .addOption(OptionType.CHANNEL, "channel", "Channel which contains the message.", true)
                        .addOption(OptionType.STRING, "message_id", "ID of the message you want to have reaction roles removed on.", true)
                )
        );
        addRequiredBotPermissions(Permission.ADMINISTRATOR);
        addRequiredUserPermissions(Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {

        Guild guild = event.getGuild();
        Member bot = guild.getSelfMember();
        String subcommand = event.getSubcommandName();
        if (subcommand == null)
        {
            ctx.reply(ctx.getLocalized("commands.invalid_subcommand")).setEphemeral(true).queue();
            return;
        }
        if (subcommand.equalsIgnoreCase("remove"))
        {
            TextChannel channel = (TextChannel) event.getOption("channel").getAsGuildChannel();
            if (channel == null)
            {
                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.channel_not_exists")).setEphemeral(true).queue();
                return;
            }
            long messageID = 0;
            try
            {
                messageID = Long.parseLong(event.getOption("message_id").getAsString());
            }
            catch (Exception e)
            {
                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.message_invalid")).setEphemeral(true).queue();
                return;
            }
            channel.retrieveMessageById(messageID).queue(
                    (message) ->
                    {
                        ctx.getGuildData().removeReactionRoles(message.getIdLong()).update();
                        message.clearReactions().queue(s ->
                        {
                        }, e ->
                        {
                        });
                        ctx.reply(SlashCommandContext.SUCCESS + " " + ctx.getLocalized("commands.reactionroles.removed_success")).setEphemeral(true).queue();
                    }, new ErrorHandler()
                            .handle(ErrorResponse.UNKNOWN_MESSAGE, (err) -> ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.message_not_exists")).setEphemeral(true).queue())
                            .handle(EnumSet.allOf(ErrorResponse.class), (err) ->
                            {
                                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
                                LOGGER.error("An error occurred while trying to remove reaction roles!", err);
                            })
            );
        }
        else if (subcommand.equalsIgnoreCase("create"))
        {
            TextChannel channel = (TextChannel) event.getOption("channel").getAsGuildChannel();
            if (channel == null)
            {
                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.channel_not_exists")).setEphemeral(true).queue();
                return;
            }
            long messageID = 0;
            try
            {
                messageID = Long.parseLong(event.getOption("message_id").getAsString());
            }
            catch (Exception e)
            {
                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.message_invalid")).setEphemeral(true).queue();
                return;
            }
            channel.retrieveMessageById(messageID).queue(
                    (message) ->
                    {
                        Role role = event.getOption("role").getAsRole();
                        if (!bot.canInteract(role))
                        {
                            ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.cannot_interact_role", role.getAsMention())).setEphemeral(true).queue();
                            return;
                        }
                        String emoticon = event.getOption("emote").getAsString();
                        String emote = emoticon;
                        Pattern pattern = Message.MentionType.EMOTE.getPattern();
                        Matcher matcher = pattern.matcher(emoticon);
                        if (matcher.matches())
                        {
                            emoticon = "emote:" + matcher.group(2);
                            emote = matcher.group(2);
                        }
                        String finalEmote = emote;
                        message.addReaction(emoticon).queue(
                                (success) ->
                                {
                                    ReactionRole reactionRole = new at.xirado.bean.data.ReactionRole(finalEmote, message.getIdLong(), role.getIdLong());
                                    ctx.getGuildData().addReactionRoles(reactionRole).update();
                                    ctx.reply(SlashCommandContext.SUCCESS + " " + ctx.getLocalized("commands.reactionroles.added_success")).setEphemeral(true).queue();
                                },
                                new ErrorHandler()
                                        .handle(ErrorResponse.UNKNOWN_EMOJI, (e) -> ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.emote_invalid")).setEphemeral(true).queue())
                                        .handle(EnumSet.allOf(ErrorResponse.class), (e) ->
                                        {
                                            ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
                                            LOGGER.error("An error occurred while adding reaction-role!", e);
                                        })
                        );
                    },
                    (error) -> ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.message_not_exists")).setEphemeral(true).queue());
        }
    }
}
