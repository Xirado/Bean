package at.xirado.bean.data.content;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nonnull;

public interface MessageEmbedDismissable extends IDismissable<MessageEmbed> {

    @Nonnull
    default ActionRow getButtonLayout() {
        return ActionRow.of(
                Button.secondary("dismissable:" + getIdentifier(), "Don't show this again")
        );
    }
}
