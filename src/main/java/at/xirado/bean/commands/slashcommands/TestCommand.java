package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.listeners.ButtonListener;
import at.xirado.bean.misc.AES;
import at.xirado.bean.objects.SlashCommand;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
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
        try
        {
            AES aes = ButtonListener.aes;
            ReplyAction action = event.reply("hello").setEphemeral(event.getOption("ephemeral").getAsBoolean());
            Button accept = Button.success(aes.encrypt("accept"), "Regeln akzeptieren").withEmoji(Emoji.ofUnicode("✅"));
            Button leave = Button.danger(aes.encrypt("leave"), "Server verlassen").withEmoji(Emoji.ofUnicode("\uD83D\uDEAA"));
            action.addActionRows(ActionRow.of(accept, leave));
            action.queue();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
