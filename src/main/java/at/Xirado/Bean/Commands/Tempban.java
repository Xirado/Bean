package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Language.FormattedDuration;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import at.Xirado.Bean.PunishmentManager.Case;
import at.Xirado.Bean.PunishmentManager.CaseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Tempban extends Command
{

    public Tempban(JDA jda)
    {
        super(jda);
        this.invoke = "tempban";
        this.commandType = CommandType.MODERATION;
        this.description = "Bans an user temporarily";
        this.neededPermissions = Arrays.asList(Permission.BAN_MEMBERS);
        this.usage = "tempban [@User/ID] [Duration] (Optional Reason)";
    }

    @Override
    public void executeCommand(CommandEvent e) {
        String[] args = e.getArguments().toStringArray();
        Guild g = e.getGuild();
        Member m = e.getMember();
        TextChannel c = e.getChannel();
        if(args.length < 2)
        {
            e.replyErrorUsage();
            return;
        }
        Long time = FormattedDuration.parsePeriod(args[1]);
        if(time == null)
        {
            e.replyErrorUsage();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < args.length; i++)
        {
            sb.append(args[i]).append(" ");
        }
        String Reason = sb.toString().trim();
        String ID = args[0].replaceAll("[^0-9]", "");
        String finalReason = Reason;
        DiscordBot.instance.jda.retrieveUserById(ID).queue(
                (user) ->
                {
                    Long bantime = time;

                    if(g.isMember(user))
                    {
                        Member targetmember = g.getMember(user);
                        if(!m.canInteract(targetmember))
                        {
                            e.replyError("You cannot ban this user!");
                            return;
                        }
                        if(!g.getMember(DiscordBot.instance.jda.getSelfUser()).canInteract(targetmember))
                        {
                            e.replyError("I cannot ban this user!");
                            return;
                        }
                    }
                    final String wholereason = "banned ("+Util.getLength(bantime)+") by "+e.getAuthor().getAsTag()+" for "+finalReason;
                    long finalBantime = bantime;
                    g.ban(user,0, wholereason).queue(
                            (success) ->
                            {
                                final Case usercase = Case.createCase(CaseType.BAN, g.getIdLong(), user.getIdLong(), e.getAuthor().getIdLong(), finalReason, time);
                                Runnable task = () -> {
                                    usercase.fetchUpdate();
                                    if(usercase.isActive())
                                    {
                                        g.unban(user).queue();
                                        usercase.setActive(false);
                                    }else
                                    {
                                        System.out.println("Wanted to unban "+user.getAsTag()+", but he is no longer banned!");
                                    }

                                };
                                DiscordBot.instance.scheduledExecutorService.schedule(task, finalBantime, TimeUnit.MILLISECONDS);
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(CaseType.BAN.getEmbedColor())
                                        .setTimestamp(Instant.now())
                                        .setFooter("User-ID: "+user.getIdLong())
                                        .setTitle("[Ban] "+user.getAsTag())
                                        .addField("User", user.getAsMention(), true)
                                        .addField("Moderator", e.getAuthor().getAsMention(), true)
                                        .addField("Reason", Reason, true)
                                        .addField("Duration", Util.getLength(finalBantime/1000), false)
                                        .setThumbnail(user.getEffectiveAvatarUrl());
                                TextChannel logchannel = Util.getLogChannel(g);
                                if(logchannel != null)
                                {
                                    if(logchannel.getIdLong() == c.getIdLong())
                                    {
                                        c.sendMessage(builder.build()).queue(null, Util.handle(c));
                                        return;
                                    }
                                    c.sendMessage(builder.build()).queue(null, Util.handle(e.getChannel()));
                                    logchannel.sendMessage(builder.build()).queue(null, Util.handle(e.getChannel()));
                                    return;
                                }else {
                                    c.sendMessage(builder.build()).queue(null, Util.handle(e.getChannel()));
                                    return;
                                }

                            },
                            (error) ->
                            {
                                ErrorResponseException error1 = (ErrorResponseException)error;
                                c.sendMessage(Util.SimpleEmbed(Color.red,"An error occured\n`"+error1.getMeaning()+"`")).queue();
                                return;
                            }
                    );
                },
                (error) ->
                {
                    c.sendMessage(Util.SimpleEmbed(Color.red, "Invalid User!")).queue();
                    return;
                }
        );
    }

}
