package at.xirado.bean;

import at.xirado.bean.backend.Authenticator;
import at.xirado.bean.backend.WebServer;
import at.xirado.bean.command.ConsoleCommandManager;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.handler.CommandHandler;
import at.xirado.bean.command.handler.SlashCommandHandler;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.event.*;
import at.xirado.bean.lavaplayer.SpotifyAudioSource;
import at.xirado.bean.log.Shell;
import at.xirado.bean.misc.Util;
import at.xirado.bean.music.AudioManager;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lavalink.client.LavalinkUtil;
import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bean
{
    public static final long OWNER_ID = 184654964122058752L;
    public static final Set<Long> WHITELISTED_USERS = Set.of(184654964122058752L, 398610798315962408L);
    public static final String SUPPORT_GUILD_INVITE = "https://discord.com/invite/7WEjttJtKa";
    public static final long START_TIME = System.currentTimeMillis() / 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);
    private static String VERSION;
    private static long BUILD_TIME;
    private static Bean instance;
    private final ShardManager shardManager;
    private final boolean debug;
    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(),
                    new ThreadFactoryBuilder()
                            .setNameFormat("Bean Thread %d")
                            .setUncaughtExceptionHandler((t, e) -> LOGGER.error("An uncaught error occurred on the Threadpool! (Thread " + t.getName() + ")", e))
                            .build());
    private final ConsoleCommandManager consoleCommandManager;
    private final SlashCommandHandler slashCommandHandler;
    private final CommandHandler commandHandler;
    private final AudioManager audioManager;
    private final EventWaiter eventWaiter;
    private final OkHttpClient okHttpClient;
    private final WebServer webServer;
    private final Authenticator authenticator;
    private final JdaLavalink lavalink;

    private WebhookClient webhookClient = null;
    private DataObject config = loadConfig();

    public Bean() throws Exception
    {
        instance = this;
        LavalinkUtil.getPlayerManager().registerSourceManager(new SpotifyAudioSource());
        Database.connect();
        Database.awaitReady();
        consoleCommandManager = new ConsoleCommandManager();
        consoleCommandManager.registerAllCommands();
        debug = !config.isNull("debug") && config.getBoolean("debug");
        slashCommandHandler = new SlashCommandHandler();
        commandHandler = new CommandHandler();
        eventWaiter = new EventWaiter();
        Class.forName("at.xirado.bean.translation.LocaleLoader");
        okHttpClient = new OkHttpClient.Builder()
                .build();
        if (!config.isNull("webhook_url"))
            webhookClient = new WebhookClientBuilder(config.getString("webhook_url"))
                    .build();
        lavalink = new JdaLavalink(
                null,
                1,
                null
        );
        if (config.isNull("token"))
            throw new IllegalStateException("Can not start without a token!");
        shardManager = DefaultShardManagerBuilder.create(config.getString("token"), getIntents())
                .setShardsTotal(-1)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .enableCache(CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setGatewayEncoding(GatewayEncoding.ETF)
                .setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor())
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .addEventListeners(new JDAReadyListener(), new SlashCommandListener(), new MessageCreateListener(),
                        new XPMessageListener(), new MessageReactionAddListener(), new MessageReactionRemoveListener(), new VoiceUpdateListener(),
                        eventWaiter, new GuildMemberJoinListener(), lavalink, new HintAcknowledgeListener(), new GuildJoinListener())
                .build();
        audioManager = new AudioManager();
        authenticator = new Authenticator();
        webServer = new WebServer(8887);
    }

    public static Bean getInstance()
    {
        return instance;
    }

    public static EnumSet<GatewayIntent> getIntents()
    {
        return EnumSet.of(
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES
        );
    }

    public static void main(String[] args)
    {
        Thread.currentThread().setName("Main");
        try
        {
            loadPropertiesFile();
            Shell.startShell();
            Shell.awaitReady();
            new Bean();
        } catch (Exception e)
        {
            if (e instanceof LoginException ex)
            {
                LOGGER.error("Could not login to Discord!", ex);
                return;
            }
            LOGGER.error("An error occurred while starting the bot!", e);
        }
    }

    private static void loadPropertiesFile()
    {
        try
        {
            Properties properties = new Properties();
            properties.load(Bean.class.getClassLoader().getResourceAsStream("app.properties"));
            VERSION = properties.getProperty("app-version");
            BUILD_TIME = Long.parseLong(properties.getProperty("build-time"));
        } catch (Exception e)
        {
            LOGGER.error("An error occurred while reading app.properties file!", e);
            VERSION = "0.0.0";
            BUILD_TIME = 0L;
        }
    }

    public WebhookClient getWebhookClient()
    {
        return webhookClient;
    }

    public static void info(String msg)
    {
        LOGGER.info(msg);
    }

    public static void warn(String msg)
    {
        LOGGER.warn(msg);
    }

    public static void error(String msg)
    {
        LOGGER.error(msg);
    }

    public static void error(String msg, Throwable t)
    {
        LOGGER.error(msg, t);
    }

    public static String getBeanVersion()
    {
        return VERSION;
    }

    public static long getBuildTime()
    {
        return BUILD_TIME;
    }

    public static boolean isWhitelistedUser(long userId)
    {
        return WHITELISTED_USERS.contains(userId);
    }

    public synchronized DataObject getConfig()
    {
        return config;
    }

    public synchronized void updateConfig()
    {
        this.config = loadConfig();
    }

    public ScheduledExecutorService getExecutor()
    {
        return scheduledExecutorService;
    }

    public boolean isDebug()
    {
        return debug;
    }

    public ShardManager getShardManager()
    {
        return shardManager;
    }

    public ConsoleCommandManager getConsoleCommandManager()
    {
        return consoleCommandManager;
    }

    public SlashCommandHandler getSlashCommandHandler()
    {
        return slashCommandHandler;
    }

    public CommandHandler getCommandHandler()
    {
        return commandHandler;
    }

    public AudioManager getAudioManager()
    {
        return audioManager;
    }

    public EventWaiter getEventWaiter()
    {
        return eventWaiter;
    }

    public OkHttpClient getOkHttpClient()
    {
        return okHttpClient;
    }

    public Authenticator getAuthenticator()
    {
        return authenticator;
    }

    private DataObject loadConfig()
    {
        File configFile = new File("config.json");
        if (!configFile.exists())
        {
            InputStream inputStream = Bean.class.getResourceAsStream("/config.json");
            if (inputStream == null)
            {
                LOGGER.error("Could not copy config from resources folder!");
                return DataObject.empty();
            }
            Path path = Paths.get(Util.getJarPath() + "/config.json");
            try
            {
                Files.copy(inputStream, path);
            } catch (IOException e)
            {
                LOGGER.error("Could not copy config file!", e);
            }
        }
        try
        {
            return DataObject.fromJson(new FileInputStream(configFile));
        } catch (FileNotFoundException ignored)
        {
        }
        return DataObject.empty();
    }

    public WebServer getWebServer()
    {
        return webServer;
    }

    public JdaLavalink getLavalink()
    {
        return lavalink;
    }

    public void initCommandCheck()
    {
        LOGGER.info("Checking for outdated command cache...");
        getExecutor().submit(() -> {
            if (Bean.getInstance().isDebug())
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(815597207617142814L);
                if (guild == null)
                {
                    LOGGER.error("Debug guild does not exist!");
                    return;
                }
                guild.retrieveCommands().queue(list -> {
                    List<SlashCommand> commandList = Bean.getInstance().getSlashCommandHandler().getRegisteredGuildCommands().get(guild.getIdLong());
                    boolean commandRemovedOrAdded = commandList.size() != list.size();
                    if (commandRemovedOrAdded)
                    {
                        if (commandList.size() > list.size())
                            LOGGER.warn("New command(s) has/have been added! Updating Discords cache...");
                        else
                            LOGGER.warn("Command(s) has/have been removed! Updating Discords cache...");
                        Bean.getInstance().getSlashCommandHandler().updateCommands(s -> LOGGER.info("Updated {} commands!", s.size()), e -> {
                        });
                        return;
                    }
                    boolean outdated = false;
                    for (SlashCommand slashCommand : commandList)
                    {
                        Command discordCommand = list.stream()
                                .filter(x -> x.getName().equalsIgnoreCase(slashCommand.getCommandData().getName()))
                                .findFirst().orElse(null);
                        // Discord doesn't have this command yet!
                        if (discordCommand == null)
                        {
                            outdated = true;
                            break;
                        }
                        // Option size doesn't match!
                        if (discordCommand.getOptions().size() != slashCommand.getCommandData().getOptions().size())
                        {
                            outdated = true;
                            break;
                        }
                        String[] optionNames = slashCommand.getCommandData().getOptions().stream().map(OptionData::getName).toArray(String[]::new);
                        String[] discordOptionNames = discordCommand.getOptions().stream().map(Command.Option::getName).toArray(String[]::new);
                        // Option names don't match!
                        if (!Arrays.equals(optionNames, discordOptionNames))
                        {
                            outdated = true;
                            break;
                        }
                        String[] optionDescriptions = slashCommand.getCommandData().getOptions().stream().map(OptionData::getDescription).toArray(String[]::new);
                        String[] discordOptionDescriptions = discordCommand.getOptions().stream().map(Command.Option::getDescription).toArray(String[]::new);
                        // Option descriptions don't match!
                        if (!Arrays.equals(optionDescriptions, discordOptionDescriptions))
                        {
                            outdated = true;
                            break;
                        }
                    }
                    if (outdated)
                        Bean.getInstance().getSlashCommandHandler().updateCommands(s -> LOGGER.info("Updated {} commands!", s.size()), e -> {
                        });
                });
            }
            Bean.getInstance().getShardManager().getShards().get(0).retrieveCommands().queue((list) -> {
                List<SlashCommand> commandList = Bean.getInstance().getSlashCommandHandler().getRegisteredCommands()
                        .stream()
                        .filter(SlashCommand::isGlobal)
                        .toList();
                boolean commandRemovedOrAdded = commandList.size() != list.size();
                if (commandRemovedOrAdded)
                {
                    if (commandList.size() > list.size())
                        LOGGER.warn("New command(s) has/have been added! Updating Discords cache...");
                    else
                        LOGGER.warn("Command(s) has/have been removed! Updating Discords cache...");
                    Bean.getInstance().getSlashCommandHandler().updateCommands(s -> LOGGER.info("Updated {} commands!", s.size()), e -> {
                    });
                    return;
                }
                boolean outdated = false;
                for (SlashCommand slashCommand : commandList)
                {
                    Command discordCommand = list.stream()
                            .filter(x -> x.getName().equalsIgnoreCase(slashCommand.getCommandData().getName()))
                            .findFirst().orElse(null);
                    // Discord doesn't have this command yet!
                    if (discordCommand == null)
                    {
                        outdated = true;
                        break;
                    }
                    // Option size doesn't match!
                    if (discordCommand.getOptions().size() != slashCommand.getCommandData().getOptions().size())
                    {
                        outdated = true;
                        break;
                    }
                    String[] optionNames = slashCommand.getCommandData().getOptions().stream().map(OptionData::getName).toArray(String[]::new);
                    String[] discordOptionNames = discordCommand.getOptions().stream().map(Command.Option::getName).toArray(String[]::new);
                    // Option names don't match!
                    if (!Arrays.equals(optionNames, discordOptionNames))
                    {
                        outdated = true;
                        break;
                    }
                    String[] optionDescriptions = slashCommand.getCommandData().getOptions().stream().map(OptionData::getDescription).toArray(String[]::new);
                    String[] discordOptionDescriptions = discordCommand.getOptions().stream().map(Command.Option::getDescription).toArray(String[]::new);
                    // Option descriptions don't match!
                    if (!Arrays.equals(optionDescriptions, discordOptionDescriptions))
                    {
                        outdated = true;
                        break;
                    }
                }
                if (outdated)
                    Bean.getInstance().getSlashCommandHandler().updateCommands(s -> LOGGER.info("Updated {} commands!", s.size()), e -> {
                    });
            });
        });
    }
}