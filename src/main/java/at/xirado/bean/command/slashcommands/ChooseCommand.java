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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChooseCommand extends SlashCommand {
    public ChooseCommand() {
        setCommandData(Commands.slash("choose", "Chooses between up to 10 things.")
                .addOption(OptionType.STRING, "1st", "First argument.", true)
                .addOption(OptionType.STRING, "2nd", "Second argument.", true)
                .addOption(OptionType.STRING, "3rd", "Third argument.")
                .addOption(OptionType.STRING, "4th", "Fourth argument.")
                .addOption(OptionType.STRING, "5th", "Fifth argument.")
                .addOption(OptionType.STRING, "6th", "Sixth argument.")
                .addOption(OptionType.STRING, "7th", "Seventh argument.")
                .addOption(OptionType.STRING, "8th", "Eighth argument.")
                .addOption(OptionType.STRING, "9th", "Ninth argument.")
                .addOption(OptionType.STRING, "10th", "Tenth argument.")
        );
    }


    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        List<OptionMapping> chooseOptions = event.getOptions();
        int i = ThreadLocalRandom.current().nextInt(event.getOptions().size());
        ctx.reply(String.format("I choose... %s", chooseOptions.get(i).getAsString())).queue();
    }
}
