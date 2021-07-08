package at.xirado.bean.command.terminal;


import at.xirado.bean.command.ConsoleCommand;

public class Embed extends ConsoleCommand
{
    public Embed()
    {
        this.invoke = "embed";
        this.description = "Posts an embed";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {


    }
}
