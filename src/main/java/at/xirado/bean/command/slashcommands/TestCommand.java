package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestCommand extends SlashCommand
{
    public TestCommand()
    {
        setCommandData(new CommandData("test", "this command is only for test purposes")
                .addOption(OptionType.BOOLEAN, "ephemeral", "if this message is ephemeral", true)
        );
        Global(false);
        setEnabledGuilds(815597207617142814L);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        System.out.println(JDAInfo.VERSION);
        event.getGuild().createTextChannel("test123").setTopic("Hello123").queue();

        EmbedBuilder builder = new EmbedBuilder()
                .setDescription("<a:kekW:859813384244166746>");
        if (event.getOption("ephemeral").getAsBoolean())
        {
            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
            return;
        } else
        {
            event.replyEmbeds(builder.build()).queue();
        }

    }
}
