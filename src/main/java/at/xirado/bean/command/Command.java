package at.xirado.bean.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public abstract class Command
{
    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;
    private final List<Permission> requiredPermissions;
    private final List<Permission> requiredBotPermissions;
    private final List<Long> allowedGuilds;
    private final EnumSet<CommandFlag> commandFlags;
    private CommandCategory commandCategory;

    public Command(String name, String description, String usage)
    {
        Checks.notNull(name, "name");
        Checks.notNull(description, "description");
        Checks.notNull(usage, "usage");
        this.name = name;
        this.description = description;
        this.usage = usage;
        aliases = new ArrayList<>();
        requiredPermissions = new ArrayList<>();
        requiredBotPermissions = new ArrayList<>();
        allowedGuilds = new ArrayList<>();
        commandFlags = EnumSet.noneOf(CommandFlag.class);
        commandCategory = CommandCategory.NONE;
    }


    public void setAliases(String... aliases)
    {
        Checks.notNull(aliases, "Aliases");
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void setRequiredPermissions(Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        this.requiredPermissions.addAll(Arrays.asList(permissions));
    }

    public void setRequiredBotPermissions(Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public void addAllowedGuilds(Long... guildIDs)
    {
        Checks.notEmpty(guildIDs, "Guild Ids");
        this.allowedGuilds.addAll(Arrays.asList(guildIDs));
    }

    public void setCommandFlags(CommandFlag... commandFlags)
    {
        Checks.notNull(commandFlags, "CommandFlags");
        this.commandFlags.addAll(Arrays.asList(commandFlags));
    }

    public void setCommandCategory(CommandCategory commandCategory)
    {
        Checks.notNull(commandCategory, "CommandCategory");
        this.commandCategory = commandCategory;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getUsage()
    {
        return usage;
    }


    public List<String> getAliases()
    {
        return aliases;
    }

    public List<Permission> getRequiredPermissions()
    {
        return requiredPermissions;
    }

    public List<Permission> getRequiredBotPermissions()
    {
        return requiredBotPermissions;
    }

    public List<Long> getAllowedGuilds()
    {
        return allowedGuilds;
    }

    public CommandCategory getCommandCategory()
    {
        return commandCategory;
    }

    public EnumSet<CommandFlag> getCommandFlags()
    {
        return commandFlags;
    }

    public boolean hasCommandFlag(CommandFlag flag)
    {
        return commandFlags.contains(flag);
    }

    public boolean isAvailableIn(long GuildID)
    {
        if (!hasCommandFlag(CommandFlag.PRIVATE_COMMAND)) return true;
        return getAllowedGuilds().contains(GuildID);
    }

    public abstract void executeCommand(MessageReceivedEvent event, CommandContext context);
}
