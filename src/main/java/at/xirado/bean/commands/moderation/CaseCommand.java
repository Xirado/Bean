package at.xirado.bean.commands.moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.Punishments;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;

public class CaseCommand extends Command
{
    public CaseCommand(JDA jda)
    {
        super(jda);
        this.invoke = "case";
        this.commandType = CommandType.MODERATION;
        this.aliases = Arrays.asList("incident");
        this.description = "Shows information of a case or changes the reason.";
        this.usage = "case [Case ID] || case [Case ID] reason [New Reason] || case [Case ID] delete";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Member m = event.getMember();
        PermissionCheckerManager permissionCheckerManager = DiscordBot.getInstance().permissionCheckerManager;
        if(!permissionCheckerManager.isModerator(m) && !m.hasPermission(Permission.ADMINISTRATOR))
        {
            event.replyError(event.getLocalized("general.no_perms"));
            return;
        }
        String[] args = event.getArguments().toStringArray();
        Guild g = event.getGuild();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        if(args.length == 1)
        {
            if(args[0].length() != 6)
            {
                event.replyError(event.getLocalized("commands.casecmd.must_be_6_digit"));
                return;
            }
            Case modcase = Punishments.getCaseByID(args[0], g.getIdLong());
            if(modcase == null)
            {
                event.replyError(event.getLocalized("commands.casecmd.not_exists", args[0]));
                return;
            }
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(modcase.getType().getEmbedColor())
                    .setTitle(modcase.getType().getFriendlyName()+" | Case #"+modcase.getCaseID())
                    .setTimestamp(Instant.ofEpochMilli(modcase.getCreatedAt()))
                    .setFooter(event.getLocalized("commands.issued"))
                    .addField(event.getLocalized("commands.target"), "<@"+modcase.getTargetID()+">", true)
                    .addField("Moderator", "<@"+modcase.getModeratorID()+">", true)
                    .addField(event.getLocalized("commands.reason"), modcase.getReason(), true);
            if(modcase.getDuration() > 0)
            {
                builder.addField(event.getLocalized("commands.duration"), Util.getLength(modcase.getDuration()/1000), true);
            }
            event.reply(builder.build());

        }else if(StringUtils.startsWithIgnoreCase(args[1], "reason"))
        {
            if(args.length < 3)
            {
                event.replyError(event.getLocalized("commands.casecmd.invalid_usage", DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())));
                return;
            }
            String caseID = args[0];
            if(caseID.length() != 6)
            {
                event.replyError(event.getLocalized("commands.casecmd.must_be_6_digit"));
                return;
            }
            StringBuilder sb = new StringBuilder();
            for(int i = 2; i < args.length; i++)
            {
                sb.append(args[i]).append(" ");
            }
            String Reason = sb.toString().trim();
            Case modcase = Punishments.getCaseByID(caseID, g.getIdLong());
            if(modcase == null)
            {
                event.replyError(event.getLocalized("commands.casecmd.not_exists", caseID));
                return;
            }
            modcase.setReason(Reason);
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.green)
                    .setDescription(event.getLocalized("commands.casecmd.reason_changed", modcase.getCaseID(), Reason))
                    .setTimestamp(Instant.now());
            event.reply(builder.build());
            if(event.hasLogChannel())
            {
                event.replyinLogChannel(builder.build());
            }

        }
    }
}
