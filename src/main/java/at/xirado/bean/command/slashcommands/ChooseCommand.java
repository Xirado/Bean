package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChooseCommand extends SlashCommand
{
    public ChooseCommand()
    {
        setCommandData(new CommandData("choose", "Chooses between 2 things.")
                .addOption(OptionType.STRING, "1st", "First argument.", true)
                .addOption(OptionType.STRING, "2nd", "Second argument.", true)
                .addOption(OptionType.STRING, "3rd", "Third argument.", false)
                .addOption(OptionType.STRING, "4th", "Fourth argument.", false)
                .addOption(OptionType.STRING, "5th", "Fifth argument.", false)
                .addOption(OptionType.STRING, "6th", "Sixth argument.", false)
                .addOption(OptionType.STRING, "7th", "Seventh argument.", false)
                .addOption(OptionType.STRING, "8th", "Eighth argument.", false)
                .addOption(OptionType.STRING, "9th", "Ninth argument.", false)
                .addOption(OptionType.STRING, "10th", "Tenth argument.", false)
        );
        setRunnableInDM(true);
    }


    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        List<OptionMapping> chooseOptions = event.getOptions();
        if(checkOptions(chooseOptions)){
            ctx.reply("Trying to exploit bots and annoying other people is not nice :(").setEphemeral(true).queue();
            return;
        }
        int i = ThreadLocalRandom.current().nextInt(event.getOptions().size());
        ctx.reply(String.format("I choose... %s", chooseOptions.get(i).getAsString())).queue();
    }

    public boolean checkOptions(List<OptionMapping> options){ //checks if a option could lead to a mass-ping
        for(OptionMapping option : options){
            if(option.getAsString().contains("@everyone") || option.getAsString().contains("@here"))
                return true;
        }
        return false;
    }
}
