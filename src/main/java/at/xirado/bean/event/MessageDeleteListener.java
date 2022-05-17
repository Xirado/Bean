package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.music.GuildAudioPlayer;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MessageDeleteListener extends ListenerAdapter {
    private final Logger LOG = LoggerFactory.getLogger(Bean.class);

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (!event.isFromGuild())
            return;

        long messageId = event.getMessageIdLong();

        Set<GuildAudioPlayer> players = Bean.getInstance().getAudioManager().getAudioPlayers();

        GuildAudioPlayer player = players.stream()
                .filter(pl -> pl.getGuildId() == event.getGuild().getIdLong())
                .findFirst().orElse(null);

        if (player != null && player.getOpenPlayer() != null) {
            if (player.getOpenPlayer().getMessageId() == messageId) {
                player.setOpenPlayer(null);
                LOG.info("Player for guild {} has been deleted!", event.getGuild().getName());
            }
        }
    }
}
