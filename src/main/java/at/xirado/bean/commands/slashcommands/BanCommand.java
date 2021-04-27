package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.SlashCommand;
import at.xirado.bean.commandmanager.SlashCommandContext;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

public class BanCommand extends SlashCommand
{

    public BanCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("ban", "permanently bans a user from this guild")
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.USER, "user", "the user to ban").setRequired(true))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "reason", "the reason for this ban").setRequired(false))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.INTEGER, "delDays", "how many days of messages to delete").setRequired(false))
        );
        setNeededUserPermissions(Collections.singletonList(Permission.BAN_MEMBERS));
        setNeededBotPermissions(Collections.singletonList(Permission.BAN_MEMBERS));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild g = event.getGuild();
        if(g == null) return;
        User targetUser = event.getOption("user").getAsUser();
        Member targetMember = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") == null ? null : event.getOption("reason").getAsString();
        int deldays = event.getOption("delDays") != null ? (int) Math.max(0, Math.min(7, event.getOption("delDays").getAsLong())) : 0;
        if(targetMember != null)
        {
            if(sender.getIdLong() == targetMember.getIdLong())
            {
                ctx.reply(ctx.getLocalized("commands.ban.cannot_ban_self")).setEphemeral(true).queue();
                return;
            }

            if (!sender.canInteract(targetMember))
            {
                ctx.reply(SlashCommandContext.DENY+" "+ctx.getLocalized("commands.ban.you_cannot_ban_this_member")).setEphemeral(true).queue();
                return;
            }

            if(DiscordBot.getInstance().permissionCheckerManager.isModerator(targetMember))
            {
                event.reply(SlashCommandContext.DENY+" "+ctx.getLocalized("commands.ban.cannot_ban_moderator")).setEphemeral(true).queue();
                return;
            }

            if (!g.getSelfMember().canInteract(targetMember))
            {
                ctx.reply(SlashCommandContext.DENY+" "+ctx.getLocalized("commands.ban.i_cannot_ban_this_member")).setEphemeral(true).queue();
                return;
            }
        }
        Case bancase = Case.createCase(CaseType.BAN, g.getIdLong(), targetUser.getIdLong(), sender.getIdLong(), reason != null ? reason : ctx.getLocalized("commands.noreason"), -1);
        if(bancase == null)
        {
            ctx.reply(SlashCommandContext.ERROR+" "+ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
            return;
        }
        try
        {
            PrivateChannel privateChannel = targetUser.openPrivateChannel().complete();
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.BAN.getEmbedColor())
                    .setAuthor(ctx.getLocalized("commands.ban.you_have_been_banned", g.getName()))
                    .addField(ctx.getLocalized("commands.reason"), bancase.getReason(), true)
                    .addField("Moderator", sender.getAsMention() + "("+sender.getUser().getAsTag()+")", true);
            privateChannel.sendMessage(builder.build()).complete();
        }catch (Exception ignored)
        {

        }
        g.ban(targetUser, deldays, bancase.getReason()).queue(
                (success) ->
                {
                    ctx.reply(SlashCommandContext.SUCCESS+" "+ctx.getLocalized("commands.ban.has_been_banned", targetUser.getAsMention())+"\n`"+ctx.getLocalized("commands.reason")+": "+bancase.getReason()+" (#"+bancase.getCaseID()+")`").setEphemeral(true).queue();
                    TextChannel logchannel = DiscordBot.getInstance().logChannelManager.getLogChannel(g.getIdLong());
                    EmbedBuilder builder2 = new EmbedBuilder()
                            .setTimestamp(Instant.now())
                            .setColor(0x8b0000)
                            .setThumbnail(targetUser.getEffectiveAvatarUrl())
                            .setFooter(ctx.getLocalized("commands.target_id")+": "+targetUser.getIdLong())
                            .setTitle("Ban | Case #"+bancase.getCaseID())
                            .addField(ctx.getLocalized("commands.target"), targetUser.getAsMention()+" ("+targetUser.getAsTag()+")", true)
                            .addField("Moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true)
                            .addField(ctx.getLocalized("commands.reason"), bancase.getReason(), false);
                    Objects.requireNonNullElseGet(logchannel, event::getChannel).sendMessage(builder2.build()).queue();
                },
                (error) ->
                {
                    ctx.reply(SlashCommandContext.ERROR + " "+ctx.getLocalized("general.unknown_error_occured")).setEphemeral(true).queue();
                }
        );
    }
}
