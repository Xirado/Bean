package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Tempban extends Command
{

    public Tempban(JDA jda)
    {
        super(jda);
    }

    @Override
    public void execute(CommandEvent e) {
        String[] args = e.getArguments().getArguments();
        Guild g = e.getGuild();
        Member m = e.getMember();
        TextChannel c = e.getChannel();
        if(args.length < 4)
        {
            e.replyErrorUsage();
            return;
        }
        int time;
        try
        {
            time = Integer.parseInt(args[1]);
        }catch (NumberFormatException e1)
        {
            c.sendMessage(Util.SimpleEmbed(Color.red,"Invalid time!")).queue();
            return;
        }
        char timeunit;
        String timeformat = args[2];
        switch (timeformat.toLowerCase()) {
            case "s":
            case "second":
            case "seconds":
            case "sec":
                timeunit = 's';
                break;
            case "minute":
            case "minutes":
            case "min":
            case "m":
                timeunit = 'm';
                break;
            case "hours":
            case "hour":
            case "hr":
            case "h":
                timeunit = 'h';
                break;
            case "day":
            case "days":
            case "d":
                timeunit = 'd';
                break;
            case "week":
            case "weeks":
            case "w":
                timeunit = 'w';
                break;
            case "month":
            case "months":
                timeunit = 'M';
                break;
            default:
                c.sendMessage(Util.SimpleEmbed(Color.red,"Invalid Timeunit!")).queue();
                return;

        }
        StringBuilder sb = new StringBuilder();
        for(int i = 3; i < args.length; i++)
        {
            sb.append(args[i]+" ");
        }
        String Reason = sb.toString();
        Reason = Reason.substring(0,Reason.length()-1);
        String ID = args[0].replaceAll("[^0-9]", "");
        String finalReason = Reason;
        String finalReason1 = Reason;
        DiscordBot.instance.jda.retrieveUserById(ID).queue(
                (user) ->
                {
                    Long bantime = null;
                    switch (timeunit)
                    {
                        case 's':
                            bantime = (long) time;
                            break;
                        case 'm':
                            bantime = (long) time*60;
                            break;
                        case 'h':
                            bantime = (long) time*60*60;
                            break;
                        case 'd':
                            bantime = (long) time*60*60*24;
                            break;
                        case 'w':
                            bantime = (long) time*60*60*24*7;
                            break;
                        case 'M':
                            bantime = (long) time*60*60*24*30;
                            break;
                        default:
                            break;
                    }
                    if(g.isMember(user))
                    {
                        Member targetmember = g.getMember(user);
                        if(!m.canInteract(targetmember))
                        {
                            c.sendMessage(Util.SimpleEmbed(Color.red,"You cannot ban this user!")).queue();
                            return;
                        }
                        if(!g.getMember(DiscordBot.instance.jda.getSelfUser()).canInteract(targetmember))
                        {
                            c.sendMessage(Util.SimpleEmbed(Color.red,"I cannot ban this user!")).queue();
                            return;
                        }
                    }
                    final String wholereason = "banned ("+Util.getLength(bantime)+") by "+e.getAuthor().getAsTag()+" for "+finalReason;
                    long finalBantime = bantime;
                    g.ban(user,0, wholereason).queue(
                            (success) ->
                            {
                                Util.addBan(g.getIdLong(),user.getIdLong(),(System.currentTimeMillis()/1000)+finalBantime);
                                Runnable task = () -> {
                                    g.unban(user).queue();
                                    Util.removeBan(g.getIdLong(), user.getIdLong());
                                };
                                DiscordBot.instance.scheduledExecutorService.schedule(task, finalBantime, TimeUnit.SECONDS);
                                EmbedBuilder builder = new EmbedBuilder()
                                        .setColor(Color.decode("#FEFEFE"))
                                        .setTimestamp(Instant.now())
                                        .setFooter("ID: "+user.getIdLong())
                                        .setTitle("User banned.")
                                        .addField("Moderator", e.getAuthor().getAsMention(), true)
                                        .addField("Banned", user.getAsMention(), true)
                                        .addField("Reason", finalReason1, true)
                                        .addField("Duration", Util.getLength(finalBantime), false)
                                        .setThumbnail(user.getAvatarUrl());
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

    /*@Override
    public List<Permission> neededPermissions() {
        ArrayList<Permission> perms = new ArrayList<>();
        perms.add(Permission.BAN_MEMBERS);
        return perms;
    }

    @Override
    public String getInvoke() {
        return "tempban";
    }

    @Override
    public String getDescription() {
        return "Temporary bans a member";
    }

    @Override
    public String getUsage() {
        return "tempban [@User/ID] [time] [s,m,h,d,w] [reason]";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }*/
}
