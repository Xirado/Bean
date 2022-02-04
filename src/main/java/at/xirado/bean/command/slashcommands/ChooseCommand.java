package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChooseCommand extends SlashCommand
{
    public ChooseCommand()
    {
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
        setRunnableInDM(true);
    }


    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        List<OptionMapping> chooseOptions = event.getOptions();
        int i = ThreadLocalRandom.current().nextInt(event.getOptions().size());
        ctx.reply(String.format("I choose... %s", chooseOptions.get(i).getAsString())).queue();
    }
}
