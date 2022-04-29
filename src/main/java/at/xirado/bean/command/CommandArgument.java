package at.xirado.bean.command;

import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandArgument
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandArgument.class);
    private static final Pattern MENTION_PATTERN = Pattern.compile("<@!?\\d+>\\s*?(\\w+)\\s*([\\s\\S]+)?");
    private final String command;
    private final String rawArguments;
    private final String[] args;

    public CommandArgument(String argumentString, String prefix)
    {
        Checks.notEmpty(argumentString, "Argument");
        Checks.notEmpty(prefix, "Prefix");
        String[] argumentArray = argumentString.split("\\s+");
        command = argumentArray[0].substring(prefix.length());
        rawArguments = argumentString.substring(argumentArray[0].length() + 1);
        List<String> arguments = new ArrayList<>(Arrays.asList(argumentArray).subList(1, argumentArray.length));
        args = new String[arguments.size()];
        arguments.toArray(args);
    }

    public CommandArgument(String argumentString, long userId)
    {
        Matcher matcher = MENTION_PATTERN.matcher(argumentString);
        if (!matcher.matches())
            throw new IllegalArgumentException("Input string does not match regex!");
        command = matcher.group(1);
        rawArguments = matcher.group(2);
        if (rawArguments != null)
            args = rawArguments.split("\\s+");
        else
            args = new String[0];
    }

    public String getCommandName()
    {
        return command;
    }

    public String[] toStringArray()
    {
        return args;
    }

    public String getRawArguments()
    {
        return rawArguments;
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
