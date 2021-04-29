package at.xirado.bean.logging;

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
    public static volatile boolean startedSuccessfully = false;

    public static final AttributedStyle BLUE = AttributedStyle.DEFAULT.foreground(0x00, 0xDB, 0xFF);
    public static final AttributedStyle PINK = AttributedStyle.DEFAULT.foreground(0xFF, 0x00, 0xFF);



    public static final String LOGO = new AttributedStringBuilder().style(BLUE).append("           ______     ______     ______     __   __\n          ")
            .style(PINK).append("/").style(BLUE).append("\\  ").style(BLUE).append("== ").style(BLUE).append("\\   ").style(PINK).append("/").style(BLUE)
            .append("\\  ").style(BLUE).append("___").style(BLUE).append("\\   ").style(PINK).append("/").style(BLUE).append("\\").style(BLUE)
            .append("  __ ").style(BLUE).append("\\   ").style(PINK).append("/").style(BLUE).append("\\ ").style(BLUE).append("\"-.").style(BLUE)
            .append("\\ \\\n").style(PINK).append("          \\ ").style(BLUE).append("\\  ").style(BLUE).append("__<").style(PINK).append("   \\ ")
            .style(BLUE).append("\\  ").style(BLUE).append("__\\").style(PINK).append("   \\ ").style(BLUE).append("\\  __ \\  ").style(PINK).append("\\ ")
            .style(BLUE).append("\\ \\").style(BLUE).append("-.  ").style(BLUE).append("\\\n").style(PINK).append("           \\ ").style(BLUE)
            .append("\\_____\\  ").style(PINK).append("\\ ").style(BLUE).append("\\_____\\  ").style(PINK).append("\\ ").style(BLUE).append("\\_\\ \\_\\  ")
            .style(PINK).append("\\ ").style(BLUE).append("\\_\\").style(PINK).append("\\\"").style(BLUE).append("\\_\\\n").style(PINK).append("            \\/_____/   \\/_____/   \\/_/\\/_/   \\/_/ \\/_/\n\n                      Bean v").append(Bean.instance.VERSION).append(" by Xirado\n").toAnsi();



    public static void startShell(Runnable success)
    {
        Thread t = new Thread(() ->
        {
            AnsiConsole.systemInstall();
            String prompt = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0,255,255)).append("» ").style(AttributedStyle.DEFAULT).toAnsi();
            TerminalBuilder builder = TerminalBuilder.builder();
            builder.system(true);

            try
            {
                terminal = builder.build();
            } catch (IOException e)
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
            terminal.writer().println(LOGO);
            success.run();
            while (true) {
                String line;
                try {
                    line = reader.readLine(prompt, null, (MaskingCallback) null, null);
                    line = line.trim();
                    terminal.flush();
                    if(line.length() == 0) continue;
                    ParsedLine pl = reader.getParser().parse(line, 0);
                    String[] argv = pl.words().subList(1, pl.words().size()).toArray(new String[0]);
                    Bean.instance.consoleCommandManager.handleConsoleCommand(pl.word(), argv);

                } catch (UserInterruptException e)
                {
                    System.exit(0);
                }
                catch (Exception e)
                {
                    logger.error("An error occured", e);
                }
            }
        });
        t.setName("Terminal Worker");
        t.setDaemon(true);
        t.start();
    }
}
