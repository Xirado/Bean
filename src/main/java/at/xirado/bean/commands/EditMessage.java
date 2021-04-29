package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;

public class EditMessage extends Command
{

    public EditMessage()
    {
        super("edit", "Edits a message posted by me", "edit [#channel] [messageID] [Text]");
        setRequiredPermissions(Permission.ADMINISTRATOR);
        setCommandCategory(CommandCategory.ADMIN);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        TextChannel channel = event.getChannel();
        Member m = context.getMember();
        if (args.length < 3)
        {
            context.replyErrorUsage();
            return;
        }
        String channelid = args[0].replaceAll("[^0-9]", "");
        TextChannel targetchannel = event.getGuild().getTextChannelById(channelid);
        if(targetchannel == null)
        {
            context.replyErrorUsage();
            return;
        }
        if(targetchannel.getGuild().getIdLong() != event.getGuild().getIdLong())
        {
            return;
        }
        targetchannel.retrieveMessageById(args[1]).queue(
                message ->
                {
                    if (!message.getAuthor().getId().equals(Bean.instance.jda.getSelfUser().getId())) {
                        channel.sendMessage(Util.SimpleEmbed(Color.red, context.getLocalized("commands.message_not_sent_by_me"))).queue();
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
                                    channel.sendMessage(Util.SimpleEmbed(Color.green, context.getLocalized("commands.message_edited"))).queue();
                                },
                                Util.handle(channel)
                        );
                        return;
                    }
                    message.editMessage(sbtostring).queue(
                            success ->
                            {
                                channel.sendMessage(Util.SimpleEmbed(Color.green, context.getLocalized("commands.message_edited"))).queue();
                            },
                            Util.handle(channel)
                    );
                },
                error ->
                {
                    channel.sendMessage(Util.SimpleEmbed(Color.red, context.getLocalized("commands.message_not_exists"))).queue();
                }
        );
    }
}
