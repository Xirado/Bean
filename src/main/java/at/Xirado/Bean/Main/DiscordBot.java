package at.Xirado.Bean.Main;

import at.Xirado.Bean.CommandManager.CommandManager;
import at.Xirado.Bean.Handlers.*;
import at.Xirado.Bean.Misc.JSONConfig;
import at.Xirado.Bean.Misc.SQL;
import at.Xirado.Bean.Misc.Util;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.BotConfig;
import com.jagrosh.jmusicbot.Listener;
import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.gui.GUI;
import com.jagrosh.jmusicbot.settings.SettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DiscordBot
{
    public static final long OWNER_ID = 184654964122058752L;
    public static final long STARTTIME = System.currentTimeMillis()/1000;

    public static DiscordBot instance;

    public static DiscordBot getInstance()
    {
        return instance;
    }

    public final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(6);
    public final ExecutorService executorService = Executors.newFixedThreadPool(6);

    public final PrefixManager prefixManager = new PrefixManager();
    public final LogChannelManager logChannelManager = new LogChannelManager();
    public final BlacklistManager blacklistManager = new BlacklistManager();
    public final ReactionRoleManager reactionRoleManager = new ReactionRoleManager();

    public Bot musicinstance;
    protected final String token;
    public final String path;
    public final JDA jda;
    public final CommandManager commandManager;
    public static boolean debugMode;

    public DiscordBot() throws Exception
    {
        instance = this;
        File file = new File("token.txt");
        if(!file.exists())
        {
            file.createNewFile();
            System.out.println("token.txt has been created. Please add your Discord Bot Token to it, save and start again!");
            System.exit(1);
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        this.token = br.readLine();
        br.close();
        this.path = Util.getPath();
        Util.loadFile("config.json");
        Util.loadFile("log4j.properties");
        JSONConfig.updateConfig();
        SQL.connect();
        SQL.initKeepAlive();
        if(!SQL.isConnected())
        {
            System.out.println("Der Bot funktioniert ohne SQL nicht!");
            System.exit(0);
        }
        SQLHelper.createTables();
        this.jda = JDABuilder.create(token, EnumSet.allOf(GatewayIntent.class)).build();
        PropertyConfigurator.configure("log4j.properties");
        this.jda.awaitReady();
        System.out.println("Successfully logged in as @"+this.jda.getSelfUser().getAsTag());
        this.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("bean.bz | +help"), false);
        Util.addListeners();

        addShutdownHook();

        // startup log
        Logger log = LoggerFactory.getLogger("Startup");

        // create prompt to handle startup
        Prompt prompt = new Prompt("JMusicBot", "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag.",
                "true".equalsIgnoreCase(System.getProperty("nogui", "false")));

        commandManager = new CommandManager();
        commandManager.registerAllCommands();
        // get and check latest version


        // check for valid java version

        // load config
        BotConfig config = new BotConfig(prompt);
        config.load();
        if(!config.isValid())
            return;

        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        this.musicinstance = new Bot(waiter, config, settings);


        if(!prompt.isNoGUI())
        {
            try
            {
                GUI gui = new GUI(this.musicinstance);
                this.musicinstance.setGUI(gui);
                gui.init();
            }
            catch(Exception e)
            {
                log.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }

        log.info("Loaded config from " + config.getConfigLocation());
        this.musicinstance.setJDA(instance.jda);
        this.jda.addEventListener(waiter);
        this.jda.addEventListener(new Listener(this.musicinstance));
        System.out.println("Successfully started up!");
    }

    public static void main(String[] args) throws Exception {
        new DiscordBot();
        debugMode = runningFromIntelliJ();
        if(debugMode)
        {
            System.out.println("Running in IntelliJ! No other users than "+getInstance().jda.getUserById(OWNER_ID).getAsTag()+" can use commands!");
            getInstance().jda.getPresence().setPresence(OnlineStatus.INVISIBLE,false);
        }

    }

    public static boolean runningFromIntelliJ()
    {
        return Boolean.parseBoolean(JSONConfig.config.get("Debug"));
    }

    public static void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Shutting down...");
            getInstance().executorService.shutdown();
            getInstance().scheduledExecutorService.shutdown();
            getInstance().jda.shutdown();
            System.out.println("Shutting down complete!");
        }));
    }
}
