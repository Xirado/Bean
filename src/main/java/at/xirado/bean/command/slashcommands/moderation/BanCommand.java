/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BanCommand extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);

    public BanCommand() {
        setCommandData(Commands.slash("ban", "Permanently bans a user from a server.")
                .addOptions(new OptionData(OptionType.USER, "user", "User to ban.")
                        .setRequired(true)
                )
                .addOptions(new OptionData(OptionType.STRING, "reason", "Reason for the ban."))
                .addOptions(new OptionData(OptionType.INTEGER, "del_days", "How many days of messages to delete."))
        );
        addRequiredBotPermissions(Permission.BAN_MEMBERS);
        addRequiredUserPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        Member sender = event.getMember();
        Guild guild = event.getGuild();
        if (guild == null) {
            LOGGER.error("Received /ban command with empty guild!");
            return;
        }
        User targetUser = event.getOption("user").getAsUser();
        Member targetMember = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") == null ? null : event.getOption("reason").getAsString();
        int delDays = event.getOption("del_days") != null ? (int) Math.max(0, Math.min(7, event.getOption("del_days").getAsLong())) : 0;
        if (targetMember != null) {
            if (sender.getIdLong() == targetMember.getIdLong()) {
                ctx.reply(EmbedUtil.errorEmbed(ctx.getLocalized("commands.ban.cannot_ban_self"))).setEphemeral(true).queue();
                return;
            }
            if (!sender.canInteract(targetMember)) {
                ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.you_cannot_ban_this_member"))).setEphemeral(true).queue();
                return;
            }
            if (ctx.getGuildData().isModerator(targetMember)) {
                ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.cannot_ban_moderator"))).setEphemeral(true).queue();
                return;
            }
            if (!guild.getSelfMember().canInteract(targetMember)) {
                ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.ban.i_cannot_ban_this_member"))).setEphemeral(true).queue();
                return;
            }
        }
        if (reason != null && reason.length() > 256) {
            ctx.reply(EmbedUtil.errorEmbed(ctx.getLocalized("commands.ban.reason_too_long")))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        String reasonString = reason == null ? ctx.getLocalized("commands.noreason") : reason;
        EmbedBuilder dmEmbed = new EmbedBuilder()
                .setColor(CaseType.BAN.getEmbedColor())
                .setAuthor(ctx.getLocalized("commands.ban.you_have_been_banned", guild.getName()), null, guild.getIconUrl())
                .addField("Moderator", sender.getAsMention() + " (" + sender.getUser().getAsTag() + ")", true);
        if (reason != null)
            dmEmbed.addField(ctx.getLocalized("commands.reason"), reasonString, true);
        event.deferReply(true)
                .flatMap(hook -> targetUser.openPrivateChannel())
                .flatMap((c) -> c.sendMessageEmbeds(dmEmbed.build()))
                .mapToResult()
                .flatMap((result) -> guild.ban(targetUser, delDays, TimeUnit.DAYS).reason(reason))
                .queue((x) ->
                {
                    ModCase modCase = ModCase.createModCase(CaseType.BAN, guild.getIdLong(), targetUser.getIdLong(), sender.getIdLong(), reason);
                    MessageEmbed confirmationEmbed = new EmbedBuilder()
                            .setColor(EmbedUtil.SUCCESS_COLOR)
                            .setAuthor(ctx.getLocalized("commands.ban.has_been_banned", targetUser.getAsTag()), null, targetUser.getEffectiveAvatarUrl())
                            .addField(ctx.getLocalized("commands.reason"), reasonString, true)
                            .addField(ctx.getLocalized("commands.duration"), "∞", true)
                            .build();
                    event.getHook().sendMessageEmbeds(confirmationEmbed).queue();

                    Long logChannelId = ctx.getGuildData().getLogChannel();
                    GuildMessageChannel logChannel = logChannelId == null
                            ? null
                            : event.getGuild().getChannelById(GuildMessageChannel.class, logChannelId);

                    if (logChannel != null) {
                        MessageEmbed logEmbed = new EmbedBuilder()
                                .setColor(CaseType.BAN.getEmbedColor())
                                .setAuthor("Ban • " + targetUser.getAsTag(), null, targetUser.getEffectiveAvatarUrl())
                                .addField(ctx.getLocalized("commands.reason"), reasonString, true)
                                .addField("Moderator", sender.getAsMention() + " (" + sender.getUser().getAsTag() + ")", true)
                                .addField(ctx.getLocalized("commands.duration"), "∞", true)
                                .setFooter(ctx.getLocalized("commands.user_id", targetUser.getIdLong()))
                                .build();
                        logChannel.sendMessageEmbeds(logEmbed).queue();
                    }
                }, e -> event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed(ctx.getLocalized("general.unknown_error_occured"))).setEphemeral(true).queue());
    }
}
