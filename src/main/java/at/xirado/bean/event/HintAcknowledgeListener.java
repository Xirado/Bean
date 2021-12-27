package at.xirado.bean.event;

import at.xirado.bean.data.Hints;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class HintAcknowledgeListener extends ListenerAdapter
{

    private static final MessageEmbed CONFIRMATION_EMBED = EmbedUtil.defaultEmbedBuilder("Thank you for your feedback!").build();

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event)
    {
        String componentId = event.getComponentId();
        if (!componentId.startsWith("ackHint:"))
            return;
        String hint = componentId.substring(8);
        Hints.acknowledgeHint(event.getUser().getIdLong(), hint);
        event.editMessageEmbeds(CONFIRMATION_EMBED)
                .setActionRows(Collections.emptyList())
                .queue();
    }
}
