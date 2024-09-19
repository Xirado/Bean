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

package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class AvatarCommand extends SlashCommand {
    public AvatarCommand() {
        setCommandData(Commands.slash("avatar", "Gets the avatar of a user.")
                .addOption(OptionType.USER, "user", "User to get the avatar from.", false)
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        OptionMapping option = event.getOption("user");
        User user = option != null ? option.getAsUser() : event.getUser();
        EmbedBuilder b = new EmbedBuilder()
                .setImage(user.getEffectiveAvatarUrl() + "?size=512")
                .setColor(Color.magenta)
                .setTimestamp(Instant.now())
                .setAuthor(ctx.getLocalized("commands.avatar_title", user.getAsTag()), null, user.getEffectiveAvatarUrl());

        ctx.reply(b.build()).queue();
    }
}
