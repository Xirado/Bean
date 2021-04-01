package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.SlashCommand;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TestCommand extends SlashCommand
{
    public TestCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("test", "this command is only for test purposes")
            .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "this", "is a test").addChoice("that", "1").setRequired(true))
        );
        Global(false);
        setEnabledGuilds(Arrays.asList(815597207617142814L));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        ctx.reply(CommandContext.SUCCESS+" \""+event.getOption("this").getAsString()+"\"").setEphemeral(true).queue();
    }
}
