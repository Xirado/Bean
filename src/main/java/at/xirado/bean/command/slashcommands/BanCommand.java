package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.moderation.CaseType;
import at.xirado.bean.moderation.ModCase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        int delDays = event.getOption("deldays") != null ? (int) Math.max(0, Math.min(7, event.getOption("deldays").getAsLong())) : 0;
        if (targetMember != null)
        {
            if (sender.getIdLong() == targetMember.getIdLong())
            {
                ctx.reply(EmbedUtil.errorEmbed(ctx.getLocalized("commands.ban.cannot_ban_self"))).setEphemeral(true).queue();
                return;
            }
            if (!sender.canInteract(targetMember))
            {
                ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.you_cannot_ban_this_member"))).setEphemeral(true).queue();
                return;
            }
            if (ctx.getGuildData().isModerator(targetMember))
            {
                ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.cannot_ban_moderator"))).setEphemeral(true).queue();
                return;
            }
            if (!g.getSelfMember().canInteract(targetMember))
            {
                ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.i_cannot_ban_this_member"))).setEphemeral(true).queue();
                return;
            }
        }
        String reasonString = reason == null ? ctx.getLocalized("commands.noreason") : reason;
        MessageEmbed dmEmbed = new EmbedBuilder()
                .setColor(CaseType.BAN.getEmbedColor())
                .setAuthor(ctx.getLocalized("commands.ban.you_have_been_banned", g.getName()), null, g.getIconUrl())
                .addField(ctx.getLocalized("commands.reason"), reasonString, true)
                .addField("Moderator", sender.getAsMention() + " (" + sender.getUser().getAsTag() + ")", true)
                .build();
        event.deferReply(true)
                .flatMap(hook -> targetUser.openPrivateChannel())
                .flatMap((c) -> c.sendMessageEmbeds(dmEmbed))
                .mapToResult()
                .flatMap((result) -> g.ban(targetUser, delDays, reason))
                .queue((x) ->
                {
                    ModCase modCase = ModCase.createModCase(CaseType.BAN, g.getIdLong(), targetUser.getIdLong(), sender.getIdLong(), reason);
                    MessageEmbed confirmationEmbed = new EmbedBuilder()
                            .setColor(CaseType.BAN.getEmbedColor())
                            .setAuthor(ctx.getLocalized("commands.ban.has_been_banned", targetUser.getAsTag()), null, targetUser.getEffectiveAvatarUrl())
                            .addField(ctx.getLocalized("commands.reason"), reasonString, true)
                            .setFooter("Case-ID: `"+modCase.getUuid().toString()+"`")
                            .setDescription(ctx.getLocalized("commands.user_id", targetUser.getIdLong()))
                            .build();
                    event.getHook().sendMessageEmbeds(confirmationEmbed).queue();
                    if (ctx.getGuildData().getLogChannel() != null)
                    {
                        TextChannel logChannel = ctx.getGuildData().getLogChannel();
                        MessageEmbed logEmbed = new EmbedBuilder()
                                .setColor(CaseType.BAN.getEmbedColor())
                                .setAuthor("Ban | "+targetUser.getAsTag(), null, targetUser.getEffectiveAvatarUrl())
                                .addField(ctx.getLocalized("commands.reason"), reasonString, true)
                                .addField("Moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true)
                                .setFooter("Case-ID: `"+modCase.getUuid().toString()+"`")
                                .setDescription(ctx.getLocalized("commands.user_id", targetUser.getIdLong()))
                                .build();
                        logChannel.sendMessageEmbeds(logEmbed).queue(s -> {}, e -> {});
                    }
                }, e -> ctx.reply(EmbedUtil.errorEmbed(ctx.getLocalized("general.unknown_error_occured"))).setEphemeral(true).queue());
    }
}
