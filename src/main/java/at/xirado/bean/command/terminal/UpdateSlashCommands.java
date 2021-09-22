package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.log.MCColor;

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
        Bean.getInstance().getSlashCommandHandler().updateCommands((x) -> System.out.println(MCColor.translate("&aQueued "+x.size()+" commands!")), Throwable::printStackTrace);
    }
}
