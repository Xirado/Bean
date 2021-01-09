// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class BotConfig
{
    private final Prompt prompt;
    private static final String CONTEXT = "Config";
    private static final String START_TOKEN = "/// START OF JMUSICBOT CONFIG ///";
    private static final String END_TOKEN = "/// END OF JMUSICBOT CONFIG ///";
    private Path path;
    private String token;
    private String prefix;
    private String altprefix;
    private String helpWord;
    private String playlistsFolder;
    private String successEmoji;
    private String warningEmoji;
    private String errorEmoji;
    private String loadingEmoji;
    private String searchingEmoji;
    private boolean stayInChannel;
    private boolean songInGame;
    private boolean npImages;
    private boolean updatealerts;
    private boolean useEval;
    private boolean dbots;
    private long owner;
    private long maxSeconds;
    private OnlineStatus status;
    private Activity game;
    private Config aliases;
    private boolean valid;
    
    public BotConfig(final Prompt prompt) {
        this.path = null;
        this.valid = false;
        this.prompt = prompt;
    }
    
    public void load() {
        this.valid = false;
        try {
            this.path = OtherUtil.getPath(System.getProperty("config.file", System.getProperty("config", "config.txt")));
            if (this.path.toFile().exists()) {
                if (System.getProperty("config.file") == null) {
                    System.setProperty("config.file", System.getProperty("config", "config.txt"));
                }
                ConfigFactory.invalidateCaches();
            }
            final Config config = ConfigFactory.load();
            this.token = config.getString("token");
            this.prefix = config.getString("prefix");
            this.altprefix = config.getString("altprefix");
            this.helpWord = config.getString("help");
            this.owner = config.getLong("owner");
            this.successEmoji = config.getString("success");
            this.warningEmoji = config.getString("warning");
            this.errorEmoji = config.getString("error");
            this.loadingEmoji = config.getString("loading");
            this.searchingEmoji = config.getString("searching");
            this.game = OtherUtil.parseGame(config.getString("game"));
            this.status = OtherUtil.parseStatus(config.getString("status"));
            this.stayInChannel = config.getBoolean("stayinchannel");
            this.songInGame = config.getBoolean("songinstatus");
            this.npImages = config.getBoolean("npimages");
            this.updatealerts = config.getBoolean("updatealerts");
            this.useEval = config.getBoolean("eval");
            this.maxSeconds = config.getLong("maxtime");
            this.playlistsFolder = config.getString("playlistsfolder");
            this.aliases = config.getConfig("aliases");
            this.dbots = (this.owner == 113156185389092864L);
            boolean write = false;
            if (this.token == null || this.token.isEmpty() || this.token.equalsIgnoreCase("BOT_TOKEN_HERE")) {
                this.token = this.prompt.prompt("Please provide a bot token.\nInstructions for obtaining a token can be found here:\nhttps://github.com/jagrosh/MusicBot/wiki/Getting-a-Bot-Token.\nBot Token: ");
                if (this.token == null) {
                    this.prompt.alert(Prompt.Level.WARNING, "Config", "No token provided! Exiting.\n\nConfig Location: " + this.path.toAbsolutePath().toString());
                    return;
                }
                write = true;
            }
            if (this.owner <= 0L) {
                try {
                    this.owner = Long.parseLong(this.prompt.prompt("Owner ID was missing, or the provided owner ID is not valid.\nPlease provide the User ID of the bot's owner.\nInstructions for obtaining your User ID can be found here:\nhttps://github.com/jagrosh/MusicBot/wiki/Finding-Your-User-ID\nOwner User ID: "));
                }
                catch (NumberFormatException | NullPointerException ex4) {
                    final RuntimeException ex = ex4;
                    this.owner = 0L;
                }
                if (this.owner <= 0L) {
                    this.prompt.alert(Prompt.Level.ERROR, "Config", "Invalid User ID! Exiting.\n\nConfig Location: " + this.path.toAbsolutePath().toString());
                    return;
                }
                write = true;
            }
            if (write) {
                this.writeToFile();
            }
            this.valid = true;
        }
        catch (ConfigException ex2) {
            this.prompt.alert(Prompt.Level.ERROR, "Config", ex2 + ": " + ex2.getMessage() + "\n\nConfig Location: " + this.path.toAbsolutePath().toString());
        }
    }
    
    private void writeToFile() {
        final String original = OtherUtil.loadResource(this, "/reference.conf");
        byte[] bytes;
        if (original == null) {
            bytes = ("token = " + this.token + "\r\nowner = " + this.owner).getBytes();
        }
        else {
            bytes = original.substring(original.indexOf("/// START OF JMUSICBOT CONFIG ///") + "/// START OF JMUSICBOT CONFIG ///".length(), original.indexOf("/// END OF JMUSICBOT CONFIG ///")).replace("BOT_TOKEN_HERE", this.token).replace("0 // OWNER ID", Long.toString(this.owner)).trim().getBytes();
        }
        try {
            Files.write(this.path, bytes, new OpenOption[0]);
        }
        catch (IOException ex) {
            this.prompt.alert(Prompt.Level.WARNING, "Config", "Failed to write new config options to config.txt: " + ex + "\nPlease make sure that the files are not on your desktop or some other restricted area.\n\nConfig Location: " + this.path.toAbsolutePath().toString());
        }
    }
    
    public boolean isValid() {
        return this.valid;
    }
    
    public String getConfigLocation() {
        return this.path.toFile().getAbsolutePath();
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public String getAltPrefix() {
        return "NONE".equalsIgnoreCase(this.altprefix) ? null : this.altprefix;
    }
    
    public String getToken() {
        return this.token;
    }
    
    public long getOwnerId() {
        return this.owner;
    }
    
    public String getSuccess() {
        return this.successEmoji;
    }
    
    public String getWarning() {
        return this.warningEmoji;
    }
    
    public String getError() {
        return this.errorEmoji;
    }
    
    public String getLoading() {
        return this.loadingEmoji;
    }
    
    public String getSearching() {
        return this.searchingEmoji;
    }
    
    public Activity getGame() {
        return this.game;
    }
    
    public OnlineStatus getStatus() {
        return this.status;
    }
    
    public String getHelp() {
        return this.helpWord;
    }
    
    public boolean getStay() {
        return this.stayInChannel;
    }
    
    public boolean getSongInStatus() {
        return this.songInGame;
    }
    
    public String getPlaylistsFolder() {
        return this.playlistsFolder;
    }
    
    public boolean getDBots() {
        return this.dbots;
    }
    
    public boolean useUpdateAlerts() {
        return this.updatealerts;
    }
    
    public boolean useEval() {
        return this.useEval;
    }
    
    public boolean useNPImages() {
        return this.npImages;
    }
    
    public long getMaxSeconds() {
        return this.maxSeconds;
    }
    
    public String getMaxTime() {
        return FormatUtil.formatTime(this.maxSeconds * 1000L);
    }
    
    public boolean isTooLong(final AudioTrack track) {
        return this.maxSeconds > 0L && Math.round(track.getDuration() / 1000.0) > this.maxSeconds;
    }
    
    public String[] getAliases(final String command) {
        try {
            return this.aliases.getStringList(command).toArray(new String[0]);
        }
        catch (NullPointerException | ConfigException.Missing ex2) {
            final RuntimeException ex = ex2;
            return new String[0];
        }
    }
}
