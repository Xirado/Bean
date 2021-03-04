package at.xirado.bean.commands;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;

public class Mute extends Command
{
    public Mute(JDA jda)
    {
        super(jda);
        this.invoke = "mute";
        this.commandType = CommandType.MODERATION;
        this.description = "Mutes a member";
        this.neededPermissions = Arrays.asList(Permission.ADMINISTRATOR);
        this.usage = "mute [@User/ID] [duration] (Optional Reason)";
    }

    @Override
    public void executeCommand(CommandEvent event)
    {
        String[] args = event.getArguments().toStringArray();

    }
}
