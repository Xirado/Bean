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
        String jdaVersion = JDAInfo.VERSION;
        String javaVersion = System.getProperty("java.version");
        String lavaPlayerVersion = PlayerLibrary.VERSION;
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(EmbedUtil.DEFAULT_COLOR)
                .addField("Uptime", ctx.parseDuration(currentTime - Bean.START_TIME, " "), false)
                .addField("Memory", "max memory: `"+ Info.convertBytes(Runtime.getRuntime().maxMemory())+"`\n" +
                        "allocated memory: `"+Info.convertBytes(Runtime.getRuntime().totalMemory())+"`\n" +
                        "free memory: `"+Info.convertBytes(Runtime.getRuntime().freeMemory())+"`\n" +
                        "used memory: `"+Info.convertBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())+"`", false)
                .addField("Java", "OpenJDK "+javaVersion, true)
                .addField("JDA", "JDA "+jdaVersion, true)
                .addField("Lavaplayer", "Lavaplayer "+lavaPlayerVersion, true)
                .setFooter("Bean "+Bean.getBeanVersion());
        event.replyEmbeds(builder.build()).queue();
    }
}
