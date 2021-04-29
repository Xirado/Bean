package at.xirado.bean.consolecommands;

import at.xirado.bean.objects.ConsoleCommand;

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
