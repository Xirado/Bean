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
            event.replyError("ID may not be empty!");
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
                event.replyError("This user is not in this guild!");
                return;
            }else if(e.getErrorResponse() == ErrorResponse.UNKNOWN_USER)
            {
                event.replyError("This user does not exist!");
                return;
            }
        }
        if(target_Member == null)
        {
            event.replyError("This user does not exist!");
            return;
        }
        if(!sender.canInteract(target_Member))
        {
            event.replyError("You cannot kick this member!");
            return;
        }
        if(!event.getSelfMember().canInteract(target_Member))
        {
            event.replyError("I cannot kick this member!");
            return;
        }
        if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
        {
            event.replyError("You cannot kick a moderator!");
            return;
        }
        boolean withReason = args.length > 1;
        final String Reason = withReason ? event.getArguments().toString(1) : "No reason specified";
        User target_User = target_Member.getUser();
        try
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.KICK.getEmbedColor())
                    .setTitle("You have been kicked from "+guild.getName()+"!")
                    .addField("Reason", Reason, true)
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
            event.replyError("Could not kick this member!");
            return;
        }
        Case modcase = Case.createCase(CaseType.KICK, guild.getIdLong(), target_Member.getIdLong(), sender.getIdLong(), Reason, 0);
        if(modcase == null)
        {
            logger.error("Could not create modcase!", new Exception());
            return;
        }
        if(event.hasLogChannel())
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.KICK.getEmbedColor())
                    .setDescription(CommandEvent.SUCCESS_EMOTE +" "+target_User.getAsTag()+" has been kicked")
                    .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
            event.reply(builder.build());
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(CaseType.KICK.getEmbedColor())
                .setThumbnail(target_User.getEffectiveAvatarUrl())
                .setFooter("Target ID: "+target_User.getIdLong())
                .setTitle("Kick | Case "+modcase.getCaseID())
                .addField("Target", target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                .addField("Moderator", sender.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                .addField("Reason", Reason, false);
        if(!withReason)
        {
            builder.addField("", "Use `"+ DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"reason "+modcase.getCaseID()+" [Reason]` to add a reason to this kick.", false);

        }
        if(!event.hasLogChannel())
        {
            event.reply(builder.build());
        }else
        {
            event.replyinLogChannel(builder.build());
        }
    }
}
