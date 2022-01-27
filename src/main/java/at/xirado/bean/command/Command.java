package at.xirado.bean.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Command
{
    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases = new ArrayList<>();
    private final List<Permission> requiredPermissions = new ArrayList<>();
    private final List<Permission> requiredBotPermissions = new ArrayList<>();
    private final List<Long> allowedGuilds = new ArrayList<>();
    private final EnumSet<CommandFlag> commandFlags;

    protected Command(String name, String description, String usage)
    {
        Checks.notNull(name, "name");
        Checks.notNull(description, "description");
        Checks.notNull(usage, "usage");

        this.name = name;
        this.description = description;
        this.usage = usage;
        commandFlags = EnumSet.noneOf(CommandFlag.class);
    }


    public void addAliases(@NotNull String... aliases)
    {
        Checks.notNull(aliases, "Aliases");
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void addRequiredPermissions(@NotNull Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        this.requiredPermissions.addAll(Arrays.asList(permissions));
    }

    public void addRequiredBotPermissions(@NotNull Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public void addAllowedGuilds(@NotNull Long... guildIDs)
    {
        Checks.notEmpty(guildIDs, "Guild Ids");
        this.allowedGuilds.addAll(Arrays.asList(guildIDs));
    }

    public void addCommandFlags(@NotNull CommandFlag... commandFlags)
    {
        Checks.notNull(commandFlags, "CommandFlags");
        this.commandFlags.addAll(Arrays.asList(commandFlags));
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


    public @NotNull List<String> getAliases()
    {
        return Collections.unmodifiableList(aliases);
    }

    public @NotNull List<Permission> getRequiredPermissions()
    {
        return Collections.unmodifiableList(requiredPermissions);
    }

    public @NotNull List<Permission> getRequiredBotPermissions()
    {
        return Collections.unmodifiableList(requiredBotPermissions);
    }

    public @NotNull List<Long> getAllowedGuilds()
    {
        return Collections.unmodifiableList(allowedGuilds);
    }

    public @NotNull Set<CommandFlag> getCommandFlags()
    {
        return Collections.unmodifiableSet(commandFlags);
    }

    public boolean hasCommandFlag(CommandFlag flag)
    {
        return commandFlags.contains(flag);
    }

    public boolean isAvailableIn(long guildID)
    {
        if (hasCommandFlag(CommandFlag.PRIVATE_COMMAND)) {
            return getAllowedGuilds().contains(guildID);
        } else {
            return true;
        }
    }

    public abstract void executeCommand(GuildMessageReceivedEvent event, CommandContext context);

    @NonNls
    @NotNull
    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", usage='" + usage + '\'' +
                ", aliases=" + aliases +
                ", requiredPermissions=" + requiredPermissions +
                ", requiredBotPermissions=" + requiredBotPermissions +
                ", allowedGuilds=" + allowedGuilds +
                ", commandFlags=" + commandFlags +
                '}';
    }
}