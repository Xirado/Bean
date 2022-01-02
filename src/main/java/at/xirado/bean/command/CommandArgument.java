package at.xirado.bean.command;

import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArgument
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandArgument.class);

    private final String command;
    private final String[] args;

    public CommandArgument(String argumentString, String prefix)
    {
        Checks.notEmpty(argumentString, "Argument");
        Checks.notEmpty(prefix, "Prefix");
        String[] argumentArray = argumentString.split("\\s+");
        command = argumentArray[0].substring(prefix.length());
        List<String> arguments = new ArrayList<>(Arrays.asList(argumentArray).subList(1, argumentArray.length));
        args = new String[arguments.size()];
        arguments.toArray(args);
    }

    public String getCommandName()
    {
        return command;
    }

    public String[] toStringArray()
    {
        return args;
    }

    public String toString(int startIndex)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++)
        {
            sb.append(args[i]).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString()
    {
        return toString(0);
    }
}
