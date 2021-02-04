package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;

public class Mute extends Command
{
    public Mute(JDA jda)
    {
        super(jda);
        this.invoke = "mute";
        this.commandType = CommandType.MODERATION;
        this.description = "Mutes a member";
        this.neededPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.usage = "mute [@User/ID] [duration] (Optional Reason)";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        String[] args = event.getArguments().getArguments();

    }
}
