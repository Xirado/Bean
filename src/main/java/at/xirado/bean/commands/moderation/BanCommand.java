package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Guild guild = event.getGuild();
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
        User target_User = null;
        try
        {
            target_User = DiscordBot.getInstance().jda.retrieveUserById(target_ID).complete();
        } catch (ErrorResponseException e)
        {
            context.replyError(context.getLocalized("commands.user_not_exists"));
            return;
        }
        boolean userIsInCurrentGuild = guild.isMember(target_User);
        if(userIsInCurrentGuild)
        {
            Member target_Member;
            try
            {
                target_Member = guild.retrieveMember(target_User).complete();
            } catch (Exception e)
            {
                context.replyError(context.getLocalized("general.unknown_error_occured"));
                return;
            }
            if(!context.getMember().canInteract(target_Member))
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
            context.replyError(context.getLocalized("commands.ban.already_banned"));
            return;
        }
        boolean withReason = args.length > 1;
        final String Reason = withReason ? context.getArguments().toString(1) : context.getLocalized("commands.noreason");
        try
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(CaseType.BAN.getEmbedColor())
                    .setAuthor(context.getLocalized("commands.ban.you_have_been_banned", guild.getName()))
                    .addField(context.getLocalized("commands.reason"), Reason, true)
                    .addField("Moderator", event.getAuthor().getAsTag(), true);
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
            context.replyError(context.getLocalized("commands.ban.could_not_ban_user"));
            return;
        }
        Case bancase = Case.createCase(CaseType.BAN, guild.getIdLong(), target_User.getIdLong(), context.getMember().getIdLong(), Reason, -1);
        if(bancase == null)
        {
            context.replyError(context.getLocalized("general.unknown_error_occured"));
            return;
        }
        if(context.hasLogChannel())
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(0x8b0000)
                    .setDescription(CommandContext.SUCCESS_EMOTE +" "+context.getLocalized("commands.ban.has_been_banned", target_User.getAsTag()))
                    .setFooter("Case #"+bancase.getCaseID()+" ("+Reason+")");
            context.reply(builder.build());
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(0x8b0000)
                .setThumbnail(target_User.getEffectiveAvatarUrl())
                .setFooter(context.getLocalized("commands.target_id")+": "+target_User.getIdLong())
                .setTitle("Ban | Case #"+bancase.getCaseID())
                .addField(context.getLocalized("commands.target"), target_User.getAsMention()+" ("+target_User.getAsTag()+")", true)
                .addField("Moderator", context.getMember().getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                .addField(context.getLocalized("commands.reason"), Reason, false);
        if(!withReason)
        {
            builder.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(guild.getIdLong())+"case "+bancase.getCaseID()+" reason [Reason]`\n to add a reason to this ban.", false);

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
