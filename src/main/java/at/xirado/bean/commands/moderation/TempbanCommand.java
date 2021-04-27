package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TempbanCommand extends Command
{

    public TempbanCommand(JDA jda)
    {
        super(jda);
        this.invoke = "tempban";
        this.commandType = CommandType.MODERATION;
        this.neededPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.neededBotPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.description = "Temporarily bans a member from the server";
        this.usage = "tempban [@Mention/ID] [Duration] (optional reason)";
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Guild guild = event.getGuild();
        Member sender = context.getMember();
        String[] args = context.getArguments().toStringArray();
        if(args.length < 2)
        {
            context.replyErrorUsage();
            return;
        }
        String target_ID = args[0].replaceAll("[^0-9]", "");
        if(target_ID.length() == 0)
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
        guild.retrieveMemberById(target_ID).queue(
                (target_Member) ->
                {
                    if(!sender.canInteract(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.ban.you_cannot_ban_this_member"));
                        return;
                    }
                    if(!event.getGuild().getSelfMember().canInteract(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.ban.i_cannot_ban_this_member"));
                        return;
                    }
                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.ban.cannot_ban_moderator"));
                        return;
                    }
                    boolean withReason = args.length > 2;
                    final String Reason = withReason ? context.getArguments().toString(2) : context.getLocalized("commands.noreason");
                    User target_User = target_Member.getUser();
                    long channelid = event.getChannel().getIdLong();
                    guild.ban(target_Member, 0, Reason).queue(
                            (success) ->
                            {
                                Case modcase = Case.createCase(CaseType.TEMPBAN, guild.getIdLong(), target_Member.getIdLong(), sender.getIdLong(), Reason, time);
                                if(modcase == null)
                                {
                                    context.replyError(context.getLocalized("general.unknown_error_occured"));
                                    return;
                                }
                                Runnable r = () ->
                                {
                                    Punishments.unban(modcase, DiscordBot.getInstance().jda.getTextChannelById(channelid));

                                };
                                DiscordBot.getInstance().scheduledExecutorService.schedule(r, time, TimeUnit.MILLISECONDS);
                                if(context.hasLogChannel())
                                {
                                    EmbedBuilder builder = new EmbedBuilder()
                                            .setColor(0x8b0000)
                                            .setDescription(CommandContext.SUCCESS_EMOTE +" "+context.getLocalized("commands.ban.has_been_banned", target_User.getAsTag()))
                                            .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                                    context.reply(builder.build());
                                }
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(0x8b0000)
                                        .setThumbnail(target_User.getEffectiveAvatarUrl())
                                        .setFooter(context.getLocalized("commands.target_id")+": "+target_User.getIdLong())
                                        .setTitle("Ban | Case #"+modcase.getCaseID())
                                        .addField(context.getLocalized("commands.target"), target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                                        .addField("Moderator", sender.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                        .addField(context.getLocalized("commands.reason"), Reason, false)
                                        .addField(context.getLocalized("commands.duration"), Util.getLength(time/1000), true);
                                if(!withReason)
                                {
                                    builder.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"case "+modcase.getCaseID()+" reason [Reason]`\n to add a reason to this ban.", false);

                                }
                                if(!context.hasLogChannel())
                                {
                                    context.reply(builder.build());
                                }else
                                {
                                    context.replyInLogChannel(builder.build());
                                }
                            },
                            (error) -> context.replyError(context.getLocalized("commands.ban.could_not_ban_user"))
                    );
                }, new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_MEMBER, (err) ->
                    {
                        context.replyError(context.getLocalized("commands.user_not_in_guild"));
                    })
                    .handle(ErrorResponse.UNKNOWN_USER, (err) ->
                    {
                        context.replyError(context.getLocalized("commands.user_not_exists"));
                    })
        );

    }
}
