package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

public class KickCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(KickCommand.class);

    public KickCommand(JDA jda)
    {
        super(jda);
        this.invoke = "kick";
        this.commandType = CommandType.MODERATION;
        this.description = "Kicks a member from the server";
        this.neededBotPermissions = Arrays.asList(Permission.KICK_MEMBERS);
        this.neededPermissions = Arrays.asList(Permission.KICK_MEMBERS);
        this.usage = "kick [@Mention/ID] (optional reason)";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Guild guild = event.getGuild();
        Member sender = event.getMember();
        String[] args = event.getArguments().toStringArray();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        String target_ID = args[0].replaceAll("[^0-9]", "");
        if(target_ID.length() == 0)
        {
            event.replyError(event.getLocalized("commands.id_empty"));
            return;
        }
        Member target_Member = null;
        try
        {
            target_Member = guild.retrieveMemberById(target_ID).complete();
        } catch (ErrorResponseException e)
        {
            if(e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER)
            {
                event.replyError(event.getLocalized("commands.user_not_in_guild"));
                return;
            }else if(e.getErrorResponse() == ErrorResponse.UNKNOWN_USER)
            {
                event.replyError(event.getLocalized("commands.user_not_exists"));
                return;
            }
        }
        if(target_Member == null)
        {
            event.replyError(event.getLocalized("commands.user_not_exists"));
            return;
        }
        if(!sender.canInteract(target_Member))
        {
            event.replyError(event.getLocalized("commands.kick.you_cannot_kick"));
            return;
        }
        if(!event.getSelfMember().canInteract(target_Member))
        {
            event.replyError(event.getLocalized("commands.kick.i_cannot_kick"));
            return;
        }
        if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
        {
            event.replyError(event.getLocalized("commands.kick.you_cannot_kick_moderator"));
            return;
        }
        boolean withReason = args.length > 1;
        final String Reason = withReason ? event.getArguments().toString(1) : event.getLocalized("commands.noreason");
        User target_User = target_Member.getUser();
        try
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.KICK.getEmbedColor())
                    .setTitle(event.getLocalized("commands.kick.you_have_been_kicked", guild.getName()))
                    .addField(event.getLocalized("reason"), Reason, true)
                    .addField("Moderator", sender.getUser().getAsTag(), true);
            PrivateChannel privateChannel = target_User.openPrivateChannel().complete();
            privateChannel.sendMessage(builder.build()).complete();
        }catch(ErrorResponseException ignored)
        {

        }
        try
        {
            guild.kick(target_Member, Reason).complete();
        } catch (ErrorResponseException e)
        {
            event.replyError(event.getLocalized("commands.kick.could_not_kick_member"));
            return;
        }
        Case modcase = Case.createCase(CaseType.KICK, guild.getIdLong(), target_Member.getIdLong(), sender.getIdLong(), Reason, 0);
        if(modcase == null)
        {
            event.replyError(event.getLocalized("general.unknown_error_occured"));
            return;
        }
        if(event.hasLogChannel())
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.KICK.getEmbedColor())
                    .setDescription(CommandEvent.SUCCESS_EMOTE +" "+event.getLocalized("commands.kick.has_been_kicked", target_User.getAsTag()))
                    .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
            event.reply(builder.build());
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(CaseType.KICK.getEmbedColor())
                .setThumbnail(target_User.getEffectiveAvatarUrl())
                .setFooter(event.getLocalized("commands.target_id")+": "+target_User.getIdLong())
                .setTitle("Kick | Case "+modcase.getCaseID())
                .addField(event.getLocalized("commands.target"), target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                .addField("Moderator", sender.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                .addField(event.getLocalized("commands.reason"), Reason, false);
        if(!withReason)
        {
            builder.addField("", "Use `"+ DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"reason "+modcase.getCaseID()+" [Reason]` to add a reason to this kick.", false);

        }
        if(!event.hasLogChannel())
        {
            event.reply(builder.build());
        }else
        {
            event.replyInLogChannel(builder.build());
        }
    }
}
