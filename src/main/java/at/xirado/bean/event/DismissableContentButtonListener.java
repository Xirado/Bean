package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.data.content.DismissableState;
import at.xirado.bean.data.content.Feature;
import at.xirado.bean.data.content.Status;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class DismissableContentButtonListener extends ListenerAdapter {

    private static final MessageEmbed CONFIRMATION_EMBED = EmbedUtil.defaultEmbedBuilder("You may delete this message now.").build();

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        var componentId = event.getComponentId();
        if (!componentId.startsWith("dismissable:"))
            return;
        var identifier = componentId.substring(12);

        var contentManager = Bean.getInstance().getDismissableContentManager();
        var feature = Feature.fromIdentifier(identifier);
        var state = contentManager.getState(event.getUser().getIdLong(), feature);
        if (state == null)
            return;

        state.updateStatus(Status.ACKNOWLEDGED);
        event.editMessageEmbeds(CONFIRMATION_EMBED)
                .setActionRows(Collections.emptyList())
                .queue();
    }
}
