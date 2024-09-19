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

package at.xirado.bean.command.slashcommands.moderation;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

public class UnbanCommand extends SlashCommand {
    public UnbanCommand() {
        setCommandData(Commands.slash("unban", "Unbans a user from a server.")
                .addOption(OptionType.USER, "user", "User to unban.", true)
        );
        addRequiredUserPermissions(Permission.BAN_MEMBERS);
        addRequiredBotPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        User user = event.getOption("user").getAsUser();
        event.deferReply(true)
                .flatMap(ban -> guild.unban(user))
                .flatMap(ban -> event.getHook().sendMessageEmbeds(EmbedUtil.successEmbed(user.getAsTag() + " has been unbanned!")))
                .queue(null,
                        new ErrorHandler().handle(ErrorResponse.UNKNOWN_BAN, (e) -> event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("This user is not banned!")).queue()));
    }
}
