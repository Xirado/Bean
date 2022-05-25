package at.xirado.bean.data.content

import net.dv8tion.jda.api.entities.MessageEmbed

interface MessageEmbedDismissable : IDismissable<MessageEmbed> {
    val embedColor: Int
        get() = 0x842BD7
}
