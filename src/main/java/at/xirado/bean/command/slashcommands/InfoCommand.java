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

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Metrics;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class InfoCommand extends SlashCommand {
    public InfoCommand() {
        setCommandData(Commands.slash("info", "Shows info about the bot."));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        long currentTime = System.currentTimeMillis() / 1000;
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(EmbedUtil.DEFAULT_COLOR)
                .addField("Uptime", ctx.parseDuration(currentTime - Bean.START_TIME, " "), true)
                .addField("Memory", "**" + convertBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "** / " +
                        "**" + convertBytes(Runtime.getRuntime().maxMemory()) + "**", true)
                .addField("Ping", "Gateway Ping: " + event.getJDA().getGatewayPing() + "ms\nREST Ping: " + (int) Metrics.DISCORD_REST_PING.get() + "ms", true)
                .addField("Compilation Date", "<t:" + (Bean.getBuildTime() / 1000) + ">", true)
                .setFooter("Bean " + Bean.getBeanVersion());
        event.replyEmbeds(builder.build()).queue();
    }

    public static String convertBytes(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1048576) return bytes / 1024 + " kB";
        if (bytes < 1073741824) return (bytes / 1024) / 1024 + " MB";
        return ((bytes / 1024) / 1024) / 1024 + " GB";
    }
}
