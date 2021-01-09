// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.settings;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;

public class SettingsManager implements GuildSettingsManager
{
    private final HashMap<Long, Settings> settings;
    
    public SettingsManager() {
        this.settings = new HashMap<Long, Settings>();
        try {
            final JSONObject loadedSettings = new JSONObject(new String(Files.readAllBytes(OtherUtil.getPath("serversettings.json"))));
            final HashMap<Long, Settings> settings = new HashMap<>();
            loadedSettings.keySet().forEach(id -> {
                final JSONObject o = loadedSettings.getJSONObject(id);
                final Settings value = new Settings(this, o.has("text_channel_id") ? o.getString("text_channel_id") : null, o.has("voice_channel_id") ? o.getString("voice_channel_id") : null, o.has("dj_role_id") ? o.getString("dj_role_id") : null, o.has("volume") ? o.getInt("volume") : 100, o.has("default_playlist") ? o.getString("default_playlist") : null, o.has("repeat") && o.getBoolean("repeat"), o.has("prefix") ? o.getString("prefix") : null);
                settings.put((Long)Long.parseLong(id), value);
            });
        }
        catch (IOException | JSONException ex2) {
            LoggerFactory.getLogger("Settings").warn("Failed to load server settings (this is normal if no settings have been set yet): " + ex2);
        }
    }
    
    @Override
    public Settings getSettings(final Guild guild) {
        return this.getSettings(guild.getIdLong());
    }
    
    public Settings getSettings(final long guildId) {
        return this.settings.computeIfAbsent(guildId, id -> this.createDefaultSettings());
    }
    
    private Settings createDefaultSettings() {
        return new Settings(this, 0L, 0L, 0L, 100, null, false, null);
    }
    
    protected void writeSettings() {
        final JSONObject obj = new JSONObject();
        final JSONObject jsonObject = new JSONObject();
        this.settings.keySet().stream().forEach(key -> {
            final JSONObject o = new JSONObject();
            final Settings s = this.settings.get(key);
            if (s.textId != 0L) {
                o.put("text_channel_id", Long.toString(s.textId));
            }
            if (s.voiceId != 0L) {
                o.put("voice_channel_id", Long.toString(s.voiceId));
            }
            if (s.roleId != 0L) {
                o.put("dj_role_id", Long.toString(s.roleId));
            }
            if (s.getVolume() != 100) {
                o.put("volume", s.getVolume());
            }
            if (s.getDefaultPlaylist() != null) {
                o.put("default_playlist", s.getDefaultPlaylist());
            }
            if (s.getRepeatMode()) {
                o.put("repeat", true);
            }
            if (s.getPrefix() != null) {
                o.put("prefix", s.getPrefix());
            }
            jsonObject.put(Long.toString(key), o);
            return;
        });
        try {
            Files.write(OtherUtil.getPath("serversettings.json"), obj.toString(4).getBytes(), new OpenOption[0]);
        }
        catch (IOException ex) {
            LoggerFactory.getLogger("Settings").warn("Failed to write to file: " + ex);
        }
    }
}
