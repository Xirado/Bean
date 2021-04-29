package at.xirado.bean.commands.moderation;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.commandutil.CommandFlag;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.objects.Command;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Instant;

public class WarnCommand extends Command
{
    public WarnCommand()
    {
        super("warn", "Warns a member", "warn [@Member/ID] (optional reason)");
        setCommandCategory(CommandCategory.MODERATION);
        setCommandFlags(CommandFlag.MODERATOR_ONLY);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        Member m = context.getMember();
        PermissionCheckerManager permissionCheckerManager = Bean.getInstance().permissionCheckerManager;
        Guild g = event.getGuild();
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
        String Reason = args.length < 2 ? context.getLocalized("commands.noreason") : context.getArguments().toString(1);
        boolean withReason = args.length > 1;
        Bean.getInstance().jda.retrieveUserById(target_ID).queue();
        g.retrieveMemberById(target_ID).queue(
                (target_Member) ->
                {
                    User target_User = target_Member.getUser();
                    if(!m.canInteract(target_Member))
                    {
                        context.replyError(context.getLocalized("commands.warn.you_cannot_warn"));
                        return;
                    }

                    if(permissionCheckerManager.isModerator(target_Member) || target_Member.hasPermission(Permission.ADMINISTRATOR))
                    {
                        context.replyError(context.getLocalized("commands.warn.you_cannot_warn_moderator"));
                        return;
                    }
                    Case modcase = Case.createCase(CaseType.WARN, g.getIdLong(), target_Member.getIdLong(), m.getIdLong(), Reason, 0);
                    if(modcase == null)
                    {
                        context.replyError(context.getLocalized("general.unknown_error_occured"));
                        return;
                    }
                    target_User.openPrivateChannel().queue(
                            (privateChannel) ->
                            {
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(CaseType.WARN.getEmbedColor())
                                        .setAuthor(context.getLocalized("commands.warn.you_have_been_warned", g.getName()), null, g.getIconUrl())
                                        .addField(context.getLocalized("commands.reason"), Reason, true)
                                        .addField("Moderator", event.getAuthor().getAsTag(), true);
                                privateChannel.sendMessage(builder.build()).queue(success -> {}, error -> {});
                            },
                            (error) ->
                            {

                            }
                    );
                    EmbedBuilder mainembed = new EmbedBuilder()
                            .setThumbnail(target_User.getEffectiveAvatarUrl())
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setTimestamp(Instant.now())
                            .setFooter(context.getLocalized("commands.target_id")+": "+target_Member.getIdLong())
                            .setTitle("Warn | Case #"+modcase.getCaseID())
                            .addField(context.getLocalized("commands.target"), target_Member.getAsMention()+" ("+target_User.getAsTag()+")", true)
                            .addField("Moderator", m.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                            .addField(context.getLocalized("commands.reason"), Reason, false);
                    if(!withReason)
                    {
                        mainembed.addField("", "Use `"+ Bean.getInstance().prefixManager.getPrefix(g.getIdLong())+"case "+modcase.getCaseID()+" reason [Reason]`\n to add a reason to this warn.", false);
                    }
                    if(!context.hasLogChannel())
                    {
                        context.reply(mainembed.build());
                    }else
                    {
                        EmbedBuilder simple = new EmbedBuilder()
                                .setColor(CaseType.WARN.getEmbedColor())
                                .setDescription(CommandContext.SUCCESS_EMOTE+" "+context.getLocalized("commands.warn.has_been_warned", target_User.getAsTag()))
                                .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                        context.reply(simple.build());
                        context.replyInLogChannel(mainembed.build());
                    }
                }, new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_MEMBER, err -> context.replyError(context.getLocalized("commands.user_not_in_guild")))
                    .handle(ErrorResponse.UNKNOWN_USER, err -> context.replyError(context.getLocalized("commands.user_not_exists")))
        );
    }
}
