// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.entities.Prompt;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OtherUtil
{
    public static final String NEW_VERSION_AVAILABLE = "There is a new version of JMusicBot available!\nCurrent version: %s\nNew Version: %s\n\nPlease visit https://github.com/jagrosh/MusicBot/releases/latest to get the latest release.";
    private static final String WINDOWS_INVALID_PATH = "c:\\windows\\system32\\";
    
    public static Path getPath(String path) {
        if (path.toLowerCase().startsWith("c:\\windows\\system32\\")) {
            final String filename = path.substring("c:\\windows\\system32\\".length());
            try {
                path = new File(JMusicBot.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath() + File.separator + filename;
            }
            catch (URISyntaxException ex) {}
        }
        return Paths.get(path, new String[0]);
    }
    
    public static String loadResource(final Object clazz, final String name) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(clazz.getClass().getResourceAsStream(name)))) {
            final StringBuilder sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append("\r\n").append(line));
            return sb.toString().trim();
        }
        catch (IOException ex) {
            return null;
        }
    }
    
    public static InputStream imageFromUrl(final String url) {
        if (url == null) {
            return null;
        }
        try {
            final URL u = new URL(url);
            final URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
            return urlConnection.getInputStream();
        }
        catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }
    
    public static Activity parseGame(final String game) {
        if (game == null || game.trim().isEmpty() || game.trim().equalsIgnoreCase("default")) {
            return null;
        }
        final String lower = game.toLowerCase();
        if (lower.startsWith("playing")) {
            return Activity.playing(makeNonEmpty(game.substring(7).trim()));
        }
        if (lower.startsWith("listening to")) {
            return Activity.listening(makeNonEmpty(game.substring(12).trim()));
        }
        if (lower.startsWith("listening")) {
            return Activity.listening(makeNonEmpty(game.substring(9).trim()));
        }
        if (lower.startsWith("watching")) {
            return Activity.watching(makeNonEmpty(game.substring(8).trim()));
        }
        if (lower.startsWith("streaming")) {
            final String[] parts = game.substring(9).trim().split("\\s+", 2);
            if (parts.length == 2) {
                return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/" + parts[0]);
            }
        }
        return Activity.playing(game);
    }
    
    public static String makeNonEmpty(final String str) {
        return (str == null || str.isEmpty()) ? "\u200b" : str;
    }
    
    public static OnlineStatus parseStatus(final String status) {
        if (status == null || status.trim().isEmpty()) {
            return OnlineStatus.ONLINE;
        }
        final OnlineStatus st = OnlineStatus.fromKey(status);
        return (st == null) ? OnlineStatus.ONLINE : st;
    }
    
    public static String checkVersion(final Prompt prompt) {
        final String version = getCurrentVersion();
        final String latestVersion = getLatestVersion();
        if (latestVersion != null && !latestVersion.equals(version)) {
            prompt.alert(Prompt.Level.WARNING, "Version", String.format("There is a new version of JMusicBot available!\nCurrent version: %s\nNew Version: %s\n\nPlease visit https://github.com/jagrosh/MusicBot/releases/latest to get the latest release.", version, latestVersion));
        }
        return version;
    }
    
    public static String getCurrentVersion() {
        if (JMusicBot.class.getPackage() != null && JMusicBot.class.getPackage().getImplementationVersion() != null) {
            return JMusicBot.class.getPackage().getImplementationVersion();
        }
        return "UNKNOWN";
    }
    
    public static String getLatestVersion() {
        try {
            final Response response = new OkHttpClient.Builder().build().newCall(new Request.Builder().get().url("https://api.github.com/repos/jagrosh/MusicBot/releases/latest").build()).execute();
            final ResponseBody body = response.body();
            if (body != null) {
                try (final Reader reader = body.charStream()) {
                    final JSONObject obj = new JSONObject(new JSONTokener(reader));
                    return obj.getString("tag_name");
                }
                finally {
                    response.close();
                }
            }
            return null;
        }
        catch (IOException | JSONException | NullPointerException ex3) {

            return null;
        }
    }
}
