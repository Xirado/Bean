package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.log.ConsoleUtils;
import at.xirado.bean.log.MCColor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;


public class SendMessage extends ConsoleCommand {
    public static ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SendMessage.class);

    public SendMessage() {
        this.invoke = "sendmessage";
        this.description = "Sends a message in a certain textchannel";
    }

    @Override
    public void executeCommand(String invoke, String[] args) {
        if (args.length < 3) {
            System.out.println(ConsoleUtils.error("&cUse &8'&csendmessage {guild} {channel} {message}&8'"));
            return;
        }

        Guild g = Bean.getInstance().getShardManager().getGuildById(args[0]);
        if (g == null) {
            System.out.println(ConsoleUtils.error("Invalid guild!"));
            return;
        }
        TextChannel tc = g.getTextChannelById(args[1]);
        if (tc == null) {
            System.out.println(ConsoleUtils.error("Invalid channel!"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) sb.append(args[i]).append(" ");
        try {
            tc.sendMessage(sb.toString().trim()).queue(
                    (success) ->
                            System.out.println(MCColor.translate("&aMessage was sent!")),
                    (error) ->
                    {
                        System.out.println(ConsoleUtils.error("Could not send message!\n" + error.getMessage()));
                    }
            );
        } catch (Exception ex) {
            System.out.println(ConsoleUtils.error("Could not send message: " + ExceptionUtils.getStackTrace(ex)));
        }

    }
}
