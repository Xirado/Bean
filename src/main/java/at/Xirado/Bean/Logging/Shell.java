package at.Xirado.Bean.Logging;

import at.Xirado.Bean.Main.DiscordBot;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Shell
{
    public static LineReader reader = null;

    public static ch.qos.logback.classic.Logger logger =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Shell.class);

    public static Terminal terminal = null;
    public static volatile boolean startedSuccessfully = false;


    public static void startShell()
    {
        Thread t = new Thread(() ->
        {
            AnsiConsole.systemInstall();
            String prompt = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(0,255,255))
                    .append("> ")
                    .style(AttributedStyle.DEFAULT)
                    .toAnsi();
            TerminalBuilder builder = TerminalBuilder.builder();
            builder.system(true);

            try
            {
                terminal = builder.build();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
            String logo = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(0,255,255))
                    .append("" +
                    " ______                    _\n" +
                    " | ___ \\                  | |\n" +
                    " | |_/ / ___  __ _ _ __   | |__ ____\n" +
                    " | ___ \\/ _ \\/ _` | '_ \\  | '_ \\_  /\n" +
                    " | |_/ /  __/ (_| | | | |_| |_) / /\n" +
                    " \\____/ \\___|\\__,_|_| |_(_)_.__/___|\n")
                    .style(AttributedStyle.DEFAULT)
                    .toAnsi();
            System.out.println(logo);
            reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(null)
                    .parser(null)
                    .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                    .variable(LineReader.INDENTATION, 2)
                    .option(LineReader.Option.INSERT_BRACKET, true)
                    .build();
            System.setOut(CustomPrintStream.getPrintStream());
            System.setErr(CustomPrintStream.getPrintStream());
            startedSuccessfully = true;
            while (true) {
                String line;
                try {
                    line = reader.readLine(prompt, null, (MaskingCallback) null, null);
                    line = line.trim();
                    terminal.flush();
                    if(line.length() == 0) continue;
                    ParsedLine pl = reader.getParser().parse(line, 0);
                    String[] argv = pl.words().subList(1, pl.words().size()).toArray(new String[0]);
                    DiscordBot.instance.consoleCommandManager.handleConsoleCommand(pl.word(), argv);

                } catch (UserInterruptException e)
                {
                    System.exit(0);
                }
                catch (Exception e)
                {
                    String stacktrace = ExceptionUtils.getStackTrace(e);
                    logger.error(stacktrace);
                }
            }
        });
        t.setName("Shell");
        t.start();
    }
}
