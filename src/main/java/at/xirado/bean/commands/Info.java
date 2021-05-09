package at.xirado.bean.commands;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Info extends Command
{


    public Info()
    {
        super("info", "Shows infos about me", "info");
        setCommandCategory(CommandCategory.BEAN);

    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        long currentTime = System.currentTimeMillis() / 1000;
        String jdaVersion = JDAInfo.VERSION;
        String javaVersion = System.getProperty("java.version");
        TextChannel c = event.getChannel();
        c.sendMessage(
                new EmbedBuilder()
                        .setColor(0x3EB489)
                        .setDescription("Uptime: " + context.parseDuration(currentTime - Bean.STARTTIME, " ") + "\n\n" +
                                "Bean "+Bean.getInstance().VERSION+", JDA "+jdaVersion+" (Java "+javaVersion+")")
                        .build()
        ).queue();
    }
}
