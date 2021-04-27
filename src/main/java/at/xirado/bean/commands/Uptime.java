package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        long currentTime = System.currentTimeMillis() / 1000;
        TextChannel c = event.getChannel();
        c.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.green)
                        .setTimestamp(Instant.now())
                        .setDescription("Uptime: " + context.parseDuration(currentTime - DiscordBot.STARTTIME, " ") + "")
                        .build()
        ).queue();
    }
}
