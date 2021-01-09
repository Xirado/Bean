// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.settings;

import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.Collection;
import java.util.Collections;

public class Settings implements GuildSettingsProvider
{
    private final SettingsManager manager;
    protected long textId;
    protected long voiceId;
    protected long roleId;
    private int volume;
    private String defaultPlaylist;
    private boolean repeatMode;
    private String prefix;
    
    public Settings(final SettingsManager manager, final String textId, final String voiceId, final String roleId, final int volume, final String defaultPlaylist, final boolean repeatMode, final String prefix) {
        this.manager = manager;
        try {
            this.textId = Long.parseLong(textId);
        }
        catch (NumberFormatException e) {
            this.textId = 0L;
        }
        try {
            this.voiceId = Long.parseLong(voiceId);
        }
        catch (NumberFormatException e) {
            this.voiceId = 0L;
        }
        try {
            this.roleId = Long.parseLong(roleId);
        }
        catch (NumberFormatException e) {
            this.roleId = 0L;
        }
        this.volume = volume;
        this.defaultPlaylist = defaultPlaylist;
        this.repeatMode = repeatMode;
        this.prefix = prefix;
    }
    
    public Settings(final SettingsManager manager, final long textId, final long voiceId, final long roleId, final int volume, final String defaultPlaylist, final boolean repeatMode, final String prefix) {
        this.manager = manager;
        this.textId = textId;
        this.voiceId = voiceId;
        this.roleId = roleId;
        this.volume = volume;
        this.defaultPlaylist = defaultPlaylist;
        this.repeatMode = repeatMode;
        this.prefix = prefix;
    }
    
    public TextChannel getTextChannel(final Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(this.textId);
    }
    
    public VoiceChannel getVoiceChannel(final Guild guild) {
        return (guild == null) ? null : guild.getVoiceChannelById(this.voiceId);
    }
    
    public Role getRole(final Guild guild) {
        return (guild == null) ? null : guild.getRoleById(this.roleId);
    }
    
    public int getVolume() {
        return this.volume;
    }
    
    public String getDefaultPlaylist() {
        return this.defaultPlaylist;
    }
    
    public boolean getRepeatMode() {
        return this.repeatMode;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    @Override
    public Collection<String> getPrefixes() {
        return (this.prefix == null) ? Collections.EMPTY_SET : Collections.singleton(this.prefix);
    }
    
    public void setTextChannel(final TextChannel tc) {
        this.textId = ((tc == null) ? 0L : tc.getIdLong());
        this.manager.writeSettings();
    }
    
    public void setVoiceChannel(final VoiceChannel vc) {
        this.voiceId = ((vc == null) ? 0L : vc.getIdLong());
        this.manager.writeSettings();
    }
    
    public void setDJRole(final Role role) {
        this.roleId = ((role == null) ? 0L : role.getIdLong());
        this.manager.writeSettings();
    }
    
    public void setVolume(final int volume) {
        this.volume = volume;
        this.manager.writeSettings();
    }
    
    public void setDefaultPlaylist(final String defaultPlaylist) {
        this.defaultPlaylist = defaultPlaylist;
        this.manager.writeSettings();
    }
    
    public void setRepeatMode(final boolean mode) {
        this.repeatMode = mode;
        this.manager.writeSettings();
    }
    
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
        this.manager.writeSettings();
    }
}
