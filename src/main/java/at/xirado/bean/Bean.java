package at.xirado.bean;

import at.xirado.bean.command.handler.InteractionHandler;
import at.xirado.bean.data.content.DismissableContentManager;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.event.*;
import at.xirado.bean.misc.Util;
import at.xirado.bean.music.AudioManager;
import ch.qos.logback.classic.Level;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bean {
    public static final long OWNER_ID = 184654964122058752L;
    public static final Set<Long> WHITELISTED_USERS = Set.of(184654964122058752L, 398610798315962408L);
    public static final String SUPPORT_GUILD_INVITE = "https://discord.com/invite/7WEjttJtKa";

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);
    private static String VERSION;
    private static long BUILD_TIME;
    private static Bean instance;
    private final ShardManager shardManager;
    private final boolean debug;

    private final ExecutorService commandExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                    new ThreadFactoryBuilder()
                            .setNameFormat("Bean Command Thread %d")
                            .setUncaughtExceptionHandler((t, e) -> LOGGER.error("An uncaught error occurred on the command thread-pool! (Thread {})", t.getName(), e))
                            .build());

    private final ScheduledExecutorService scheduledExecutor =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("Bean Scheduled Executor Thread")
                    .setUncaughtExceptionHandler((t, e) -> LOGGER.error("An uncaught error occurred on the scheduled executor!", e))
                    .build());

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("Bean Executor Thread")
                    .setUncaughtExceptionHandler((t, e) -> LOGGER.error("An uncaught error occurred on the executor!", e))
                    .build());

    private final InteractionHandler interactionHandler;
    private final AudioManager audioManager;
    private final EventWaiter eventWaiter;
    private final OkHttpClient okHttpClient;
    private final DismissableContentManager dismissableContentManager;

    private DataObject config = loadConfig();

    public Bean() throws Exception {
        instance = this;
        Database.connect();
        Database.awaitReady();
        debug = !config.isNull("debug") && config.getBoolean("debug");
        interactionHandler = new InteractionHandler(this);
        eventWaiter = new EventWaiter();
        okHttpClient = new OkHttpClient.Builder()
                .build();

        if (config.isNull("token"))
            throw new IllegalStateException("Can not start without a token!");
        shardManager = DefaultShardManagerBuilder.create(config.getString("token"), getIntents())
                .setShardsTotal(-1)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setActivity(Activity.listening("music"))
                .enableCache(CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setGatewayEncoding(GatewayEncoding.ETF)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .addEventListeners(new JDAReadyListener(), new SlashCommandListener(), new VoiceUpdateListener(),
                        eventWaiter, new DismissableContentButtonListener(),
                        new MusicPlayerButtonListener(), new MessageDeleteListener())
                .build();
        audioManager = new AudioManager();
        dismissableContentManager = new DismissableContentManager();
    }

    public static Bean getInstance() {
        return instance;
    }

    public static EnumSet<GatewayIntent> getIntents() {
        return EnumSet.of(
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES
        );
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        String debugProperty = System.getenv("bean_debug");

        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.INFO);
        if (debugProperty != null) {
            String[] packages = debugProperty.split("\\s+");

            for (String pkg : packages) {
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(pkg)).setLevel(Level.DEBUG);
                LOGGER.info("Set logger \"{}\"'s loglevel to DEBUG", pkg);
            }
        }

        try {
            loadPropertiesFile();
            new Bean();
        } catch (Exception e) {
            if (e instanceof LoginException ex) {
                LOGGER.error("Could not login to Discord!", ex);
                return;
            }
            LOGGER.error("An error occurred while starting the bot!", e);
        }
    }

    private static void loadPropertiesFile() {
        try {
            Properties properties = new Properties();
            properties.load(Bean.class.getClassLoader().getResourceAsStream("app.properties"));
            VERSION = properties.getProperty("app-version");
            BUILD_TIME = Long.parseLong(properties.getProperty("build-time"));
        } catch (Exception e) {
            LOGGER.error("An error occurred while reading app.properties file!");
            VERSION = "0.0.0";
            BUILD_TIME = 0L;
        }
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }

    public static void error(String msg, Throwable t) {
        LOGGER.error(msg, t);
    }

    public static String getBeanVersion() {
        return VERSION;
    }

    public static long getBuildTime() {
        return BUILD_TIME;
    }

    public static boolean isWhitelistedUser(long userId) {
        return WHITELISTED_USERS.contains(userId);
    }

    public synchronized DataObject getConfig() {
        return config;
    }

    public synchronized void updateConfig() {
        this.config = loadConfig();
    }

    public ExecutorService getCommandExecutor() {
        return commandExecutor;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public boolean isDebug() {
        return debug;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public InteractionHandler getInteractionHandler() {
        return interactionHandler;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public DismissableContentManager getDismissableContentManager() {
        return dismissableContentManager;
    }

    private DataObject loadConfig() {
        File configFile = new File("config.json");
        if (!configFile.exists()) {
            InputStream inputStream = Bean.class.getResourceAsStream("/config.json");
            if (inputStream == null) {
                LOGGER.error("Could not copy config from resources folder!");
                return DataObject.empty();
            }
            Path path = Paths.get(Util.getJarPath() + "/config.json");
            try {
                Files.copy(inputStream, path);
            } catch (IOException e) {
                LOGGER.error("Could not copy config file!", e);
            }
        }
        try {
            return DataObject.fromJson(new FileInputStream(configFile));
        } catch (FileNotFoundException ignored) {
        }
        return DataObject.empty();
    }
}
