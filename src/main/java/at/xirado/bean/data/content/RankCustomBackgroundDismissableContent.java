package at.xirado.bean.data.content;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankCustomBackgroundDismissableContent implements MessageEmbedDismissable {

    @NotNull
    @Override
    public String getIdentifier() {
        return "rankcustombackground";
    }

    @Nullable
    @Override
    public String getMediaUrl() {
        return "https://bean.bz/assets/content/rank_custom_backgrounds.png";
    }

    @NotNull
    @Override
    public MessageEmbed get() {
        return new EmbedBuilder()
                .setTitle("Did you know?")
                .setDescription("You can set your own custom background on your rank card using `/setxpcard`!")
                .setImage(getMediaUrl())
                .setColor(getEmbedColor())
                .build();
    }
}
