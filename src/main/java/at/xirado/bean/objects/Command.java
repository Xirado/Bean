package at.xirado.bean.objects;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.commandutil.CommandFlag;
import at.xirado.bean.misc.Checks;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
    private CommandCategory commandCategory;
    private final EnumSet<CommandFlag> commandFlags;

    public Command(String name, String description, String usage)
    {
        Checks.nonNull(name);
        Checks.nonNull(description);
        Checks.nonNull(usage);
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
        Checks.nonNull(aliases);
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void setRequiredPermissions(Permission... permissions)
    {
        Checks.nonNull(permissions);
        this.requiredPermissions.addAll(Arrays.asList(permissions));
    }

    public void setRequiredBotPermissions(Permission... permissions)
    {
        Checks.nonNull(permissions);
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public void addAllowedGuilds(Long... guildIDs)
    {
        Checks.nonNull(guildIDs);
        this.allowedGuilds.addAll(Arrays.asList(guildIDs));
    }

    public void setCommandFlags(CommandFlag... commandFlags)
    {
        Checks.nonNull(commandFlags);
        this.commandFlags.addAll(Arrays.asList(commandFlags));
    }

    public void setCommandCategory(CommandCategory commandCategory)
    {
        Checks.nonNull(commandCategory);
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
        if(!hasCommandFlag(CommandFlag.PRIVATE_COMMAND)) return true;
        return getAllowedGuilds().contains(GuildID);
    }

    public abstract void executeCommand(GuildMessageReceivedEvent event, CommandContext context);
}
