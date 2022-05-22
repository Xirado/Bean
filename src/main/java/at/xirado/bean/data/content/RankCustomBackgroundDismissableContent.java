package at.xirado.bean.data.content;

import checkers.nullness.quals.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;

public class RankCustomBackgroundDismissableContent implements MessageEmbedDismissable {

    @NonNull
    @Override
    public String getIdentifier() {
        return "rankcustombackground";
    }

    @NonNull
    @Override
    public String getMediaUrl() {
        return "https://bean.bz/assets/content/rank_custom_backgrounds.png";
    }

    @Nonnull
    @Override
    public MessageEmbed get() {
        return new EmbedBuilder()
                .setTitle("Did you know?")
                .setDescription("You can set your own custom background on your rank card using `/setxpcard`!")
                .setImage(getMediaUrl())
                .setColor(0x842BD7)
                .build();
    }
}
