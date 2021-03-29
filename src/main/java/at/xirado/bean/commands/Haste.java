package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;

public class Haste extends Command
{
    public Haste(JDA jda)
    {
        super(jda);
        this.invoke = "haste";
        this.aliases = Arrays.asList("posthaste", "hastebin");
        this.commandType = CommandType.UTILITIES;
        this.description = "Creates a haste and gives you the link";
        this.usage = "haste [Content]";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        String[] args = event.getArguments().toStringArray();
        if(args.length == 0)
        {
            event.replyErrorUsage();
            return;
        }
        event.deleteInvokeMessage();
        String haste = event.getArguments().toString(0);
        String linkresponse = Util.postHaste(haste, false);
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.decode("#93bdca"))
                .setFooter("Hastebin")
                .setTimestamp(Instant.now())
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl())
                .setDescription("Your haste is finished: "+linkresponse);
        event.reply(builder.build());

    }
}
