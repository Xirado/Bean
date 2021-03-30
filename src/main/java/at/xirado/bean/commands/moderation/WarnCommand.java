package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
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
import java.util.List;

public class WarnCommand extends Command
{
    public WarnCommand(JDA jda)
    {
        super(jda);
        this.invoke = "warn";
        this.commandType = CommandType.MODERATION;
        this.description = "Warns a member";
        this.usage = "warn [@Member/ID] (optional reason)";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Member m = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        Guild g = event.getGuild();
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            event.replyError("You are not permissed to do this!");
            return;
        }
        String[] args = event.getArguments().toStringArray();
        if(args.length < 1)
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
        String Reason = args.length < 2 ? "No reason specified" : event.getArguments().toString(1);
        boolean withReason = args.length > 1;
        DiscordBot.getInstance().jda.retrieveUserById(target_ID).queue();
        g.retrieveMemberById(target_ID).queue(
                (target_Member) ->
                {
                    User target_User = target_Member.getUser();
                    if(!m.canInteract(target_Member))
                    {
                        event.replyError("You cannot warn this member!");
                        return;
                    }

                    if(permissionCheckerManager.isModerator(target_Member) || target_Member.hasPermission(Permission.ADMINISTRATOR))
                    {
                        event.replyError("You cannot warn a moderator!");
                        return;
                    }
                    Case modcase = Case.createCase(CaseType.WARN, g.getIdLong(), target_Member.getIdLong(), m.getIdLong(), Reason, 0);
                    if(modcase == null)
                    {
                        event.replyError("An error occured!");
                        return;
                    }
                    target_User.openPrivateChannel().queue(
                            (privateChannel) ->
                            {
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(CaseType.WARN.getEmbedColor())
                                        .setAuthor("You have been warned on "+g.getName()+"!", null, g.getIconUrl())
                                        .addField("Reason", Reason, true)
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
                            .setFooter("Target ID: "+target_Member.getIdLong())
                            .setTitle("Warn | Case #"+modcase.getCaseID())
                            .addField("Target", target_Member.getAsMention()+" ("+target_User.getAsTag()+")", true)
                            .addField("Moderator", m.getAsMention()+" ("+event.getAuthor().getAsTag()+")", true)
                            .addField("Reason", Reason, false);
                    if(!withReason)
                    {
                        mainembed.addField("", "Use `"+DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())+"case "+modcase.getCaseID()+" reason [Reason]`\n to add a reason to this warn.", false);
                    }
                    if(!event.hasLogChannel())
                    {
                        event.reply(mainembed.build());
                    }else
                    {
                        EmbedBuilder simple = new EmbedBuilder()
                                .setColor(CaseType.WARN.getEmbedColor())
                                .setDescription(CommandEvent.SUCCESS_EMOTE+" "+target_User.getAsTag()+" has been warned")
                                .setFooter("Case #"+modcase.getCaseID()+" ("+Reason+")");
                        event.reply(simple.build());
                        event.replyinLogChannel(mainembed.build());
                    }
                }, new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_MEMBER, err -> event.replyError("This user is not on this server!"))
                    .handle(ErrorResponse.UNKNOWN_USER, err -> event.replyError("This user does not exist!"))
        );
    }

    /**
     * HTL-Server
     * @param m Member
     */
    public static void doWarnPolicy(Member m)
    {
        List<Case> cases = Punishments.getAllWarns(m);
        if(cases == null || cases.isEmpty()) return;
        int warnsLessThan100DaysOld = 0;
        for(Case modcase : cases)
        {
            if(!modcase.isActive()) continue;
            if(modcase.getCreatedAt() > System.currentTimeMillis()-8640000000L)
            {
                warnsLessThan100DaysOld++;
            }
        }
        switch (warnsLessThan100DaysOld)
        {
            case 0:
                return;
            case 1:

        }
    }
}
