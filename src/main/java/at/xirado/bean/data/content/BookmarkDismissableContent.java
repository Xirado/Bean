package at.xirado.bean.data.content;

import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BookmarkDismissableContent implements MessageEmbedDismissable{

    @NotNull
    @Override
    public String getIdentifier() {
        return "bookmark";
    }

    @Nullable
    @Override
    public String getMediaUrl() {
        return "https://bean.bz/assets/content/bookmark.png";
    }

    @NotNull
    @Override
    public MessageEmbed get() {
        return EmbedUtil.defaultEmbedBuilder("Bookmark songs and playlists using the **/bookmark** command!\nHaving to always type the link to your favourite youtube or spotify playlist is annoying, isn't it?")
                .setImage(getMediaUrl())
                .setTitle("Did you know?")
                .build();
    }
}
