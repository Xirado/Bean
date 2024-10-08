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
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.urbandictionary.UrbanDefinition;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrbanDictionaryCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrbanDictionaryCommand.class);
    private static final Pattern PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

    public UrbanDictionaryCommand() {
        setCommandData(Commands.slash("urban", "Searches for urbandictionary.com definitions.")
                .addOptions(new OptionData(OptionType.STRING, "phrase", "Phrase to search for.")
                        .setRequired(true))
                .addOptions(new OptionData(OptionType.INTEGER, "definition", "Index.")
                        .setRequired(false))
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        String phrase = event.getOption("phrase").getAsString();
        int index = event.getOption("definition") != null ? (int) event.getOption("definition").getAsLong() : 1;
        if (index < 1) index = 1;
        event.deferReply().queue();
        DataObject object;
        try {
            String url = "http://api.urbandictionary.com/v0/define?term=" + phrase.replaceAll("\\s+", "+");
            Response response = Bean.getInstance().getOkHttpClient()
                    .newCall(new Request.Builder().url(url).build()).execute();
            object = DataObject.fromJson(response.body().bytes());
            response.close();
        } catch (Exception ex) {
            LOGGER.error("Could not get data from API!", ex);
            event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred, please try again later.")).queue();
            return;
        }

        List<UrbanDefinition> results = object.getArray("list").stream(DataArray::getObject).map(UrbanDefinition::fromData).toList();
        if (results.isEmpty()) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(0x1D2439)
                    .setTitle(ctx.getLocalized("commands.urban.not_found"))
                    .setTimestamp(Instant.now());
            event.getHook().sendMessageEmbeds(builder.build()).queue();
            return;
        }
        if (results.size() < index)
            index = results.size();
        UrbanDefinition result = results.get(index - 1);
        String description = result.getDefinition();
        Matcher matcher = PATTERN.matcher(description);
        description = matcher.replaceAll(
                match -> "[" + match.group().replaceAll("\\[|\\]", "") + "]" + "(https://urbandictionary.com/define.php?term=" + match.group().replaceAll("\\s+", "+").replaceAll("\\[|\\]", "") + ")");
        if (description.length() > 4096) {
            String replaceString = "... [**" + ctx.getLocalized("general.read_more") + "**](" + result.getPermalink() + ")";
            String split = description.substring(0, 4096 - replaceString.length());
            description = split + replaceString;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x1D2439)
                .setTitle(result.getWord(), result.getPermalink())
                .setFooter(Util.ordinal(index) + " definition", "https://bean.bz/assets/udlogo.png")
                .setDescription(description);
        String example = result.getExample();
        if (example != null) {
            boolean alreadyRecursive = example.startsWith("*") && example.endsWith("*");
            Matcher matcher2 = PATTERN.matcher(example);
            example = matcher2.replaceAll(
                    match -> "[" + match.group().replaceAll("\\[|\\]", "") + "]" + "(https://www.urbandictionary.com/define.php?term=" + match.group().replaceAll(" ", "+").replaceAll("\\[|\\]", "") + ")");
            if (example.length() <= 1022)
                builder.addField(ctx.getLocalized("general.example"), ((alreadyRecursive) ? (example) : ("*" + example + "*")), false);
        }
        String authorUrl = "https://www.urbandictionary.com/author.php?author=";
        authorUrl += result.getAuthor().replaceAll("\\s+", "%20");
        builder.setTimestamp(Instant.parse(result.getWrittenOn()));
        builder.addField(ctx.getLocalized("general.author"), "[" + result.getAuthor() + "](" + authorUrl + ")\n\uD83D\uDC4D " + result.getUpvotes() + " \uD83D\uDC4E " + result.getDownvotes(), false);
        event.getHook().sendMessageEmbeds(builder.build()).queue();
    }
}
