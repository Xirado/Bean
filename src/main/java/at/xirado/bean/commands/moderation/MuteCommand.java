package at.xirado.bean.commands.moderation;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.commandutil.CommandFlag;
import at.xirado.bean.handlers.MutedRoleManager;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.Command;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import at.xirado.bean.punishmentmanager.Punishments;
import at.xirado.bean.translation.FormattedDuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends Command
{
    public MuteCommand()
    {
        super("mute", "mutes a member", "mute [@Mention/ID] [duration] (optional reason)");
        setCommandCategory(CommandCategory.MODERATION);
        setRequiredBotPermissions(Permission.MANAGE_ROLES);
        setCommandFlags(CommandFlag.MODERATOR_ONLY);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Member m = context.getMember();
        Guild g = event.getGuild();
        String[] args = context.getArguments().toStringArray();
        if(args.length < 2)
        {
            context.replyErrorUsage();
            return;
        }
        String targetID = args[0].replaceAll("[^0-9]", "");
        if(targetID.length() == 0)
        {
            context.replyError(context.getLocalized("commands.id_empty"));
            return;
        }
        Long time = FormattedDuration.parsePeriod(args[1]);
        if(time == null)
        {
            context.replyErrorUsage();
            return;
        }
        if(time < 1000)
        {
            context.replyError(context.getLocalized("commands.mute.duration_too_short"));
            return;
        }
        String Reason = args.length > 2 ? context.getArguments().toString(2) : context.getLocalized("commands.noreason");
        g.retrieveMemberById(targetID).queue(
                (target_Member) ->
                {
                    if(!m.canInteract(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.mute.you_cannot_mute"));
                        return;
                    }
                    if(!event.getGuild().getSelfMember().canInteract(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.mute.i_cannot_mute"));
                        return;
                    }
                    if(Bean.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.mute.you_cannot_mute_moderator"));
                        return;
                    }
                    MutedRoleManager mutedRoleManager = Bean.getInstance().mutedRoleManager;
                    Long mutedrole = mutedRoleManager.getMutedRole(g.getIdLong());
                    if(mutedrole == null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription("You have not set up a muted-role!\nAfter you have created and/or configured your muted-role, use `"+ Bean.getInstance().prefixManager.getPrefix(g.getIdLong())+"setmutedrole [@Role/ID]`");
                        context.reply(builder.build());
                        return;
                    }
                    Role role = g.getRoleById(mutedrole);
                    if(role == null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription(context.getLocalized("commands.mute.no_role_setup", Bean.getInstance().prefixManager.getPrefix(g.getIdLong())));
                        context.reply(builder.build());
                        return;
                    }
                    if(!event.getGuild().getSelfMember().canInteract(role))
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription(context.getLocalized("commands.cannot_interact_role", role.getAsMention()));
                        context.reply(builder.build());
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
                                    context.replyError(context.getLocalized("general.unknown_error_occured"));
                                    return;
                                }
                                Runnable runnable = () ->
                                {
                                    TextChannel currentchannel = Bean.getInstance().jda.getTextChannelById(thischannel);
                                    Punishments.unmute(modcase, currentchannel);

                                };
                                Bean.getInstance().scheduledExecutorService.schedule(runnable, time, TimeUnit.MILLISECONDS);
                                EmbedBuilder small = new EmbedBuilder()
                                        .setColor(CaseType.MUTE.getEmbedColor())
                                        .setDescription(CommandContext.SUCCESS_EMOTE+" "+context.getLocalized("commands.mute.has_been_muted", target_Member.getUser().getAsTag(), Util.getLength(time/1000)))
                                        .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                                EmbedBuilder big = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(CaseType.MUTE.getEmbedColor())
                                        .setThumbnail(target_Member.getUser().getEffectiveAvatarUrl())
                                        .setFooter(context.getLocalized("commands.target_id")+": "+target_Member.getIdLong())
                                        .setTitle("Mute | Case #"+modcase.getCaseID())
                                        .addField(context.getLocalized("commands.target"), target_Member.getAsMention()+" ("+target_Member.getUser().getAsTag()+")", true)
                                        .addField("Moderator", m.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                        .addField(context.getLocalized("commands.reason"), Reason, false)
                                        .addField(context.getLocalized("commands.duration"), Util.getLength(time/1000), true);
                                if(!context.hasLogChannel())
                                {
                                    context.reply(big.build());
                                }else
                                {
                                    context.reply(small.build());
                                    context.replyInLogChannel(big.build());
                                }
                            },
                            (failure) ->
                            {
                                context.replyError(context.getLocalized("general.unknown_error_occured"));
                            }
                    );
                }, new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MEMBER, err -> context.replyError(context.getLocalized("general.user_not_in_guild")))
                .handle(ErrorResponse.UNKNOWN_USER, err -> context.replyError(context.getLocalized("general.user_not_exists")))
        );
    }
}
