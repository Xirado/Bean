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

package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.LevelingUtils;
import at.xirado.bean.data.content.*;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

public class RankCommand extends SlashCommand {
    public RankCommand() {
        setCommandData(Commands.slash("rank", "Shows a users level.")
                .addOption(OptionType.USER, "user", "Member to get the level from.", false)
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        InteractionHook commandHook = event.getHook();

        event.deferReply(false).queue();

        OptionMapping optionData = event.getOption("user");
        User user = optionData == null ? event.getUser() : optionData.getAsUser();

        long xp = LevelingUtils.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());

        if (xp < 100) {
            if (optionData == null)
                commandHook.sendMessage("You are not yet ranked!").queue();
            else
                commandHook.sendMessage("This member is not yet ranked!").queue();
            return;
        }

        byte[] rankCard = LevelingUtils.generateLevelCard(user, event.getGuild());
        if (rankCard == null) {
            commandHook.sendMessageEmbeds(EmbedUtil.errorEmbed("Could not load rank card! Please try again later!")).queue();
            return;
        }

        commandHook.sendFiles(FileUpload.fromData(rankCard, "card.png")).queue();

        long userId = event.getUser().getIdLong();
        DismissableContentManager contentManager = Bean.getInstance().getDismissableContentManager();

        if (!contentManager.hasProgress(userId, RankCustomBackgroundDismissableContent.class)) {
            if (!LevelingUtils.getPreferredCard(event.getUser()).startsWith("card")) { // Default backgrounds
                // They have a custom background so no need to show it to them.
                contentManager.createDismissableContent(userId, RankCustomBackgroundDismissableContent.class, DismissableState.AWARE);
            } else {
                DismissableProgress progress = contentManager.createDismissableContent(
                        userId, RankCustomBackgroundDismissableContent.class, DismissableState.SEEN
                );
                var dismissable = (MessageEmbedDismissable) progress.getDismissable();
                commandHook.sendMessageEmbeds(dismissable.get())
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
