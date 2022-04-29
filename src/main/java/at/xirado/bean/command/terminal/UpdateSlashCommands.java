package at.xirado.bean.command.terminal;

import at.xirado.bean.command.ConsoleCommand;

public class UpdateSlashCommands extends ConsoleCommand
{
    public UpdateSlashCommands()
    {
        this.invoke = "updateslashcommands";
        this.description = "Tells Discord to update all commands";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
    }
}
