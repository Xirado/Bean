package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.SlashCommand;
import net.dv8tion.jda.api.commands.CommandHook;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Choose extends SlashCommand
{
    public Choose()
    {
        setCommandData(new CommandUpdateAction.CommandData("choose", "Chooses between 2 things")
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "1st", "the first argument").setRequired(true))
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "2nd", "the second argument").setRequired(true))
        );
    }


    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @NotNull Member sender, @NotNull CommandContext ctx)
    {
        String firstOption = event.getOption("1st").getAsString();
        String secondOption = event.getOption("2nd").getAsString();
        int i = new Random().nextInt(2);
        if(i == 0)
        {
            ctx.reply("I choose... "+firstOption).queue();
        }else
        {
            ctx.reply("I choose... "+secondOption).queue();
        }
    }
}
