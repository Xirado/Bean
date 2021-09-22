package at.xirado.bean.command.slashcommands;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.interactions.ButtonPaginator;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class TestCommand extends SlashCommand
{
    public TestCommand()
    {
        setCommandData(new CommandData("test", "this command is only for test purposes")
                .addOption(OptionType.BOOLEAN, "ephemeral", "if this message is ephemeral", true)
        );
        setGlobal(false);
        setEnabledGuilds(815597207617142814L);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        ButtonPaginator paginator = new ButtonPaginator.Builder(event.getJDA())
                .setColor(0x00ff00)
                .setEventWaiter(Bean.getInstance().getEventWaiter())
                .addAllowedUsers(event.getUser().getIdLong())
                .setFooter("Footer")
                .setItems(new String[]{"lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum","lorem", "ipsum"})
                .setTimeout(1, TimeUnit.MINUTES)
                .build();
        event.deferReply(true).queue(hook -> paginator.paginate(hook.sendMessage(""), 1));
    }
}
