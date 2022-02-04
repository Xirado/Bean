package at.xirado.bean.command.context.user;

import at.xirado.bean.command.context.UserContextCommand;
import at.xirado.bean.command.slashcommands.SlapCommand;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class SlapContextMenuCommand extends UserContextCommand
{
    public SlapContextMenuCommand()
    {
        setCommandData(Commands.user("slap"));
    }

    @Override
    public void executeCommand(@NotNull UserContextInteractionEvent event)
    {
        User author = event.getUser();
        User target = event.getTarget();
        if (target.getIdLong() == event.getUser().getIdLong())
        {
            event.replyEmbeds(EmbedUtil.defaultEmbed("Why do you want to slap yourself :(")).setEphemeral(true).queue();
            return;
        }
        boolean reversed = (1 + ThreadLocalRandom.current().nextInt(10)) == 1;
        User slapper = reversed ? target : author;
        User victim = reversed ? author : target;
        event.deferReply().queue();
        try
        {
            byte[] image = SlapCommand.generateImage(slapper, victim);
            WebhookMessageAction<Message> action = event.getHook().sendFile(image, "slap.png");
            if (reversed)
                action.setContent("Lol, better luck next time.");
            action.queue();
        } catch (IOException ex)
        {
            LoggerFactory.getLogger(SlapCommand.class).error("Could not generate image!", ex);
            event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred!")).setEphemeral(true).queue();
        }
    }
}
