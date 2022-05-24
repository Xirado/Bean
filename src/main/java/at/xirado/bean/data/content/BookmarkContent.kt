package at.xirado.bean.data.content

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

object BookmarkContent : MessageEmbedDismissable {
    override val identifier: String = "bookmark"
    override val mediaUrl: String = "https://bean.bz/assets/content/bookmark.png"
    override val value: MessageEmbed =
        EmbedBuilder()
            .setTitle("Did you know?")
            .setDescription("Bookmark songs and playlists using the **/bookmark** command!\nHaving to always type the link to your favourite youtube or spotify playlist is annoying, isn't it?")
            .setImage(mediaUrl)
            .setColor(embedColor)
            .build()
}
