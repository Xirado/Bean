package at.xirado.bean.main;

import at.xirado.bean.commandmanager.CommandManager;
import at.xirado.bean.commandmanager.ConsoleCommandManager;
import at.xirado.bean.commandmanager.SlashCommandManager;
import at.xirado.bean.logging.Shell;
import at.xirado.bean.misc.JSON;
import at.xirado.bean.misc.SQL;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import at.xirado.bean.punishmentmanager.Punishments;
import at.xirado.bean.handlers.*;
import at.xirado.bean.translation.I18n;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.BotConfig;
import com.jagrosh.jmusicbot.Listener;
import com.jagrosh.jmusicbot.settings.SettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
public class DiscordBot
{
    public static final long OWNER_ID = 184654964122058752L;
    public static final long STARTTIME = System.currentTimeMillis()/1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);

    public static DiscordBot instance;

    public static DiscordBot getInstance()
    {
        return instance;
    }

    public final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Threadpool (Thread %d)").build();
    public final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), namedThreadFactory);

    public final PrefixManager prefixManager = new PrefixManager();
    public final LogChannelManager logChannelManager = new LogChannelManager();
    public final BlacklistManager blacklistManager = new BlacklistManager();
    public final ReactionRoleManager reactionRoleManager = new ReactionRoleManager();
    public final PermissionCheckerManager permissionCheckerManager;
    public final MutedRoleManager mutedRoleManager;
    public final SlashCommandManager slashCommandManager;

    public Bot musicinstance;
    private String token;
    public final String path;
    public JDA jda;
    public final CommandManager commandManager;
    public final ConsoleCommandManager consoleCommandManager;
    public static boolean debugMode;
    public final JSON config;

    public final String VERSION;


    public DiscordBot() throws Exception
    {
        instance = this;
        Thread.currentThread().setName("Main-Thread");
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("settings.properties"));
        VERSION = properties.getProperty("app-version");
        Shell.startShell();
        while(!Shell.startedSuccessfully)
        {
            Thread.sleep(10);
        }
        I18n.init();
        this.path = Util.getPath();
        File file = new File("config.json");
        if(!file.exists())
        {
            InputStream inputStream = DiscordBot.class.getResourceAsStream("config.json");
            if(inputStream != null)
            {
                Path path = Paths.get(this.path);
                Files.copy(inputStream, path);
            }

        }
        config = JSON.parse(new File("config.json"));
        if(config == null) System.exit(0);
        token = config.getString("token");

        debugMode = config.getBoolean("debug");
        SQL.connect();
        SQL.initKeepAlive();
        if(!SQL.isConnected())
        {
            LOGGER.error("I can't run without a SQL connection!");
            System.exit(1);
        }
        SQLHelper.createTables();
        permissionCheckerManager = new PermissionCheckerManager();
        jda = JDABuilder.create(token, EnumSet.allOf(GatewayIntent.class)).setMemberCachePolicy(MemberCachePolicy.ONLINE).build();

        addShutdownHook();
        this.jda.awaitReady();
        LOGGER.info("Successfully logged in as @"+this.jda.getSelfUser().getAsTag());

        this.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("www.bean.bz | +help"), false);
        Util.addListeners();

        commandManager = new CommandManager();
        commandManager.registerAllCommands();
        slashCommandManager = new SlashCommandManager();
        slashCommandManager.registerAllCommands();
        consoleCommandManager = new ConsoleCommandManager();
        consoleCommandManager.registerAllCommands();
        mutedRoleManager = new MutedRoleManager();
        BotConfig config = new BotConfig(null);
        config.load();
        if(!config.isValid())
            return;

        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        this.musicinstance = new Bot(waiter, config, settings);

        this.musicinstance.setJDA(instance.jda);
        this.jda.addEventListener(waiter);
        this.jda.addEventListener(new Listener(this.musicinstance));
        scheduledExecutorService.submit(() ->
        {
            String qry = "SELECT * FROM modCases WHERE active = 1 AND duration > 0";
            Connection connection = SQL.getConnectionFromPool();
            if(connection == null)
            {
                LOGGER.error("Could not load pending punishments!", new Exception());
                return;
            }
            try(PreparedStatement ps = connection.prepareStatement(qry))
            {
                ResultSet rs = ps.executeQuery();
                List<Case> cases = new ArrayList<>();
                while(rs.next())
                {
                    cases.add(new Case(CaseType.valueOf(rs.getString("caseType").toUpperCase()), rs.getLong("guildID"), rs.getLong("targetID"), rs.getLong("moderatorID"), rs.getString("reason"), rs.getLong("duration"), rs.getLong("creationDate"), rs.getString("caseID"), rs.getBoolean("active")));

                }
                connection.close();
                for(Case modcase : cases)
                {
                    if (modcase.getType() == CaseType.TEMPBAN)
                    {
                        if(modcase.getCreatedAt()+modcase.getDuration() < System.currentTimeMillis())
                        {
                            try
                            {
                                Guild g = instance.jda.getGuildById(modcase.getGuildID());
                                if(g == null)
                                {
                                    modcase.setActive(false);
                                    continue;
                                }
                                Punishments.unban(modcase, null);
                                continue;
                            }catch (Exception e)
                            {
                                LOGGER.error("Could not undo punishment", e);
                                continue;
                            }
                        }
                        Runnable r = () ->
                        {
                            Punishments.unban(modcase, null);
                        };
                        scheduledExecutorService.schedule(r, (modcase.getDuration()+modcase.getCreatedAt())-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    }else if(modcase.getType() == CaseType.MUTE)
                    {
                        if(modcase.getCreatedAt()+modcase.getDuration() < System.currentTimeMillis())
                        {
                            try
                            {
                                Guild g = instance.jda.getGuildById(modcase.getGuildID());
                                if(g == null)
                                {
                                    modcase.setActive(false);
                                    continue;
                                }

                                Punishments.unmute(modcase, null);
                                continue;
                            }catch (Exception e)
                            {
                                LOGGER.error("Could not undo punishment", e);
                                continue;
                            }
                        }
                        Runnable r = () ->
                        {
                            Punishments.unmute(modcase, null);
                        };
                        scheduledExecutorService.schedule(r, (modcase.getDuration()+modcase.getCreatedAt())-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    }
                }
            } catch (SQLException throwables)
            {
                LOGGER.error("An error occured", throwables);
            }finally
            {
                Util.closeQuietly(connection);
            }
        });
        LOGGER.info("Successfully started up!");
        if(debugMode)
        {
            LOGGER.warn("Debug mode enabled! Not listening for any user-commands.");
        }
    }

    public static void main(String[] args) throws Exception {
        new DiscordBot();

    }

    public static void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            LOGGER.info("Shutting down...");
            getInstance().scheduledExecutorService.shutdown();
            getInstance().jda.shutdown();
        }));
    }
}
