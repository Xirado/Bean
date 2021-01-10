package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import at.Xirado.Bean.PunishmentManager.Case;
import at.Xirado.Bean.PunishmentManager.Punishments;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;

public class CaseCommand extends Command
{
    public CaseCommand(JDA jda)
    {
        super(jda);
        this.invoke = "case";
        this.commandType = CommandType.MODERATION;
        this.aliases = new String[]{"incident"};
        this.description = "Shows information of a given case ID";
        this.neededPermissions =  new Permission[]{Permission.ADMINISTRATOR};
        this.usage = "case [Case ID] || case [Case ID] [reason|duration] [argument]";
    }

    @Override
    public void execute(CommandEvent event)
    {
        String[] args = event.getArguments().getArguments();
        Member m = event.getMember();
        User u = event.getAuthor();
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
            Case modcase = Punishments.getCaseByID(args[0], g);
            if(modcase == null)
            {
                event.replyError("This Case-ID does not exist!");
                return;
            }
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(modcase.getType().getEmbedColor())
                    .setTitle("Case "+modcase.getCaseID()+" ("+modcase.getType().getFriendlyName()+")")
                    .setTimestamp(Instant.ofEpochMilli(modcase.getCreatedAt()))
                    .setFooter("Issued")
                    .addField("User", "<@"+modcase.getTargetID()+">", true)
                    .addField("Moderator", "<@"+modcase.getModeratorID()+">", true)
                    .addField("Reason", modcase.getReason(), true);
            if(modcase.getDuration() > 0)
            {
                builder.addField("Duration", Util.getLength(modcase.getDuration()/1000), true);
            }
            event.reply(builder.build());

        }
    }
}
