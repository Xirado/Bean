/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.command;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public abstract class SlashCommand implements GenericCommand {
    private SlashCommandData commandData = null;

    private final EnumSet<Permission> requiredUserPermissions = EnumSet.noneOf(Permission.class);
    private final EnumSet<Permission> requiredBotPermissions = EnumSet.noneOf(Permission.class);
    private final Set<Long> enabledGuilds = new HashSet<>();
    private final EnumSet<CommandFlag> commandFlags = EnumSet.noneOf(CommandFlag.class);

    public void setCommandData(SlashCommandData commandData) {
        // Discord has a bug where setting this field on a guild command updates it even though they stay the same
        if (!Bean.getInstance().isDebug())
            commandData.setGuildOnly(true);
        this.commandData = commandData;
    }

    public void addRequiredBotPermissions(Permission... permissions) {
        requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public void addRequiredUserPermissions(Permission... permissions) {
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
        requiredUserPermissions.addAll(Arrays.asList(permissions));
    }

    public void addCommandFlags(CommandFlag... flags) {
        commandFlags.addAll(Arrays.asList(flags));
    }

    public void addEnabledGuilds(Long... ids) {
        enabledGuilds.addAll(Arrays.asList(ids));
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }

    @Override
    public EnumSet<Permission> getRequiredUserPermissions() {
        return requiredUserPermissions;
    }

    @Override
    public EnumSet<Permission> getRequiredBotPermissions() {
        return requiredBotPermissions;
    }

    @Override
    public Command.Type getType() {
        return Command.Type.SLASH;
    }

    @Override
    public Set<Long> getEnabledGuilds() {
        return enabledGuilds;
    }

    @Override
    public EnumSet<CommandFlag> getCommandFlags() {
        return commandFlags;
    }

    /**
     * Executes requested slash command
     *
     * @param event The SlashCommandInteractionEvent
     * @param ctx   Helpful methods in context of the event
     */
    public abstract void executeCommand(@Nonnull SlashCommandInteractionEvent event, @Nonnull SlashCommandContext ctx);
}
