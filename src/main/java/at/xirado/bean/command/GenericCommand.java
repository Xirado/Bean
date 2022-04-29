package at.xirado.bean.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.EnumSet;
import java.util.Set;

public interface GenericCommand
{
    CommandData getCommandData();

    EnumSet<Permission> getRequiredUserPermissions();

    EnumSet<Permission> getRequiredBotPermissions();

    Command.Type getType();

    default boolean isGlobal()
    {
        return getEnabledGuilds().isEmpty();
    }

    Set<Long> getEnabledGuilds();

    EnumSet<CommandFlag> getCommandFlags();
}
