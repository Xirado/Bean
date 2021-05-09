package at.xirado.bean;

import at.xirado.bean.commandutil.ConsoleCommandManager;
import at.xirado.bean.handlers.*;
import at.xirado.bean.logging.Shell;
import at.xirado.bean.misc.JSON;
import at.xirado.bean.misc.SQL;
import at.xirado.bean.misc.Util;
import at.xirado.bean.punishmentmanager.Case;
import at.xirado.bean.punishmentmanager.CaseType;
import at.xirado.bean.punishmentmanager.Punishments;
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

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
public class Bean
{
    public static final long OWNER_ID = 184654964122058752L;
    public static final long STARTTIME = System.currentTimeMillis()/1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(Bean.class);

    public static Bean instance;

    public static Bean getInstance()
    {
        return instance;
    }

    public final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Threadpool (Thread %d)").build();
    public final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), namedThreadFactory);

    public final PrefixManager prefixManager = new PrefixManager();
    public final LogChannelManager logChannelManager = new LogChannelManager();
    public final BlacklistManager blacklistManager = new BlacklistManager();
    public final ReactionRoleManager reactionRoleManager = new ReactionRoleManager();
    public PermissionCheckerManager permissionCheckerManager;
    public MutedRoleManager mutedRoleManager;
    public SlashCommandHandler slashCommandHandler;

    public Bot musicinstance;
    private String token;
    public final String path;
    public JDA jda;
    public CommandHandler commandHandler;
    public ConsoleCommandManager consoleCommandManager;
    public static boolean debugMode;
    public JSON config;

    public final String VERSION;


    public Bean() throws Exception
    {
        instance = this;
        Thread.currentThread().setName("Main-Thread");
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("settings.properties"));
        VERSION = properties.getProperty("app-version");
        this.path = Util.getPath();
        Shell.startShell(() -> {});
        File file = new File("config.json");
        if(!file.exists())
        {
            InputStream inputStream = Bean.class.getResourceAsStream("config.json");
            if(inputStream != null)
            {
                Path path = Paths.get(this.path);
                try
                {
                    Files.copy(inputStream, path);

                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
        config = JSON.parse(file);
        if(config == null){
            System.out.println("Config file not existing. Aborting...");
            System.exit(0);
        }
        token = config.getString("token");
        debugMode = config.getBoolean("debug");
        SQL.connect(() -> { permissionCheckerManager = new PermissionCheckerManager();});
        try
        {
            jda = JDABuilder.create(token, EnumSet.allOf(GatewayIntent.class))
                    .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                    .addEventListeners(Util.getListeners())
                    .build();
        } catch (LoginException e)
        {
            LOGGER.error("Could not login to Discord!", e);
        }

        try
        {
            this.jda.awaitReady();
        } catch (InterruptedException e)
        {
            LOGGER.error("Could not login to Discord!", e);
        }
        LOGGER.info("Logged in as @"+this.jda.getSelfUser().getAsTag());
        addShutdownHook();
        this.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("bean.bz | +help"), false);

        commandHandler = new CommandHandler();
        slashCommandHandler = new SlashCommandHandler();
        slashCommandHandler.registerAllCommands();
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
                for(Case modCase : cases)
                {
                    if (modCase.getType() == CaseType.TEMPBAN)
                    {
                        if(modCase.getCreatedAt()+modCase.getDuration() < System.currentTimeMillis())
                        {
                            try
                            {
                                Guild g = instance.jda.getGuildById(modCase.getGuildID());
                                if(g == null)
                                {
                                    modCase.setActive(false);
                                    continue;
                                }
                                Punishments.unban(modCase, null);
                                continue;
                            }catch (Exception e)
                            {
                                LOGGER.error("Could not undo punishment", e);
                                continue;
                            }
                        }
                        Runnable r = () ->
                        {
                            Punishments.unban(modCase, null);
                        };
                        scheduledExecutorService.schedule(r, (modCase.getDuration()+modCase.getCreatedAt())-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    }else if(modCase.getType() == CaseType.MUTE)
                    {
                        if(modCase.getCreatedAt()+modCase.getDuration() < System.currentTimeMillis())
                        {
                            try
                            {
                                Guild g = instance.jda.getGuildById(modCase.getGuildID());
                                if(g == null)
                                {
                                    modCase.setActive(false);
                                    continue;
                                }

                                Punishments.unmute(modCase, null);
                                continue;
                            }catch (Exception e)
                            {
                                LOGGER.error("Could not undo punishment", e);
                                continue;
                            }
                        }
                        Runnable r = () ->
                        {
                            Punishments.unmute(modCase, null);
                        };
                        scheduledExecutorService.schedule(r, (modCase.getDuration()+modCase.getCreatedAt())-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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
        new Bean();

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
