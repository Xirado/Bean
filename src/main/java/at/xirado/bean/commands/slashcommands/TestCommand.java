package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.SlashCommand;
import at.xirado.bean.commandmanager.SlashCommandContext;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class TestCommand extends SlashCommand
{
    public TestCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("test", "this command is only for test purposes")
                .addOption(new CommandUpdateAction.OptionData(Command.OptionType.INTEGER, "time", "The duration to be parsed").setRequired(true))
        );
        Global(false);
        setEnabledGuilds(Collections.singletonList(815597207617142814L));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        long time = event.getOption("time").getAsLong();
        ctx.reply(ctx.parseDuration(time, " ")).queue();
    }



    public void execute(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        long time = event.getOption("time").getAsLong();
        ctx.reply(ctx.parseDuration(time, " ")).queue();
    }

}
