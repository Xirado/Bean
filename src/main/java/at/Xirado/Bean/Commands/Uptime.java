package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;

public class Uptime extends Command
{


    public Uptime(JDA jda)
    {
        super(jda);
        this.invoke = "uptime";
        this.description = "Shows the bot's uptime";
        this.usage = "uptime";
        this.commandType = CommandType.BEAN;

    }

    @Override
    public void execute(CommandEvent e)
    {
        long thistime = System.currentTimeMillis() / 1000;
        TextChannel c = e.getChannel();
        c.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.decode("#FEFEFE"))
                        .setTimestamp(Instant.now())
                        .setFooter("Developed by Xirado")
                        .setDescription("Uptime: `" + Util.getLength(thistime - DiscordBot.instance.STARTTIME) + "`")
                        .build()
        ).queue();
    }
}
