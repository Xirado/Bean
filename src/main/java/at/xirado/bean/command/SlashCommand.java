package at.xirado.bean.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandPermission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public abstract class SlashCommand implements GenericCommand
{
    private SlashCommandData commandData = null;

    private final EnumSet<Permission> requiredUserPermissions = EnumSet.noneOf(Permission.class);
    private final EnumSet<Permission> requiredBotPermissions = EnumSet.noneOf(Permission.class);
    private final Set<Long> enabledGuilds = new HashSet<>();
    private final EnumSet<CommandFlag> commandFlags = EnumSet.noneOf(CommandFlag.class);

    public void setCommandData(SlashCommandData commandData)
    {
        commandData.setGuildOnly(true);
        this.commandData = commandData;
    }

    public void addRequiredBotPermissions(Permission... permissions)
    {
        requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public void addRequiredUserPermissions(Permission... permissions)
    {
        commandData.setDefaultPermissions(CommandPermission.enabledFor(permissions));
        requiredUserPermissions.addAll(Arrays.asList(permissions));
    }

    public void addCommandFlags(CommandFlag... flags)
    {
        commandFlags.addAll(Arrays.asList(flags));
    }

    public void addEnabledGuilds(Long... ids)
    {
        enabledGuilds.addAll(Arrays.asList(ids));
    }

    @Override
    public CommandData getCommandData()
    {
        return commandData;
    }

    @Override
    public EnumSet<Permission> getRequiredUserPermissions()
    {
        return requiredUserPermissions;
    }

    @Override
    public EnumSet<Permission> getRequiredBotPermissions()
    {
        return requiredBotPermissions;
    }

    @Override
    public Command.Type getType()
    {
        return Command.Type.SLASH;
    }

    @Override
    public Set<Long> getEnabledGuilds()
    {
        return enabledGuilds;
    }

    @Override
    public EnumSet<CommandFlag> getCommandFlags()
    {
        return commandFlags;
    }

    /**
     * Executes requested slash command
     *
     * @param event  The SlashCommandInteractionEvent
     * @param ctx    Helpful methods in context of the event
     */
    public abstract void executeCommand(@Nonnull SlashCommandInteractionEvent event, @Nonnull SlashCommandContext ctx);

    public void handleAutocomplete(@Nonnull CommandAutoCompleteInteractionEvent event) throws Exception {};

}
