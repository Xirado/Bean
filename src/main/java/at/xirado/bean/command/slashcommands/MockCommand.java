package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockCommand extends SlashCommand
{

    public MockCommand()
    {
        setCommandData(new CommandData("mock", "Mock something.")
                .addOption(OptionType.STRING, "text", "Text to mock.", true)
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        String toMock = event.getOption("text").getAsString();
        StringBuilder sensitive = new StringBuilder();
        for (int i = 0; i < toMock.length(); i++)
        {
            if (i % 2 == 0)
                sensitive.append(String.valueOf(toMock.charAt(i)).toLowerCase());
            else
                sensitive.append(String.valueOf(toMock.charAt(i)).toUpperCase());
        }
        ctx.reply("<:mock:773566020588666961> " + sensitive + " <:mock:773566020588666961>").queue();

    }
}
