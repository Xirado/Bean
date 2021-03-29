package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;

public class Warn extends Command
{
    public Warn(JDA jda)
    {
        super(jda);
        this.invoke = "warn";
        this.neededPermissions = Arrays.asList(Permission.ADMINISTRATOR); // TODO: Add proper implementation
        this.commandType = CommandType.MODERATION;
        this.description = "warns a user";
        this.usage = "warn [@User/ID] (Optional Reason)";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        Member member = event.getMember();
        User user = event.getAuthor();
        Guild guild = event.getGuild();
        String[] args = event.getArguments().toStringArray();
        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        final String ID = args[0].replaceAll("[^0-9]", "");
        final String Reason = event.getArguments().toString(1);
        DiscordBot.instance.jda.retrieveUserById(ID).queue(
                (target) ->
                {
                    if(!guild.isMember(target))
                    {
                        event.replyError("The given user is not on this server!");
                        return;
                    }
                    Case modcase = Case.createCase(CaseType.WARN, event.getGuild().getIdLong(), target.getIdLong(), member.getIdLong(), Reason, 0L);
                    EmbedBuilder builder = new EmbedBuilder()
                            .setThumbnail(target.getEffectiveAvatarUrl())
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setTitle("[Warn] "+target.getAsTag())
                            .setTimestamp(Instant.now())
                            .setFooter("User-ID: "+target.getIdLong())
                            .addField("User", target.getAsMention(), true)
                            .addField("Moderator", member.getAsMention(), true)
                            .addField("Reason", Reason, true)
                            .setDescription("Case `"+modcase.getCaseID()+"`");
                    event.reply(new EmbedBuilder()
                            .setColor(CaseType.WARN.getEmbedColor())
                            .setDescription("✅ "+target.getAsTag()+" has been warned")

                            .build()
                    );
                    event.replyinLogChannel(builder.build());
                    Util.sendPrivateMessage(target, new EmbedBuilder()
                            .setColor(Color.red)
                            .setDescription("You have been warned in "+guild.getName()+"!\nReason: "+Reason)
                            .build());

                },
                (failure) ->
                {
                    event.replyError("Invalid User-ID!");
                }
        );
    }



}
