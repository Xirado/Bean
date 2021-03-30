package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.MutedRoleManager;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.language.FormattedDuration;
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
            event.replyError("You are not permissed to do this!");
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
            event.replyError("User-ID may not be empty!");
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
            event.replyError("You can't mute someone for this short amount of time");
            return;
        }
        String Reason = args.length > 2 ? event.getArguments().toString(2) : "No reason specified";
        g.retrieveMemberById(targetID).queue(
                (target_Member) ->
                {
                    if(!m.canInteract(target_Member))
                    {
                        event.replyError("You cannot mute this member!");
                        return;
                    }
                    if(!event.getSelfMember().canInteract(target_Member))
                    {
                        event.replyError("I cannot mute this member!");
                        return;
                    }
                    if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
                    {
                        event.replyError("You cannot mute a moderator!");
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
                                .setDescription("The muted-role does no longer exist!\nUse `"+DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())+"setmutedrole [@Role/ID]` to set a new one!");
                        event.reply(builder.build());
                        return;
                    }
                    if(!event.getSelfMember().canInteract(role))
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription("I cannot interact with the role "+role.getAsMention()+"!");
                        event.reply(builder.build());
                        return;
                    }
                    Case previouscase = Punishments.getActiveMuteCase(target_Member);
                    if(previouscase != null)
                    {
                        previouscase.setActive(false);
                    }

                    String target_tag = target_Member.getUser().getAsTag();
                    String sender_tag = m.getUser().getAsTag();
                    long thischannel = event.getChannel().getIdLong();
                    g.addRoleToMember(target_Member, role).queue(
                            (addedRole) ->
                            {
                                Case modcase = Case.createCase(CaseType.MUTE, g.getIdLong(), target_Member.getIdLong(), m.getIdLong(), Reason, time);
                                if(modcase == null)
                                {
                                    event.replyError("An error occured!");
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
                                        .setDescription(CommandEvent.SUCCESS_EMOTE +" "+target_Member.getUser().getAsTag()+" has been muted\nDuration: "+ Util.getLength(time/1000))
                                        .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                                EmbedBuilder big = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(CaseType.MUTE.getEmbedColor())
                                        .setThumbnail(target_Member.getUser().getEffectiveAvatarUrl())
                                        .setFooter("Target ID: "+target_Member.getIdLong())
                                        .setTitle("Mute | Case #"+modcase.getCaseID())
                                        .addField("Target", target_Member.getAsMention()+" ("+target_Member.getUser().getAsTag()+")", true)
                                        .addField("Moderator", m.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                                        .addField("Reason", Reason, false)
                                        .addField("Duration", Util.getLength(time/1000), true);
                                if(!event.hasLogChannel())
                                {
                                    event.reply(big.build());
                                }else
                                {
                                    event.reply(small.build());
                                    event.replyinLogChannel(big.build());
                                }
                            },
                            (failure) ->
                            {
                                event.replyError("Could not mute member!");
                            }
                    );
                }, new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MEMBER, err -> event.replyError("This user is not on this guild!"))
                .handle(ErrorResponse.UNKNOWN_USER, err -> event.replyError("This user does not exist!"))
        );
    }
}
