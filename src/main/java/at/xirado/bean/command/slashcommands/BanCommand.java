package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.moderation.Case;
import at.xirado.bean.moderation.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

public class BanCommand extends SlashCommand
{

    public BanCommand()
    {
        setCommandData(new CommandData("ban", "permanently bans a user from this guild")
                .addOption(OptionType.USER, "user", "the user to ban", true)
                .addOption(OptionType.STRING, "reason", "the reason for this ban", false)
                .addOption(OptionType.INTEGER, "deldays", "how many days of messages to delete", false)
        );
        setRequiredUserPermissions(Permission.BAN_MEMBERS);
        setRequiredBotPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild g = event.getGuild();
        if (g == null) return;
        User targetUser = event.getOption("user").getAsUser();
        Member targetMember = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") == null ? null : event.getOption("reason").getAsString();
        int deldays = event.getOption("delDays") != null ? (int) Math.max(0, Math.min(7, event.getOption("delDays").getAsLong())) : 0;
        if (targetMember != null)
        {
            if (sender.getIdLong() == targetMember.getIdLong())
            {
                ctx.reply(ctx.getLocalized("commands.ban.cannot_ban_self")).setEphemeral(true).queue();
                return;
            }

            if (!sender.canInteract(targetMember))
            {
                ctx.reply(SlashCommandContext.DENY + " " + ctx.getLocalized("commands.ban.you_cannot_ban_this_member")).setEphemeral(true).queue();
                return;
            }

            if (ctx.getGuildData().isModerator(targetMember))
            {
                ctx.reply(SlashCommandContext.DENY + " " + ctx.getLocalized("commands.ban.cannot_ban_moderator")).setEphemeral(true).queue();
                return;
            }

            if (!g.getSelfMember().canInteract(targetMember))
            {
                ctx.reply(SlashCommandContext.DENY + " " + ctx.getLocalized("commands.ban.i_cannot_ban_this_member")).setEphemeral(true).queue();
                return;
            }
        }
        Case bancase = Case.createCase(CaseType.BAN, g.getIdLong(), targetUser.getIdLong(), sender.getIdLong(), reason != null ? reason : ctx.getLocalized("commands.noreason"), -1);
        if (bancase == null)
        {
            ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
            return;
        }
        try
        {
            PrivateChannel privateChannel = targetUser.openPrivateChannel().complete();
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.BAN.getEmbedColor())
                    .setAuthor(ctx.getLocalized("commands.ban.you_have_been_banned", g.getName()))
                    .addField(ctx.getLocalized("commands.reason"), bancase.getReason(), true)
                    .addField("Moderator", sender.getAsMention() + "(" + sender.getUser().getAsTag() + ")", true);
            privateChannel.sendMessage(builder.build()).complete();
        } catch (Exception ignored)
        {

        }
        g.ban(targetUser, deldays, bancase.getReason()).queue(
                (success) ->
                {
                    ctx.reply(SlashCommandContext.SUCCESS + " " + ctx.getLocalized("commands.ban.has_been_banned", targetUser.getAsMention()) + "\n`" + ctx.getLocalized("commands.reason") + ": " + bancase.getReason() + " (#" + bancase.getCaseID() + ")`").setEphemeral(true).queue();
                    TextChannel logchannel = ctx.getGuildData().getLogChannel();
                    EmbedBuilder builder2 = new EmbedBuilder()
                            .setTimestamp(Instant.now())
                            .setColor(0x8b0000)
                            .setThumbnail(targetUser.getEffectiveAvatarUrl())
                            .setFooter(ctx.getLocalized("commands.target_id") + ": " + targetUser.getIdLong())
                            .setTitle("Ban | Case #" + bancase.getCaseID())
                            .addField(ctx.getLocalized("commands.target"), targetUser.getAsMention() + " (" + targetUser.getAsTag() + ")", true)
                            .addField("Moderator", sender.getAsMention() + " (" + sender.getUser().getAsTag() + ")", true)
                            .addField(ctx.getLocalized("commands.reason"), bancase.getReason(), false);
                    Objects.requireNonNullElseGet(logchannel, event::getChannel).sendMessage(builder2.build()).queue();
                },
                (error) ->
                {
                    ctx.reply(SlashCommandContext.ERROR + " " + ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
                }
        );
    }
}
