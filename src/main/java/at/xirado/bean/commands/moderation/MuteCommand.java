package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.MutedRoleManager;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import at.xirado.bean.punishmentmanager.Punishments;
import at.xirado.bean.translation.FormattedDuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Member m = context.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            context.replyError(context.getLocalized("general.no_perms"));
            return;
        }
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
                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.mute.you_cannot_mute_moderator"));
                        return;
                    }
                    MutedRoleManager mutedRoleManager = DiscordBot.getInstance().mutedRoleManager;
                    Long mutedrole = mutedRoleManager.getMutedRole(g.getIdLong());
                    if(mutedrole == null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription("You have not set up a muted-role!\nAfter you have created and/or configured your muted-role, use `"+DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())+"setmutedrole [@Role/ID]`");
                        context.reply(builder.build());
                        return;
                    }
                    Role role = g.getRoleById(mutedrole);
                    if(role == null)
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription(context.getLocalized("commands.mute.no_role_setup", DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())));
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
                                    TextChannel currentchannel = DiscordBot.getInstance().jda.getTextChannelById(thischannel);
                                    Punishments.unmute(modcase, currentchannel);

                                };
                                DiscordBot.getInstance().scheduledExecutorService.schedule(runnable, time, TimeUnit.MILLISECONDS);
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
