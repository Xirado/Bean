// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.NowplayingHandler;
import com.jagrosh.jmusicbot.audio.PlayerManager;
import com.jagrosh.jmusicbot.gui.GUI;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader;
import com.jagrosh.jmusicbot.settings.SettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bot
{
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadpool;
    private final BotConfig config;
    private final SettingsManager settings;
    private final PlayerManager players;
    private final PlaylistLoader playlists;
    private final NowplayingHandler nowplaying;
    private boolean shuttingDown;
    private JDA jda;
    private GUI gui;
    
    public Bot(final EventWaiter waiter, final BotConfig config, final SettingsManager settings) {
        this.shuttingDown = false;
        this.waiter = waiter;
        this.config = config;
        this.settings = settings;
        this.playlists = new PlaylistLoader(config);
        this.threadpool = Executors.newSingleThreadScheduledExecutor();
        (this.players = new PlayerManager(this)).init();
        (this.nowplaying = new NowplayingHandler(this)).init();
    }
    
    public BotConfig getConfig() {
        return this.config;
    }
    
    public SettingsManager getSettingsManager() {
        return this.settings;
    }
    
    public EventWaiter getWaiter() {
        return this.waiter;
    }
    
    public ScheduledExecutorService getThreadpool() {
        return this.threadpool;
    }
    
    public PlayerManager getPlayerManager() {
        return this.players;
    }
    
    public PlaylistLoader getPlaylistLoader() {
        return this.playlists;
    }
    
    public NowplayingHandler getNowplayingHandler() {
        return this.nowplaying;
    }
    
    public JDA getJDA() {
        return this.jda;
    }
    
    public void closeAudioConnection(final long guildId) {
        final Guild guild = this.jda.getGuildById(guildId);
        if (guild != null) {
            this.threadpool.submit(() -> guild.getAudioManager().closeAudioConnection());
        }
    }
    
    public void resetGame() {
        final Activity game = (this.config.getGame() == null || this.config.getGame().getName().equalsIgnoreCase("none")) ? null : this.config.getGame();
        if (!Objects.equals(this.jda.getPresence().getActivity(), game)) {
            this.jda.getPresence().setActivity(game);
        }
    }
    
    public void shutdown() {
        if (this.shuttingDown) {
            return;
        }
        this.shuttingDown = true;
        this.threadpool.shutdownNow();
        if (this.jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            this.jda.getGuilds().stream().forEach(g -> {
                g.getAudioManager().closeAudioConnection();
                final AudioHandler ah = (AudioHandler)g.getAudioManager().getSendingHandler();
                if (ah != null) {
                    ah.stopAndClear();
                    ah.getPlayer().destroy();
                    this.nowplaying.updateTopic(g.getIdLong(), ah, true);
                }
                return;
            });
            this.jda.shutdown();
        }
        if (this.gui != null) {
            this.gui.dispose();
        }
        System.exit(0);
    }
    
    public void setJDA(final JDA jda) {
        this.jda = jda;
    }
    
    public void setGUI(final GUI gui) {
        this.gui = gui;
    }
}
