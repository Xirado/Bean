package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.ReactionRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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

public class ReactionRoleCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionRoleCommand.class);

    public ReactionRoleCommand() {
        setCommandData(Commands.slash("reactionrole", "Sets up reaction roles.")
                .addSubcommands(new SubcommandData("create", "Creates reaction roles.")
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Channel which contains the message.", true).setChannelTypes(ChannelType.TEXT))
                        .addOption(OptionType.STRING, "message_id", "ID of the message you want to have the reaction role on.", true)
                        .addOption(OptionType.ROLE, "role", "Role that gets added upon reacting to the message.", true)
                        .addOption(OptionType.STRING, "emote", "Emote/Emoji used for reacting.", true)
                )
                .addSubcommands(new SubcommandData("remove", "Removes reaction roles.")
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Channel which contains the message.", true).setChannelTypes(ChannelType.TEXT))
                        .addOption(OptionType.STRING, "message_id", "ID of the message you want to have reaction roles removed on.", true)
                )
        );
        addRequiredUserPermissions(Permission.ADMINISTRATOR);
        addRequiredBotPermissions(Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {

        Guild guild = event.getGuild();
        Member bot = guild.getSelfMember();
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            ctx.reply(ctx.getLocalized("commands.invalid_subcommand")).setEphemeral(true).queue();
            return;
        }
        if (subcommand.equalsIgnoreCase("remove")) {
            TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
            if (channel == null) {
                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.channel_not_exists")).setEphemeral(true).queue();
                return;
            }
            long messageID = 0;
            try {
                messageID = Long.parseLong(event.getOption("message_id").getAsString());
            } catch (Exception e) {
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
        } else if (subcommand.equalsIgnoreCase("create")) {
            TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
            if (channel == null) {
                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.channel_not_exists")).setEphemeral(true).queue();
                return;
            }
            long messageID = 0;
            try {
                messageID = Long.parseLong(event.getOption("message_id").getAsString());
            } catch (Exception e) {
                ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.message_invalid")).setEphemeral(true).queue();
                return;
            }
            channel.retrieveMessageById(messageID).queue(
                    (message) ->
                    {
                        Role role = event.getOption("role").getAsRole();
                        if (!bot.canInteract(role)) {
                            ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("commands.cannot_interact_role", role.getAsMention())).setEphemeral(true).queue();
                            return;
                        }
                        String emoticon = event.getOption("emote").getAsString();
                        Emoji emoji;
                        Pattern pattern = Message.MentionType.EMOJI.getPattern();
                        Matcher matcher = pattern.matcher(emoticon);
                        if (matcher.matches()) {
                            emoji = Emoji.fromCustom(matcher.group(1), Long.parseLong(matcher.group(2)), emoticon.startsWith("<a:"));
                        } else {
                            emoji = Emoji.fromUnicode(emoticon);
                        }
                        String saved = emoji.getType() == Emoji.Type.CUSTOM ? ((CustomEmoji) emoji).getId() : emoji.getName();
                        message.addReaction(emoji).queue(
                                (success) ->
                                {
                                    ReactionRole reactionRole = new ReactionRole(saved, message.getIdLong(), role.getIdLong());
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
