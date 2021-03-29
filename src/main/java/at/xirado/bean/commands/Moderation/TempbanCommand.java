package at.xirado.bean.commands.Moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.language.FormattedDuration;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import at.xirado.bean.punishmentmanager.Punishments;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
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
    public void executeCommand(CommandEvent event)
    {
        Guild guild = event.getGuild();
        Member sender = event.getMember();
        String[] args = event.getArguments().toStringArray();
        if(args.length < 2)
        {
            event.replyErrorUsage();
            return;
        }
        String target_ID = args[0].replaceAll("[^0-9]", "");
        if(target_ID.length() == 0)
        {
            event.replyError("User-ID may not be empty!");
            return;
        }
        Long time = FormattedDuration.parsePeriod(args[1]);
        if(time == null)
        {
            event.replyErrorUsage();
            return;
        }
        guild.retrieveMemberById(target_ID).queue(
                (target_Member) ->
                {
                    if(!sender.canInteract(target_Member))
                    {
                        event.replyError("You cannot ban this member!");
                        return;
                    }
                    if(!event.getSelfMember().canInteract(target_Member))
                    {
                        event.replyError("I cannot ban this member!");
                        return;
                    }
                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        event.replyError("You cannot ban a moderator!");
                        return;
                    }
                    boolean withReason = args.length > 2;
                    final String Reason = withReason ? event.getArguments().toString(2) : "No reason specified";
                    User target_User = target_Member.getUser();
                    String user_mention = target_User.getAsMention();
                    String user_tag = target_User.getAsTag();
                    String sender_mention = sender.getAsMention();
                    String sender_tag = event.getSelfMember().getUser().getAsTag();
                    long channelid = event.getChannel().getIdLong();
                    guild.ban(target_Member, 0, Reason).queue(
                            (success) ->
                            {
                                Case modcase = Case.createCase(CaseType.TEMPBAN, guild.getIdLong(), target_Member.getIdLong(), sender.getIdLong(), Reason, time);
                                Runnable r = () ->
                                {
                                    Punishments.unban(modcase, DiscordBot.getInstance().jda.getTextChannelById(channelid));

                                };
                                DiscordBot.getInstance().scheduledExecutorService.schedule(r, time, TimeUnit.MILLISECONDS);
                                if(event.hasLogChannel())
                                {
                                    EmbedBuilder builder = new EmbedBuilder()
                                            .setColor(0x8b0000)
                                            .setDescription(CommandEvent.SUCCESS_EMOTE +" "+target_User.getAsTag()+" has been banned")
                                            .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                                    event.reply(builder.build());
                                }
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(0x8b0000)
                                        .setThumbnail(target_User.getEffectiveAvatarUrl())
                                        .setFooter("Target ID: "+target_User.getIdLong())
                                        .setTitle("Ban | Case #"+modcase.getCaseID())
                                        .addField("Target", target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                                        .addField("Moderator", sender.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                        .addField("Reason", Reason, false)
                                        .addField("Duration", Util.getLength(time/1000), true);
                                if(!withReason)
                                {
                                    builder.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"case "+modcase.getCaseID()+" reason [Reason]`\n to add a reason to this ban.", false);

                                }
                                if(!event.hasLogChannel())
                                {
                                    event.reply(builder.build());
                                }else
                                {
                                    event.replyinLogChannel(builder.build());
                                }
                            },
                            (error) -> event.replyError("Could not ban this user!")
                    );
                }, new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_MEMBER, (err) ->
                    {
                        event.replyError("This user is not member of this server!");
                    })
                    .handle(ErrorResponse.UNKNOWN_USER, (err) ->
                    {
                        event.replyError("The user you specified does not exist!");
                    })
        );

    }
}
