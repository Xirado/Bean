package at.xirado.bean.data.database;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.Util;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    public static void connect()
    {
        Runnable r = () ->
        {
            if (!isConnected())
            {
                String host = Bean.getInstance().getConfig().getString("database.host");
                String database = Bean.getInstance().getConfig().getString("database.database");
                String username = Bean.getInstance().getConfig().getString("database.username");
                String password = Bean.getInstance().getConfig().getString("database.password");
                int port = Bean.getInstance().getConfig().getInt("database.port");
                config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
                config.setUsername(username);
                config.setPassword(password);
                config.setMaximumPoolSize(10);
                config.setDriverClassName("org.mariadb.jdbc.Driver");
                config.setScheduledExecutor(Bean.getInstance().getExecutor());
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("characterEncoding", "utf8");
                config.addDataSourceProperty("useUnicode", "true");
                ds = new HikariDataSource(config);
                executeQueries();
            }
        };
        Bean.getInstance().getExecutor().submit(r);
    }

    public static Connection getConnectionFromPool()
    {
        try
        {
            return ds.getConnection();
        } catch (SQLException throwables)
        {
            LOGGER.error("Could not get Connection from SQL-Pool!", throwables);
            return null;
        }
    }

    public static void awaitReady()
    {
        while (!isConnected())
        {
            Thread.onSpinWait();
        }
    }

    public static void disconnect()
    {
        ds.close();
    }

    public static boolean isConnected()
    {
        return (ds != null);
    }

    private static void executeQueries()
    {
        String[] commands = new String[]{"CREATE TABLE IF NOT EXISTS modCases (uuid VARCHAR(36) PRIMARY KEY, caseType TINYINT, guild BIGINT, user BIGINT, moderator BIGINT, reason VARCHAR(256), createdAt BIGINT, duration BIGINT, active BOOLEAN)",
                "CREATE TABLE IF NOT EXISTS levels (guildID BIGINT, userID BIGINT, totalXP BIGINT, name VARCHAR(256), discriminator VARCHAR(4), PRIMARY KEY(guildID, userID)) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                "CREATE TABLE IF NOT EXISTS guildSettings (guildID BIGINT PRIMARY KEY, data JSON CHECK (JSON_VALID(data)))",
                "CREATE TABLE IF NOT EXISTS userSettings (userID BIGINT PRIMARY KEY, data JSON CHECK (JSON_VALID(data)))",
                "CREATE TABLE IF NOT EXISTS xpAlerts (guildID BIGINT PRIMARY KEY, mode VARCHAR(128))",
                "CREATE TABLE IF NOT EXISTS wildcardSettings (userID BIGINT PRIMARY KEY, card VARCHAR(128) NOT NULL)"
        };
        Connection connection = Database.getConnectionFromPool();
        if (connection == null) return;
        try
        {
            for (String command : commands)
            {
                PreparedStatement ps = connection.prepareStatement(command);
                ps.execute();
                ps.close();
            }
        } catch (Exception e)
        {
            LOGGER.error("Could not run command", e);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }
}
