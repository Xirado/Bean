package at.xirado.bean.data.content

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

object CustomRankBackgroundContent : MessageEmbedDismissable {
    override val identifier: String = "rankcustombackground"
    override val mediaUrl: String = "https://bean.bz/assets/content/rank_custom_backgrounds.png"
    override val value: MessageEmbed =
        EmbedBuilder()
            .setTitle("Did you know?")
            .setDescription("You can set your own custom background on your rank card using `/setxpcard`!")
            .setImage(mediaUrl)
            .setColor(embedColor)
            .build()
}
