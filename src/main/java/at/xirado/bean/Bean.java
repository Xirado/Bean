package at.xirado.bean;

import at.xirado.bean.command.handler.CommandHandler;
import at.xirado.bean.command.handler.InteractionHandler;
import at.xirado.bean.data.OkHttpInterceptor;
import at.xirado.bean.data.content.DismissableContentManager;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.data.repository.Repository;
import at.xirado.bean.event.*;
import at.xirado.bean.http.HttpServer;
import at.xirado.bean.http.oauth.DiscordAPI;
import at.xirado.bean.log.WebhookAppenderKt;
import at.xirado.bean.mee6.MEE6Queue;
import at.xirado.bean.prometheus.MetricsJob;
import at.xirado.bean.prometheus.Prometheus;
import club.minnced.discord.webhook.WebhookClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime;
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
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
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
    private static final String WEBHOOK_APPENDER_LAYOUT = "%boldGreen(%-15.-15logger{0}) %highlight(%-4level) %msg%n";
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
                    .setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in executor", e))
                    .build());

    private final ExecutorService virtualThreadExecutor =
            Executors.newThreadPerTaskExecutor(new ThreadFactoryBuilder()
                    .setThreadFactory(Thread.ofVirtual().factory())
                    .setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in executor", e))
                    .build());

    private final InteractionHandler interactionHandler;
    private final CommandHandler commandHandler;
    private final EventWaiter eventWaiter;
    private final OkHttpClient okHttpClient;

    private final HttpServer httpServer;

    private final MEE6Queue mee6Queue;
    private final DismissableContentManager dismissableContentManager;
    private final Database database;
    private final Repository repository;

    private WebhookClient webhookClient = null;
    private final Config config = ConfigKt.readConfig(true);

    private final DiscordAPI discordApi;

    public Bean() throws Exception {
        instance = this;
        Message.suppressContentIntentWarning();

        String webhookUrl = config.getWebhookUrl();

        if (webhookUrl != null) {
            WebhookAppenderKt.initWebhookLogger("info", webhookUrl, WEBHOOK_APPENDER_LAYOUT, 5000);
        }

        database = new Database(config.getDb());
        repository = new Repository(database);

        debug = config.getDebugMode();
        interactionHandler = new InteractionHandler(this);
        commandHandler = new CommandHandler();
        eventWaiter = new EventWaiter();
        Class.forName("at.xirado.bean.translation.LocaleLoader");

        okHttpClient = new OkHttpClient.Builder()
                .build();

        setupShutdownHook();
        shardManager = DefaultShardManagerBuilder.create(config.getDiscordToken(), getIntents())
                .setShardsTotal(-1)
                .setMemberCachePolicy(MemberCachePolicy.lru(500))
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

        OAuthConfig oauthConfig = config.getOauth();

        discordApi = new DiscordAPI(oauthConfig);
        httpServer = new HttpServer(config);

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
        DecoroutinatorRuntime.INSTANCE.load();
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
        } catch (Throwable e) {
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

    public synchronized Config getConfig() {
        return config;
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

    public ExecutorService getVirtualThreadExecutor() {
        return virtualThreadExecutor;
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

    public DismissableContentManager getDismissableContentManager() {
        return dismissableContentManager;
    }

    public DiscordAPI getDiscordApi() {
        return discordApi;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    public MEE6Queue getMEE6Queue() {
        return mee6Queue;
    }

    public Database getDatabase() {
        return database;
    }

    public Repository getRepository() {
        return repository;
    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Awaiting JDA ShardManager shutdown...");
            getShardManager().shutdown();
            for (JDA jda : getShardManager().getShards()) {
                while (jda.getStatus() != JDA.Status.SHUTDOWN) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            LOGGER.info("Stopped all shards");
            LOGGER.info("Goodbye");
        }));
    }
}
