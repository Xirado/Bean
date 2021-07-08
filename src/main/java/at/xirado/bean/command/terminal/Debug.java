package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug extends ConsoleCommand
{
    private static final Logger logger = LoggerFactory.getLogger(Debug.class);

    public Debug()
    {
        this.invoke = "debug";
        this.description = "Debug command";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        System.out.println(Bean.getInstance()
                .getJDA()
                .getGuildById(815597207617142814L)
                .getTextChannelById(815597207617142817L)
                .retrieveMessageById(858076088054972426L)
                .complete().getAuthor().getAsTag());
        Throwable t = new IllegalArgumentException("u hoe!");
        logger.error("Error!", t);
    }
}
