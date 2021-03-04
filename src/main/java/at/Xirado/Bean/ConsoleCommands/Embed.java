package at.Xirado.Bean.ConsoleCommands;

import at.Xirado.Bean.CommandManager.ConsoleCommand;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;

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
