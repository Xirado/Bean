package at.xirado.bean.commands.Moderation;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.handlers.PermissionCheckerManager;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.Punishments;
import com.mysql.cj.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

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
        this.usage = "case [Case ID] || case [Case ID] reason [New Reason]";
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
                event.replyError("The Case-ID must be 6 digit!\nExample: 4V8MNU");
                return;
            }
            Case modcase = Punishments.getCaseByID(args[0], g.getIdLong());
            if(modcase == null)
            {
                event.replyError("The case with the ID \""+args[0]+"\" does not exist.");
                return;
            }
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(modcase.getType().getEmbedColor())
                    .setTitle(modcase.getType().getFriendlyName()+" | Case #"+modcase.getCaseID())
                    .setTimestamp(Instant.ofEpochMilli(modcase.getCreatedAt()))
                    .setFooter("Issued")
                    .addField("Target", "<@"+modcase.getTargetID()+">", true)
                    .addField("Moderator", "<@"+modcase.getModeratorID()+">", true)
                    .addField("Reason", modcase.getReason(), true);
            if(modcase.getDuration() > 0)
            {
                builder.addField("Duration", Util.getLength(modcase.getDuration()/1000), true);
            }
            event.reply(builder.build());

        }else if(StringUtils.startsWithIgnoreCase(args[1], "reason"))
        {
            if(args.length < 3)
            {
                event.replyError("Invalid usage! Use:\n`"+DiscordBot.getInstance().prefixManager.getPrefix(g.getIdLong())+"case [CaseID] reason [New Reason]`");
                return;
            }
            String caseID = args[0];
            if(caseID.length() != 6)
            {
                event.replyError("The Case-ID must be 6 digit!\nExample: 4V8MNU");
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
                event.replyError("This modcase does not exist!");
                return;
            }
            modcase.setReason(Reason);
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.green)
                    .setDescription("Reason for case #"+modcase.getCaseID()+" has been changed to: "+Reason)
                    .setTimestamp(Instant.now());
            event.reply(builder.build());
            if(event.hasLogChannel())
            {
                event.replyinLogChannel(builder.build());
            }

        }
    }
}
