package at.xirado.bean.command.terminal;


import at.xirado.bean.command.ConsoleCommand;

public class Echo extends ConsoleCommand
{
    public Echo()
    {
        this.invoke = "echo";
        this.description = "Prints out text on the terminal";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if (args.length == 0)
        {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args)
        {
            sb.append(arg).append(" ");
        }
        String text = sb.toString().trim();
        System.out.println(text);

    }
}
