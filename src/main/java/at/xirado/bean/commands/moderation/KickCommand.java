package at.xirado.bean.commands.moderation;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class KickCommand extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(KickCommand.class);

    public KickCommand()
    {
        super("kick", "Kicks a member from this guild", "kick [@user/id] (reason)");
        setCommandCategory(CommandCategory.MODERATION);
        setRequiredPermissions(Permission.KICK_MEMBERS);
        setRequiredBotPermissions(Permission.KICK_MEMBERS);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Guild guild = event.getGuild();
        Member sender = context.getMember();
        String[] args = context.getArguments().toStringArray();
        if(args.length < 1)
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
        Member target_Member = null;
        try
        {
            target_Member = guild.retrieveMemberById(target_ID).complete();
        } catch (ErrorResponseException e)
        {
            if(e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER)
            {
                context.replyError(context.getLocalized("commands.user_not_in_guild"));
                return;
            }else if(e.getErrorResponse() == ErrorResponse.UNKNOWN_USER)
            {
                context.replyError(context.getLocalized("commands.user_not_exists"));
                return;
            }
        }
        if(target_Member == null)
        {
            context.replyError(context.getLocalized("commands.user_not_exists"));
            return;
        }
        if(!sender.canInteract(target_Member))
        {
            context.replyError(context.getLocalized("commands.kick.you_cannot_kick"));
            return;
        }
        if(!guild.getSelfMember().canInteract(target_Member))
        {
            context.replyError(context.getLocalized("commands.kick.i_cannot_kick"));
            return;
        }
        if(Bean.getInstance().permissionCheckerManager.isModerator(target_Member))
        {
            context.replyError(context.getLocalized("commands.kick.you_cannot_kick_moderator"));
            return;
        }
        boolean withReason = args.length > 1;
        final String Reason = withReason ? context.getArguments().toString(1) : context.getLocalized("commands.noreason");
        User target_User = target_Member.getUser();
        try
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.KICK.getEmbedColor())
                    .setTitle(context.getLocalized("commands.kick.you_have_been_kicked", guild.getName()))
                    .addField(context.getLocalized("reason"), Reason, true)
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
            context.replyError(context.getLocalized("commands.kick.could_not_kick_member"));
            return;
        }
        Case modcase = Case.createCase(CaseType.KICK, guild.getIdLong(), target_Member.getIdLong(), sender.getIdLong(), Reason, 0);
        if(modcase == null)
        {
            context.replyError(context.getLocalized("general.unknown_error_occured"));
            return;
        }
        if(context.hasLogChannel())
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.KICK.getEmbedColor())
                    .setDescription(CommandContext.SUCCESS_EMOTE +" "+context.getLocalized("commands.kick.has_been_kicked", target_User.getAsTag()))
                    .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
            context.reply(builder.build());
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(CaseType.KICK.getEmbedColor())
                .setThumbnail(target_User.getEffectiveAvatarUrl())
                .setFooter(context.getLocalized("commands.target_id")+": "+target_User.getIdLong())
                .setTitle("Kick | Case "+modcase.getCaseID())
                .addField(context.getLocalized("commands.target"), target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                .addField("Moderator", sender.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                .addField(context.getLocalized("commands.reason"), Reason, false);
        if(!withReason)
        {
            builder.addField("", "Use `"+ Bean.getInstance().prefixManager.getPrefix(guild.getIdLong())+"reason "+modcase.getCaseID()+" [Reason]` to add a reason to this kick.", false);

        }
        if(!context.hasLogChannel())
        {
            context.reply(builder.build());
        }else
        {
            context.replyInLogChannel(builder.build());
        }
    }
}
