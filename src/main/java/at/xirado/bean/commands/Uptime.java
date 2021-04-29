package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;

public class Uptime extends Command
{


    public Uptime()
    {
        super("uptime", "Shows the bot's uptime", "uptime");
        setCommandCategory(CommandCategory.BEAN);

    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        long currentTime = System.currentTimeMillis() / 1000;
        TextChannel c = event.getChannel();
        c.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.green)
                        .setTimestamp(Instant.now())
                        .setDescription("Uptime: " + context.parseDuration(currentTime - Bean.STARTTIME, " ") + "")
                        .build()
        ).queue();
    }
}
