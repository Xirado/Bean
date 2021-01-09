// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.jagrosh.jmusicbot.commands.admin.SetdjCmd;
import com.jagrosh.jmusicbot.commands.admin.SettcCmd;
import com.jagrosh.jmusicbot.commands.admin.SetvcCmd;
import com.jagrosh.jmusicbot.commands.dj.*;
import com.jagrosh.jmusicbot.commands.general.SettingsCmd;
import com.jagrosh.jmusicbot.commands.music.*;
import com.jagrosh.jmusicbot.commands.owner.*;
import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.gui.GUI;
import com.jagrosh.jmusicbot.settings.SettingsManager;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Arrays;

public class JMusicBot
{
    public static final String PLAY_EMOJI = "\u25b6";
    public static final String PAUSE_EMOJI = "\u23f8";
    public static final String STOP_EMOJI = "\u23f9";
    public static final Permission[] RECOMMENDED_PERMS;
    public static final GatewayIntent[] INTENTS;
    
    public static void main(final String[] args) {
        final Logger log = LoggerFactory.getLogger("Startup");
        final Prompt prompt = new Prompt("JMusicBot", "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag.", "true".equalsIgnoreCase(System.getProperty("nogui", "false")));
        final String version = OtherUtil.checkVersion(prompt);
        if (!System.getProperty("java.vm.name").contains("64")) {
            prompt.alert(Prompt.Level.WARNING, "Java Version", "It appears that you may not be using a supported Java version. Please use 64-bit java.");
        }
        final BotConfig config = new BotConfig(prompt);
        config.load();
        if (!config.isValid()) {
            return;
        }
        final EventWaiter waiter = new EventWaiter();
        final SettingsManager settings = new SettingsManager();
        final Bot bot = new Bot(waiter, config, settings);
        final AboutCommand aboutCommand = new AboutCommand(Color.BLUE.brighter(), "a music bot that is [easy to host yourself!](https://github.com/jagrosh/MusicBot) (v" + version + ")", new String[] { "High-quality music playback", "FairQueue\u2122 Technology", "Easy to host yourself" }, JMusicBot.RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("\ud83c\udfb6");
        final CommandClientBuilder cb = new CommandClientBuilder().setPrefix(config.getPrefix()).setAlternativePrefix(config.getAltPrefix()).setOwnerId(Long.toString(config.getOwnerId())).setEmojis(config.getSuccess(), config.getWarning(), config.getError()).setHelpWord(config.getHelp()).setLinkedCacheSize(200).setGuildSettingsManager(settings).addCommands(aboutCommand, new PingCommand(), new SettingsCmd(bot), new LyricsCmd(bot), new NowplayingCmd(bot), new PlayCmd(bot), new PlaylistsCmd(bot), new QueueCmd(bot), new RemoveCmd(bot), new SearchCmd(bot), new SCSearchCmd(bot), new ShuffleCmd(bot), new SkipCmd(bot), new ForceRemoveCmd(bot), new ForceskipCmd(bot), new MoveTrackCmd(bot), new PauseCmd(bot), new PlaynextCmd(bot), new RepeatCmd(bot), new SkiptoCmd(bot), new StopCmd(bot), new VolumeCmd(bot), new SetdjCmd(bot), new SettcCmd(bot), new SetvcCmd(bot), new AutoplaylistCmd(bot), new DebugCmd(bot), new PlaylistCmd(bot), new SetavatarCmd(bot), new SetgameCmd(bot), new SetnameCmd(bot), new SetstatusCmd(bot), new ShutdownCmd(bot));
        if (config.useEval()) {
            cb.addCommand(new EvalCmd(bot));
        }
        boolean nogame = false;
        if (config.getStatus() != OnlineStatus.UNKNOWN) {
            cb.setStatus(config.getStatus());
        }
        if (config.getGame() == null) {
            cb.useDefaultGame();
        }
        else if (config.getGame().getName().equalsIgnoreCase("none")) {
            cb.setActivity(null);
            nogame = true;
        }
        else {
            cb.setActivity(config.getGame());
        }
        if (!prompt.isNoGUI()) {
            try {
                final GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            }
            catch (Exception e) {
                log.error("Could not start GUI. If you are running on a server or in a location where you cannot display a window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }
        log.info("Loaded config from " + config.getConfigLocation());
        try {
            final JDA jda = JDABuilder.create(config.getToken(), Arrays.asList(JMusicBot.INTENTS))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE)
                    .setActivity(nogame ? null : Activity.playing("loading..."))
                    .setStatus((config.getStatus() == OnlineStatus.INVISIBLE || config.getStatus() == OnlineStatus.OFFLINE) ? OnlineStatus.INVISIBLE : OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(cb.build(), waiter, new Listener(bot)).setBulkDeleteSplittingEnabled(true).build();
            bot.setJDA(jda);
        }
        catch (LoginException ex) {
            prompt.alert(Prompt.Level.ERROR, "JMusicBot", ex + "\nPlease make sure you are editing the correct config.txt file, and that you have used the correct token (not the 'secret'!)\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        }
        catch (IllegalArgumentException ex2) {
            prompt.alert(Prompt.Level.ERROR, "JMusicBot", "Some aspect of the configuration is invalid: " + ex2 + "\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        }
    }
    
    static {
        RECOMMENDED_PERMS = new Permission[] { Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE };
        INTENTS = new GatewayIntent[] { GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES };
    }
}
