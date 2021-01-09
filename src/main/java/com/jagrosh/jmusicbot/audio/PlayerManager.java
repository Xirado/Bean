// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;

public class PlayerManager extends DefaultAudioPlayerManager
{
    private final Bot bot;
    
    public PlayerManager(final Bot bot) {
        this.bot = bot;
    }
    
    public void init() {
        AudioSourceManagers.registerRemoteSources(this);
        AudioSourceManagers.registerLocalSource(this);
        this.source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }
    
    public Bot getBot() {
        return this.bot;
    }
    
    public boolean hasHandler(final Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }
    
    public AudioHandler setUpHandler(final Guild guild) {
        AudioHandler handler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            final AudioPlayer player = this.createPlayer();
            player.setVolume(this.bot.getSettingsManager().getSettings(guild).getVolume());
            handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        }
        else {
            handler = (AudioHandler)guild.getAudioManager().getSendingHandler();
        }
        return handler;
    }
}
