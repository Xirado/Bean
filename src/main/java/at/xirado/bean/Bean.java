package at.xirado.bean;

import at.xirado.bean.backend.Authenticator;
import at.xirado.bean.backend.WebServer;
import at.xirado.bean.command.handler.CommandHandler;
import at.xirado.bean.command.handler.InteractionHandler;
import at.xirado.bean.data.OkHttpInterceptor;
import at.xirado.bean.data.content.DismissableContentManager;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.event.*;
import at.xirado.bean.mee6.MEE6Queue;
import at.xirado.bean.misc.Util;
import at.xirado.bean.prometheus.MetricsJob;
import at.xirado.bean.prometheus.Prometheus;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.IOUtil;
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
    public static final Long TEST_SERVER_ID = 815597207617142814L;
    public static final Set<Long> WHITELISTED_USERS = Set.of(184654964122058752L, 398610798315962408L);
    public static final String SUPPORT_GUILD_INVITE = "https://discord.com/invite/7WEjttJtKa";
    public static final long START_TIME = System.currentTimeMillis() / 1000;

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
    private final CommandHandler commandHandler;
    private final EventWaiter eventWaiter;
    private final OkHttpClient okHttpClient;
    private final WebServer webServer;
    private final Authenticator authenticator;
    private final MEE6Queue mee6Queue;
    private final DismissableContentManager dismissableContentManager;

    private WebhookClient webhookClient = null;
    private DataObject config = loadConfig();

    public Bean() throws Exception {
        instance = this;
        Message.suppressContentIntentWarning();
        Database.connect();
        Database.awaitReady();
        debug = !config.isNull("debug") && config.getBoolean("debug");
        interactionHandler = new InteractionHandler(this);
        commandHandler = new CommandHandler();
        eventWaiter = new EventWaiter();
        Class.forName("at.xirado.bean.translation.LocaleLoader");
        okHttpClient = new OkHttpClient.Builder()
                .build();
        if (!config.isNull("webhook_url"))
            webhookClient = new WebhookClientBuilder(config.getString("webhook_url"))
                    .build();
        if (config.isNull("token"))
            throw new IllegalStateException("Can not start without a token!");
        shardManager = DefaultShardManagerBuilder.create(config.getString("token"), getIntents())
                .setShardsTotal(-1)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setActivity(Activity.playing("bean.bz"))
                .enableCache(CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setHttpClientBuilder(IOUtil.newHttpClientBuilder().addInterceptor(new OkHttpInterceptor()))
                .setGatewayEncoding(GatewayEncoding.ETF)
                .setEnableShutdownHook(false)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .addEventListeners(new JDAReadyListener(), new SlashCommandListener(), new MessageCreateListener(),
                        new XPMessageListener(), new MessageReactionAddListener(), new MessageReactionRemoveListener(),
                        eventWaiter, new GuildMemberJoinListener(), new DismissableContentButtonListener(), new GuildJoinListener(),
                        EvalListener.INSTANCE)
                .build();
        authenticator = new Authenticator();
        String host = config.isNull("ip") ? "127.0.0.1" : config.getString("ip");
        int port = config.isNull("port") ? 8887 : config.getInt("port");

        webServer = new WebServer(host, port);
        dismissableContentManager = new DismissableContentManager();
        new Prometheus();
        new MetricsJob().start();
        mee6Queue = new MEE6Queue();
        mee6Queue.start();
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
        try {
            loadPropertiesFile();
            setupShutdownHook();
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
        } catch(Throwable e) {
            LOGGER.error("Could not read app.properties file!");
            VERSION = "0.0.0";
            BUILD_TIME = 0L;
        }
    }

    public WebhookClient getWebhookClient() {
        return webhookClient;
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

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
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

    public WebServer getWebServer() {
        return webServer;
    }

    public MEE6Queue getMEE6Queue() {
        return mee6Queue;
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Awaiting JDA ShardManager shutdown...");
            getInstance().getShardManager().shutdown();
            for (JDA jda : getInstance().getShardManager().getShards()) {
                while (jda.getStatus() != JDA.Status.SHUTDOWN) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {}
                }
            }
            LOGGER.info("Stopped all shards");
            LOGGER.info("Goodbye");
        }));
    }
}
