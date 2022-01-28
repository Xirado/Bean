package at.xirado.bean.command.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.command.terminal.Info;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoCommand extends SlashCommand
{
    public InfoCommand()
    {
        setCommandData(new CommandData("info", "Shows info about the bot."));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        long currentTime = System.currentTimeMillis() / 1000;
        event.getJDA().getRestPing().queue(restPing -> {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(EmbedUtil.DEFAULT_COLOR)
                    .addField("Uptime", ctx.parseDuration(currentTime - Bean.START_TIME, " "), true)
                    .addField("Memory", "**" + Info.convertBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "** / " +
                            "**" + Info.convertBytes(Runtime.getRuntime().maxMemory()) + "**", true)
                    .addField("Available Lavalink Nodes", "**" + Util.getAvailableLavalinkNodes() + "**", true)
                    .addField("Ping", "Gateway Ping: " + event.getJDA().getGatewayPing() + "ms\nREST Ping: " + restPing + "ms", true)
                    .addField("Compilation Date", "<t:" + (Bean.getBuildTime() / 1000) + ">", true)
                    .setFooter("Bean " + Bean.getBeanVersion());
            event.replyEmbeds(builder.build()).queue();
        });
    }
}
