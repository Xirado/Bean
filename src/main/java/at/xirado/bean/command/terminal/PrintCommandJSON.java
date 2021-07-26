package at.xirado.bean.command.terminal;

import at.xirado.bean.command.ConsoleCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

public class PrintCommandJSON extends ConsoleCommand
{
    public PrintCommandJSON()
    {
        this.invoke = "printcommands";
        this.description = "Prints the raw JSON of all slash commands";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        TextChannel channel = null;
        for (int i = 0; i < 5; i++)
        {
            channel.sendMessage("Message "+i).flatMap(message -> message.editMessage(message.getId())).queue();
        }
    }
}
