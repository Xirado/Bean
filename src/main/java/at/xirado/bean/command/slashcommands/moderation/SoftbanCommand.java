package at.xirado.bean.command.slashcommands.moderation;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.moderation.CaseType;
import at.xirado.bean.moderation.ModCase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftbanCommand extends SlashCommand
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);

    public SoftbanCommand()
    {
        setCommandData(Commands.slash("softban", "Kicks a member and deletes previously written messages.")
                .addOption(OptionType.USER, "member", "Member to kick.", true)
                .addOption(OptionType.STRING, "reason", "Reason for the kick.", false)
        );

        setRequiredUserPermissions(Permission.BAN_MEMBERS);
        setRequiredBotPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild guild = event.getGuild();
        if (guild == null)
        {
            LOGGER.error("Received /softban command with empty guild!");
            return;
        }
        OptionMapping memberOption = event.getOption("member");
        if (memberOption.getAsMember() == null)
        {
            ctx.replyError(ctx.getLocalized("commands.user_not_in_guild")).setEphemeral(true).queue();
            return;
        }
        String reason = event.getOption("reason") == null ? null : event.getOption("reason").getAsString();
        String reasonString = reason == null ? ctx.getLocalized("commands.noreason") : reason;
        Member member = memberOption.getAsMember();
        if (sender.getIdLong() == member.getIdLong())
        {
            ctx.reply(EmbedUtil.errorEmbed(ctx.getLocalized("commands.ban.cannot_ban_self"))).setEphemeral(true).queue();
            return;
        }
        if (!sender.canInteract(member))
        {
            ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.you_cannot_ban_this_member"))).setEphemeral(true).queue();
            return;
        }
        if (ctx.getGuildData().isModerator(member))
        {
            ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.cannot_ban_moderator"))).setEphemeral(true).queue();
            return;
        }
        if (!guild.getSelfMember().canInteract(member))
        {
            ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.i_cannot_ban_this_member"))).setEphemeral(true).queue();
            return;
        }
        EmbedBuilder dmEmbed = new EmbedBuilder()
                .setColor(CaseType.SOFTBAN.getEmbedColor())
                .setAuthor(ctx.getLocalized("commands.softban.you_have_been_softbanned", guild.getName()), null, guild.getIconUrl())
                .addField("Moderator", sender.getAsMention() + " (" + sender.getUser().getAsTag() + ")", true);
        if (reason != null)
            dmEmbed.addField(ctx.getLocalized("commands.reason"), reasonString, true);
        event.deferReply(true)
                .flatMap(hook -> member.getUser().openPrivateChannel())
                .flatMap(channel -> channel.sendMessageEmbeds(dmEmbed.build()))
                .mapToResult()
                .flatMap(message -> guild.ban(member, 7))
                .flatMap(x -> guild.unban(member.getId()))
                .queue(x ->
                {
                    ModCase.createModCase(CaseType.SOFTBAN, guild.getIdLong(), member.getIdLong(), sender.getIdLong(), reason);
                    MessageEmbed confirmationEmbed = new EmbedBuilder()
                            .setColor(EmbedUtil.SUCCESS_COLOR)
                            .setAuthor(ctx.getLocalized("commands.softban.has_been_softbanned", member.getUser().getAsTag()), null, member.getUser().getEffectiveAvatarUrl())
                            .addField(ctx.getLocalized("commands.reason"), reasonString, true)
                            .build();
                    event.getHook().sendMessageEmbeds(confirmationEmbed).queue();
                    if (ctx.getGuildData().getLogChannel() != null)
                    {
                        TextChannel logChannel = ctx.getGuildData().getLogChannel();
                        MessageEmbed logEmbed = new EmbedBuilder()
                                .setColor(CaseType.SOFTBAN.getEmbedColor())
                                .setAuthor("Softban • " + member.getUser().getAsTag(), null, member.getUser().getEffectiveAvatarUrl())
                                .addField(ctx.getLocalized("commands.reason"), reasonString, true)
                                .addField("Moderator", sender.getAsMention() + " (" + sender.getUser().getAsTag() + ")", true)
                                .setFooter(ctx.getLocalized("commands.user_id", member.getIdLong()))
                                .build();
                        logChannel.sendMessageEmbeds(logEmbed).queue(s ->
                        {
                        }, e ->
                        {
                        });
                    }
                }, e -> event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed(ctx.getLocalized("general.unknown_error_occured"))).setEphemeral(true).queue());
    }
}
