package at.xirado.bean.data.content;

import net.dv8tion.jda.api.entities.MessageEmbed;

public interface MessageEmbedDismissable extends IDismissable<MessageEmbed> {

    default int getEmbedColor() {
        return 0x842BD7;
    }

}
