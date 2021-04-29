package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.objects.SlashCommand;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Mock extends SlashCommand
{

    public Mock()
    {
        setCommandData(new CommandUpdateAction.CommandData("mock", "vAcCiNeS cAuSe AuTiSm")
            .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "text", "the text to mock")
                .setRequired(true)
            )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        String toMock = event.getOption("text").getAsString();
        StringBuilder sensitive = new StringBuilder();
        for(int i = 0; i < toMock.length(); i++)
        {
            if(i%2 == 0)
            {
                sensitive.append(String.valueOf(toMock.charAt(i)).toLowerCase());
            }
            else
            {
                sensitive.append(String.valueOf(toMock.charAt(i)).toUpperCase());
            }
        }
        ctx.reply("<:mock:773566020588666961> "+sensitive.toString()+" <:mock:773566020588666961>").queue();

    }
}
