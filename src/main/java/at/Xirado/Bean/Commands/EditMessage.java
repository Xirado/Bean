package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class EditMessage extends Command
{

    public EditMessage(JDA jda)
    {
        super(jda);
        this.invoke = "edit";
        this.description = "Edits a message posted by me";
        this.usage = "edit [#TextChannel] [Message-ID] [New Content]";
        this.neededPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.commandType = CommandType.ADMIN;
    }

    @Override
    public void execute(CommandEvent e) {
        String[] args = e.getArguments().getArguments();
        TextChannel channel = e.getChannel();
        Member m = e.getMember();
        if (args.length < 3)
        {
            e.replyErrorUsage();
            return;
        }
        String channelid = args[0].replaceAll("[^0-9]", "");
        TextChannel targetchannel = e.getGuild().getTextChannelById(channelid);
        if(targetchannel == null)
        {
            e.replyErrorUsage();
            return;
        }
        if(targetchannel.getGuild().getIdLong() != e.getGuild().getIdLong())
        {
            return;
        }
        targetchannel.retrieveMessageById(args[1]).queue(
                message ->
                {
                    if (!message.getAuthor().getId().equals(DiscordBot.instance.jda.getSelfUser().getId())) {
                        channel.sendMessage(Util.SimpleEmbed(Color.red, "This message was not sent by me!")).queue();
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for(int i = 2; i < args.length; i++)
                    {
                        sb.append(args[i]).append(" ");
                    }
                    String sbtostring = sb.toString();
                    sbtostring = sbtostring.substring(0, sbtostring.length()-1);
                    if(message.getEmbeds().size() == 1)
                    {
                        MessageEmbed me = message.getEmbeds().get(0);
                        EmbedBuilder b = new EmbedBuilder(me);
                        b.setDescription(sbtostring);
                        message.editMessage(b.build()).queue(
                                success ->
                                {
                                    channel.sendMessage(Util.SimpleEmbed(Color.green, "Message has been edited!")).queue();
                                    return;
                                },
                                Util.handle(channel)
                        );
                        return;
                    }
                    message.editMessage(sbtostring).queue(
                            success ->
                            {
                                channel.sendMessage(Util.SimpleEmbed(Color.green, "Message has been edited!")).queue();
                                return;
                            },
                            Util.handle(channel)
                    );
                },
                error ->
                {
                    channel.sendMessage(Util.SimpleEmbed(Color.red, "Invalid Message-ID")).queue();
                    return;
                }
        );
    }
}
