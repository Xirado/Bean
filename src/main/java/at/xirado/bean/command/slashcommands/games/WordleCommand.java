package at.xirado.bean.command.slashcommands.games;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
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

        if (WordleManager.hasFinishedDaily(event.getUser().getIdLong()) || WordleManager.hasPlayedToday(event.getUser().getIdLong()))
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("You already finished today's wordle quiz! Try again " + WordleManager.getDiscordRelativeTimeUntilMidnight())).queue();
            return;
        }
        var wordle = WordleManager.createWordleGame(event.getUser().getIdLong());

        try
        {
            event.replyFile(wordle.generateImage(), "image.png")
                    .setEphemeral(true)
                    .addActionRow(Button.success("wordle_next", wordle.getCurrentTry() == 0 ? "Enter first word" : "Try again"))
                    .queue();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
