package at.xirado.bean.command.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
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
        event.replyEmbeds(
                new EmbedBuilder()
                        .setColor(0x3EB489)
                        .setDescription("Uptime: " + ctx.parseDuration(currentTime - Bean.START_TIME, " ") + "\n\n" +
                                "Bean Remastered v" + Bean.getBeanVersion() + " (JDA " + jdaVersion+")")
                        .setFooter("OpenJDK "+javaVersion)
                        .build()
        ).queue();
    }
}
