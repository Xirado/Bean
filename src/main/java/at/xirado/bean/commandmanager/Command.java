package at.xirado.bean.commandmanager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class Command
{
    public String invoke;
    public List<String> aliases;
    public List<Permission> neededPermissions;
    public List<Permission> neededBotPermissions;
    public String description;
    public CommandType commandType;
    public String usage;
    public boolean global;
    public List<Long> enabledGuilds;
    public boolean ifNotPermissedCheckForModRole;

    public String getInvoke()
    {
        return invoke;
    }

    public List<String> getAliases()
    {
        return aliases;
    }

    public List<Permission> getNeededPermissions()
    {
        return neededPermissions;
    }

    public List<Permission> getNeededBotPermissions()
    {
        return neededBotPermissions;
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
            return this.enabledGuilds.contains(guildID);
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

    public List<Long> getEnabledGuilds()
    {
        return enabledGuilds;
    }

    public Command(JDA jda)
    {
        this.global = true;
        this.enabledGuilds = new ArrayList<>();
        this.usage = null;
        this.invoke = null;
        this.description = null;
        this.commandType = CommandType.EXCLUDED;
        this.neededPermissions = new ArrayList<>();
        this.neededBotPermissions = new ArrayList<>();
        this.aliases = new ArrayList<>();
        this.ifNotPermissedCheckForModRole = false;
    }

    public abstract void executeCommand(GuildMessageReceivedEvent event, CommandContext context);

}
