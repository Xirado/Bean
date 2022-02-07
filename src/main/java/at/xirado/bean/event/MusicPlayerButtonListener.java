package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.music.GuildAudioPlayer;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MusicPlayerButtonListener extends ListenerAdapter
{

    public static final List<String> BUTTON_IDS = List.of("previous", "play", "next", "repeat");

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event)
    {
        if (!event.isFromGuild())
            return;

        if (!BUTTON_IDS.contains(event.getComponentId()))
            return;

        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());

        switch (event.getComponentId())
        {
            case "previous" -> Bean.getInstance().getLavalink().getExistingLink(event.getGuild()).getPlayer().seekTo(0L);
            case "play" -> guildAudioPlayer.getPlayer().setPaused(!guildAudioPlayer.getPlayer().isPaused());
            case "next" -> guildAudioPlayer.getScheduler().nextTrack();
            case "repeat" -> guildAudioPlayer.getScheduler().setRepeat(!guildAudioPlayer.getScheduler().isRepeat());
        }
        event.deferEdit().setActionRows(MusicUtil.getPlayerButtons(guildAudioPlayer.getPlayer().isPaused(), guildAudioPlayer.getScheduler().isRepeat())).queue();
    }
}
