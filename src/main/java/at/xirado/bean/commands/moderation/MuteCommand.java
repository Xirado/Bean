package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.MutedRoleManager;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.translation.FormattedDuration;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import at.xirado.bean.punishmentmanager.Punishments;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends Command
{
    public MuteCommand(JDA jda)
    {
        super(jda);
        this.invoke = "mute";
        this.commandType = CommandType.MODERATION;
        this.neededBotPermissions = Arrays.asList(Permission.MANAGE_ROLES);
        this.description = "mutes a member";
        this.usage = "mute [@Mention/ID] [duration] (optional reason)";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Member m = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            event.replyError(event.getLocalized("general.no_perms"));
            return;
        }
        Guild g = event.getGuild();
        String[] args = event.getArguments().toStringArray();
        if(args.length < 2)
        {
            event.replyErrorUsage();
            return;
        }
        String targetID = args[0].replaceAll("[^0-9]", "");
        if(targetID.length() == 0)
        {
            event.replyError(event.getLocalized("commands.id_empty"));
            return;
        }
        Long time = FormattedDuration.parsePeriod(args[1]);
        if(time == null)
        {
            event.replyErrorUsage();
            return;
        }
        if(time < 1000)
        {
            event.replyError(event.getLocalized("commands.mute.duration_too_short"));
            return;
        }
        String Reason = args.length > 2 ? event.getArguments().toString(2) : event.getLocalized("commands.noreason");
        g.retrieveMemberById(targetID).queue(
                (target_Member) ->
                {
                    if(!m.canInteract(target_Member))
                    {
                        event.replyError(event.getLocalized("commands.mute.you_cannot_mute"));
                        return;
                    }
                    if(!event.getSelfMember().canInteract(target_Member))
                    {
                        event.replyError(event.getLocalized("commands.mute.i_cannot_mute"));
                        return;
                    }
                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        event.replyError(event.getLocalized("commands.mute.you_cannot_mute_moderator"));
                        return;
                    }
                    MutedRoleManager mutedRoleManager = DiscordBot.getInstance().mutedRoleManager;
                    Long mutedrole = mutedRoleManager.getMutedRole(g.getIdLong());
                    if(mutedrole == null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription("You have not set up a muted-role!\nAfter you have created and/or configured your muted-role, use `"+DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())+"setmutedrole [@Role/ID]`");
                        event.reply(builder.build());
                        return;
                    }
                    Role role = g.getRoleById(mutedrole);
                    if(role == null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription(event.getLocalized("commands.mute.no_role_setup", DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())));
                        event.reply(builder.build());
                        return;
                    }
                    if(!event.getSelfMember().canInteract(role))
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription(event.getLocalized("commands.cannot_interact_role", role.getAsMention()));
                        event.reply(builder.build());
                        return;
                    }
                    Case previouscase = Punishments.getActiveMuteCase(target_Member);
                    if(previouscase != null)
                    {
                        previouscase.setActive(false);
                    }
                    long thischannel = event.getChannel().getIdLong();
                    g.addRoleToMember(target_Member, role).queue(
                            (addedRole) ->
                            {
                                Case modcase = Case.createCase(CaseType.MUTE, g.getIdLong(), target_Member.getIdLong(), m.getIdLong(), Reason, time);
                                if(modcase == null)
                                {
                                    event.replyError(event.getLocalized("general.unknown_error_occured"));
                                    return;
                                }
                                Runnable runnable = () ->
                                {
                                    TextChannel currentchannel = DiscordBot.getInstance().jda.getTextChannelById(thischannel);
                                    Punishments.unmute(modcase, currentchannel);

                                };
                                DiscordBot.getInstance().scheduledExecutorService.schedule(runnable, time, TimeUnit.MILLISECONDS);
                                EmbedBuilder small = new EmbedBuilder()
                                        .setColor(CaseType.MUTE.getEmbedColor())
                                        .setDescription(CommandEvent.SUCCESS_EMOTE+" "+event.getLocalized("commands.mute.has_been_muted", target_Member.getUser().getAsTag(), Util.getLength(time/1000)))
                                        .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                                EmbedBuilder big = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(CaseType.MUTE.getEmbedColor())
                                        .setThumbnail(target_Member.getUser().getEffectiveAvatarUrl())
                                        .setFooter(event.getLocalized("commands.target_id")+": "+target_Member.getIdLong())
                                        .setTitle("Mute | Case #"+modcase.getCaseID())
                                        .addField(event.getLocalized("commands.target"), target_Member.getAsMention()+" ("+target_Member.getUser().getAsTag()+")", true)
                                        .addField("Moderator", m.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                        .addField(event.getLocalized("commands.reason"), Reason, false)
                                        .addField(event.getLocalized("commands.duration"), Util.getLength(time/1000), true);
                                if(!event.hasLogChannel())
                                {
                                    event.reply(big.build());
                                }else
                                {
                                    event.reply(small.build());
                                    event.replyInLogChannel(big.build());
                                }
                            },
                            (failure) ->
                            {
                                event.replyError(event.getLocalized("general.unknown_error_occured"));
                            }
                    );
                }, new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MEMBER, err -> event.replyError(event.getLocalized("general.user_not_in_guild")))
                .handle(ErrorResponse.UNKNOWN_USER, err -> event.replyError(event.getLocalized("general.user_not_exists")))
        );
    }
}
