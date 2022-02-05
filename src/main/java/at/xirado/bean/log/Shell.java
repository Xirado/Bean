package at.xirado.bean.log;

import at.xirado.bean.Bean;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Shell
{
    public static LineReader reader = null;

    private static final Logger logger = LoggerFactory.getLogger(Shell.class);

    public static Terminal terminal = null;
    public static volatile boolean started = false;

    public static final AttributedStyle BLUE = AttributedStyle.DEFAULT.foreground(0x00, 0xDB, 0xFF);
    public static final AttributedStyle PRIMARY = AttributedStyle.DEFAULT.foreground(255, 111, 97);


    public static final int LOGO_LENGTH = 44;


    public static void startShell()
    {
        Thread t = new Thread(() ->
        {
            AnsiConsole.systemInstall();
            String prompt = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0, 255, 255)).append("» ").style(AttributedStyle.DEFAULT).toAnsi();
            TerminalBuilder builder = TerminalBuilder.builder();
            builder.system(true);

            try
            {
                terminal = builder.build();
            }
            catch (IOException e)
            {
                logger.error("Could not build Terminal!", e);
            }
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
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
            terminal.writer().println(getLogo());
            started = true;
            while (true)
            {
                String line;
                try
                {
                    line = reader.readLine(prompt, null, (MaskingCallback) null, null);
                    line = line.trim();
                    terminal.flush();
                    if (line.length() == 0) continue;
                    ParsedLine pl = reader.getParser().parse(line, 0);
                    String[] argv = pl.words().subList(1, pl.words().size()).toArray(new String[0]);
                    Bean.getInstance().getConsoleCommandManager().handleConsoleCommand(pl.word(), argv);

                }
                catch (UserInterruptException e)
                {
                    System.exit(0);
                }
                catch (Exception e)
                {
                    logger.error("An error occured", e);
                }
            }
        });
        t.setName("Terminal Thread");
        t.setDaemon(true);
        t.start();
    }

    public static void awaitReady() throws InterruptedException
    {
        if (started) return;
        while (!started)
        {
            Thread.onSpinWait();
        }
    }

    public static String centerText(String text)
    {
        int width = terminal.getWidth();
        text = text.trim();
        int textLength = text.length();
        int spaces = (width / 2) - (textLength / 2);
        return " ".repeat(Math.max(0, spaces)) + text;
    }

    public static String getLogo()
    {
        String logo =
                """
                        ▄▄▄▄· ▄▄▄ . ▄▄▄·  ▐ ▄\s
                        ▐█ ▀█▪▀▄.▀·▐█ ▀█ •█▌▐█
                        ▐█▀▀█▄▐▀▀▪▄▄█▀▀█ ▐█▐▐▌
                        ██▄▪▐█▐█▄▄▌▐█ ▪▐▌██▐█▌
                        ·▀▀▀▀  ▀▀▀  ▀  ▀ ▀▀ █▪
                        """;
        int width = terminal.getWidth();
        String[] split = logo.split("\n");
        AttributedStringBuilder builder = new AttributedStringBuilder();
        builder.style(PRIMARY);
        for (String s : split)
        {
            int repeat = (width / 2) - (s.length() / 2);
            if (repeat < 1) repeat = 1;
            builder.append(" ".repeat(repeat)).append(s).append("\n");
        }
        String version = "Bean " + Bean.getBeanVersion() + " by Xirado";
        builder.append(" ".repeat((width / 2) - (version.length() / 2))).append(version + "\n");
        return builder.toAnsi();
    }

    public static void println(String text)
    {
        System.out.println(text);
    }

    public static void printErr(String text)
    {
        String ansiString = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(255, 45, 0))
                .append("Error: ")
                .append(text)
                .toAnsi();
        System.out.println(ansiString);
    }
}
