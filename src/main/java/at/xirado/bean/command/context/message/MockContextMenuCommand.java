package at.xirado.bean.command.context.message;

import at.xirado.bean.command.context.MessageContextCommand;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MockContextMenuCommand extends MessageContextCommand
{

    public MockContextMenuCommand()
    {
        setCommandData(Commands.message("mock"));
    }

    @Override
    public void executeCommand(@NotNull MessageContextInteractionEvent event)
    {
        Message target = event.getTarget();
        String toMock = target.getContentRaw();

        if (toMock.isBlank())
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("This message has no content.")).setEphemeral(true).queue();
            return;
        }

        StringBuilder sensitive = new StringBuilder();
        for (int i = 0; i < toMock.length(); i++)
        {
            if (i % 2 == 0)
                sensitive.append(String.valueOf(toMock.charAt(i)).toLowerCase());
            else
                sensitive.append(String.valueOf(toMock.charAt(i)).toUpperCase());
        }
        event.reply("<:mock:773566020588666961> " + sensitive + " <:mock:773566020588666961>")
                .allowedMentions(Collections.emptyList())
                .addActionRow(Button.link(target.getJumpUrl(), "Jump to message"))
                .queue();
    }
}
