package at.xirado.bean.consolecommands;

import at.xirado.bean.Bean;
import at.xirado.bean.objects.ConsoleCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.LoggerFactory;

import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
public class SendMessage extends ConsoleCommand
{
    public static ch.qos.logback.classic.Logger logger =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SendMessage.class);

    public SendMessage()
    {
        this.invoke = "sendmessage";
        this.description = "Sends a message in a certain textchannel";
    }
    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if(args.length < 3)
        {
            System.out.println(ansi().fg(RED).a("Syntax Fehler: sendMessage [Guild] [TextChannel] [Nachricht]").toString());
            return;
        }

        Guild g = Bean.instance.jda.getGuildById(args[0]);
        if(g == null)
        {
            System.out.println(ansi().fg(RED).a("Fehler: Ungültige Guild-ID!"));
            return;
        }
        TextChannel tc = g.getTextChannelById(args[1]);
        if(tc == null)
        {
            System.out.println(ansi().fg(RED).a("Fehler: Ungültige Channel-ID!"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < args.length; i++) sb.append(args[i]).append(" ");
        tc.sendMessage(sb.toString().trim()).queue(
                (success) ->
                        System.out.println(ansi().fg(GREEN).a("Nachricht wurde gesendet!")),
                (error) ->
                {
                    System.out.println(ansi().fg(RED).a("Fehler: Nachricht konnte nicht gesendet werden!"));
                    logger.error(error.getMessage());
                }
        );

    }
}
