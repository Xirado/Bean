package at.xirado.bean.commands.blockingmoderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.language.Phrase;
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

import java.time.Instant;
import java.util.Arrays;

public class BanCommand extends Command
{
    public BanCommand(JDA jda)
    {
        super(jda);
        this.invoke = "ban";
        this.commandType = CommandType.MODERATION;
        this.neededPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.neededBotPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.description = "permanently bans a user from the server";
        this.usage = "ban [@Mention/ID] (optional reason)";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Guild guild = event.getGuild();
        Member senderMember = event.getMember();
        String[] args = event.getArguments().toStringArray();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        String target_ID = args[0].replaceAll("[^0-9]", "");
        if(target_ID.length() == 0)
        {
            event.replyError(Phrase.ID_MAY_NOT_BE_EMPTY.getTranslated(guild));
            return;
        }
        User target_User = null;
        try
        {
            target_User = DiscordBot.getInstance().jda.retrieveUserById(target_ID).complete();
        } catch (ErrorResponseException e)
        {
            event.replyError(Phrase.USER_DOES_NOT_EXIST.getTranslated(guild)+"!");
            return;
        }
        boolean userIsInCurrentGuild = guild.isMember(target_User);
        if(userIsInCurrentGuild)
        {
            Member target_Member = null;
            try
            {
                target_Member = guild.retrieveMember(target_User).complete();
            } catch (Exception e)
            {
                event.replyError(Phrase.AN_ERROR_OCCURED.getTranslated(guild)+"!");
                return;
            }
            if(!senderMember.canInteract(target_Member))
            {
                event.replyError(Phrase.YOU_CANNOT_BAN_THIS_MEMBER.getTranslated(guild)+"!");
                return;
            }
            if(!event.getSelfMember().canInteract(target_Member))
            {
                event.replyError(Phrase.I_CANNOT_BAN_THIS_MEMBER.getTranslated(guild)+"!");
                return;
            }
            if(DiscordBot.getInstance().permissionCheckerManager.isModerator(target_Member))
            {
                event.replyError(Phrase.YOU_CANNOT_BAN_A_MODERATOR.getTranslated(guild)+"!");
                return;
            }
        }
        boolean isbanned;
        try
        {
            guild.retrieveBan(target_User).complete();
            isbanned = true;
        } catch (ErrorResponseException e)
        {
            isbanned = false;
        }
        if(isbanned)
        {
            event.replyError(Phrase.CANNOT_BAN_ALREADY_BANNED.getTranslated(guild)+"!");
            return;
        }
        boolean withReason = args.length > 1;
        final String Reason = withReason ? event.getArguments().toString(1) : Phrase.NO_REASON_SPECIFIED.getTranslated(guild);
        try
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.BAN.getEmbedColor())
                    .setAuthor(Phrase.YOU_HAVE_BEEN_BANNED.getTranslated(guild).replaceAll("%guild%", guild.getName())+"!")
                    .addField(Phrase.REASON.getTranslated(guild), Reason, true)
                    .addField("Moderator", senderMember.getUser().getAsTag(), true);
            PrivateChannel privateChannel = target_User.openPrivateChannel().complete();
            privateChannel.sendMessage(builder.build()).complete();
        }catch(ErrorResponseException ignored)
        {

        }
        try
        {
            guild.ban(target_User, 0, Reason).complete();
        } catch (ErrorResponseException e)
        {
            event.replyError(Phrase.COULD_NOT_BAN_USER.getTranslated(guild)+"!");
            return;
        }
        Case bancase = Case.createCase(CaseType.BAN, guild.getIdLong(), target_User.getIdLong(), senderMember.getIdLong(), Reason, -1);
        if(bancase == null)
        {
            event.replyError(Phrase.AN_ERROR_OCCURED.getTranslated(guild));
            return;
        }
        if(event.hasLogChannel())
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(0x8b0000)
                    .setDescription(CommandEvent.SUCCESS_EMOTE +" "+Phrase.HAS_BEEN_BANNED.getTranslated(guild).replaceAll("%user%", target_User.getAsTag()))
                    .setFooter("Case #"+bancase.getCaseID()+" ("+Reason+")");
            event.reply(builder.build());
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(0x8b0000)
                .setThumbnail(target_User.getEffectiveAvatarUrl())
                .setFooter("Target ID: "+target_User.getIdLong())
                .setTitle("Ban | Case #"+bancase.getCaseID())
                .addField(Phrase.TARGET.getTranslated(guild), target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                .addField("Moderator", senderMember.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                .addField(Phrase.REASON.getTranslated(guild), Reason, false);
        if(!withReason)
        {
            builder.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"case "+bancase.getCaseID()+" reason [Reason]`\n to add a reason to this ban.", false);

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
