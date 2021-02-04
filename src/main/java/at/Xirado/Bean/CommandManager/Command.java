package at.Xirado.Bean.CommandManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;

public abstract class Command
{
    public String invoke;
    public String[] aliases;
    public Permission[] neededPermissions;
    public Permission[] neededBotPermissions;
    public String description;
    public CommandType commandType;
    public String usage;
    public boolean global;
    public Long[] enabledGuilds;

    public String getInvoke()
    {
        return invoke;
    }

    public String[] getAliases()
    {
        return aliases;
    }

    public Permission[] getNeededPermissions()
    {
        return neededPermissions;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean accessibleOn(Long guildID)
    {
        if(this.isGlobal())
        {
            return true;
        }else
        {
            return Arrays.asList(this.enabledGuilds).contains(guildID);
        }
    }
    public CommandType getCommandType()
    {
        return commandType;
    }

    public String getUsage()
    {
        return usage;
    }

    public boolean isGlobal()
    {
        return global;
    }

    public Long[] getEnabledGuilds()
    {
        return enabledGuilds;
    }

    public Command(JDA jda)
    {
        this.global = true;
        this.enabledGuilds = new Long[]{};
        this.usage = null;
        this.invoke = null;
        this.description = null;
        this.commandType = CommandType.EXCLUDED;
        this.neededPermissions = new Permission[]{};
        this.neededBotPermissions = new Permission[]{};
        this.aliases = new String[]{};
    }

    public abstract void executeCommand(CommandEvent event);

}
