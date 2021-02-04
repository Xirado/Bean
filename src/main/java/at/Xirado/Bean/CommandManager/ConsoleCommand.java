package at.Xirado.Bean.CommandManager;

public abstract class ConsoleCommand
{
    public String invoke;

    public ConsoleCommand()
    {
        this.invoke = null;
    }


    public abstract void executeCommand(String invoke, String[] args);
}
