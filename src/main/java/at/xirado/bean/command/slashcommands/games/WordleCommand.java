package at.xirado.bean.command.slashcommands.games;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.manager.WordleManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WordleCommand extends SlashCommand
{
    public WordleCommand()
    {
        setCommandData(Commands.slash("wordle", "Play/Continue today's wordle quiz"));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        var wordle = WordleManager.createWordleGame(event.getUser().getIdLong());

        try
        {
            event.replyFile(wordle.generateImage(), "image.png").setEphemeral(true).addActionRow(Button.success("wordle_next", "Enter first word")).queue();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
