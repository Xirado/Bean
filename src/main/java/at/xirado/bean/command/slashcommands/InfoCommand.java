package at.xirado.bean.command.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.command.terminal.Info;
import at.xirado.bean.misc.EmbedUtil;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class InfoCommand extends SlashCommand
{
    public InfoCommand()
    {
        setCommandData(new CommandData("info", "shows info about the bot"));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        long currentTime = System.currentTimeMillis() / 1000;
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(EmbedUtil.DEFAULT_COLOR)
                .addField("Uptime", ctx.parseDuration(currentTime - Bean.START_TIME, " "), true)
                .addField("Memory", "**"+Info.convertBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())+"** / " +
                        "**"+Info.convertBytes(Runtime.getRuntime().maxMemory())+"**", true)
                .addField("Ping", "Gateway Ping: "+event.getJDA().getGatewayPing()+"ms", true)
                .addField("Compilation Date", "<t:"+(Bean.getBuildTime()/1000)+">", true)
                .setFooter("Bean "+Bean.getBeanVersion());
        event.replyEmbeds(builder.build()).queue();
    }
}
