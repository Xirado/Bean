package at.xirado.bean.command;

import java.util.ArrayList;
import java.util.List;

public abstract class ConsoleCommand
{
    public String invoke;
    public List<String> aliases;
    public String description;

    public ConsoleCommand()
    {
        this.invoke = null;
        this.aliases = new ArrayList<>();
    }

    public List<String> getAliases()
    {
        return this.aliases;
    }

    public String getInvoke()
    {
        return this.invoke;
    }

    public String getDescription()
    {
        return description;
    }

    public abstract void executeCommand(String invoke, String[] args);
}
