package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.data.content.DismissableState;
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
        var progress = Bean.getInstance().getDismissableContentManager().getProgress(event.getUser().getIdLong(), identifier, true);
        if (progress == null)
            return;

        progress.setState(DismissableState.ACKNOWLEDGED).update();
        event.editMessageEmbeds(CONFIRMATION_EMBED)
                .setActionRows(Collections.emptyList())
                .queue();
    }
}
