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
import at.xirado.bean.mee6.MEE6Queue;
import at.xirado.bean.mee6.MEE6Request;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.util.Collections;

public class Mee6TransferCommand extends SlashCommand {
    public Mee6TransferCommand() {
        setCommandData(Commands.slash("mee6transfer", "Transfer-command to migrate MEE6 XP to Bean for all found members."));
        addRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        Guild guild = event.getGuild();
        MEE6Queue queue = Bean.getInstance().getMEE6Queue();
        if (queue.hasPendingRequest(guild.getIdLong())) {
            event.replyEmbeds(EmbedUtil.warningEmbed("Hey hey! The migration process of your server is still going on!")).setEphemeral(true).queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("**WARNING** This migrates all MEE6 XP! **WARNING**\n\n" +
                        "This action cannot be undone.\n" +
                        "**Existing xp will be reset for all found users**\n" +
                        "Are you sure you want to continue?\n\n" +
                        "Disclaimer: On larger servers (Over 10k users), it could take a while until all users are migrated!");

        event.deferReply(true).queue();

        event.getHook().sendMessageEmbeds(builder.build())
                .addActionRow(Button.danger("mee6transfer:" + event.getIdLong(), "Continue").withEmoji(Emoji.fromUnicode("⚠")))
                .setEphemeral(true)
                .queue(
                        (hook) ->
                        {
                            hook.getJDA().listenOnce(ButtonInteractionEvent.class)
                                    .filter((e) -> e.getComponentId().equals("mee6transfer:" + event.getIdLong()))
                                    .timeout(Duration.ofSeconds(30), () -> {
                                        MessageEmbed embed = new EmbedBuilder()
                                                .setColor(Color.RED)
                                                .setDescription("Action timed out! Please try again!")
                                                .build();

                                        hook.editMessageEmbeds(embed).setComponents(Collections.emptyList()).queue();
                                    })
                                    .subscribe((e) -> {
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription("Thank you! We will notify you via DM when we're finished. (Make sure you have DMs turned on!)")
                                                .setColor(0x452350);

                                        queue.addRequest(new MEE6Request(guild.getIdLong(), event.getUser().getIdLong()));

                                        e.editMessageEmbeds(embedBuilder.build()).setComponents(Collections.emptyList()).queue();
                                    });
                        }
                );
    }
}
