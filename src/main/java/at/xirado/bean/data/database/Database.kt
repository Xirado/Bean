package at.xirado.bean.data.database;

import at.xirado.bean.Bean;
import at.xirado.bean.Config;
import at.xirado.bean.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.prometheus.PrometheusMetricsTrackerFactory;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;
    private static org.jetbrains.exposed.sql.Database exposed;

    public static void connect(DatabaseConfig dbConfig) {
        Runnable r = () ->
        {
            if (!isConnected()) {
                String host = dbConfig.getHost();
                String database = dbConfig.getDatabase();
                String username = dbConfig.getUsername();
                String password = dbConfig.getPassword();
                int port = dbConfig.getPort();
                config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
                config.setUsername(username);
                config.setPassword(password);
                config.setMaximumPoolSize(10);
                config.setDriverClassName("org.mariadb.jdbc.Driver");
                config.setScheduledExecutor(Bean.getInstance().getScheduledExecutor());
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("characterEncoding", "utf8");
                config.addDataSourceProperty("useUnicode", "true");
                config.setMetricsTrackerFactory(new PrometheusMetricsTrackerFactory());
                ds = new HikariDataSource(config);
                executeQueries();
                exposed = ExposedMiddlemanKt.connectExposed(ds);
                ExposedMiddlemanKt.createMissingTables();
            }
        };
        Bean.getInstance().getExecutor().execute(r);
    }

    public static Connection getConnectionFromPool() {
        try {
            return ds.getConnection();
        } catch (SQLException throwables) {
            LOGGER.error("Could not get Connection from SQL-Pool!", throwables);
            return null;
        }
    }

    public static org.jetbrains.exposed.sql.Database getExposed() {
        return exposed;
    }

    public static void awaitReady() {
        while (!isConnected()) {
            Thread.onSpinWait();
        }
    }

    public static void disconnect() {
        ds.close();
    }

    public static boolean isConnected() {
        return (ds != null);
    }

    private static void executeQueries() {
        String[] commands = new String[]{"CREATE TABLE IF NOT EXISTS modcases (uuid VARCHAR(36) PRIMARY KEY, caseType TINYINT, guild BIGINT, user BIGINT, moderator BIGINT, reason VARCHAR(256), createdAt BIGINT, duration BIGINT)",
                "CREATE TABLE IF NOT EXISTS levels (guildID BIGINT, userID BIGINT, totalXP BIGINT, name VARCHAR(256), discriminator VARCHAR(4), avatar VARCHAR(128), PRIMARY KEY(guildID, userID)) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                "CREATE TABLE IF NOT EXISTS guildsettings (guildID BIGINT PRIMARY KEY, data JSON CHECK (JSON_VALID(data)))",
                "CREATE TABLE IF NOT EXISTS usersettings (userID BIGINT PRIMARY KEY, data JSON CHECK (JSON_VALID(data)))",
                "CREATE TABLE IF NOT EXISTS xpalerts (guildID BIGINT PRIMARY KEY, mode VARCHAR(128))",
                "CREATE TABLE IF NOT EXISTS wildcardsettings (userID BIGINT PRIMARY KEY, card VARCHAR(128) NOT NULL, accent INT)",
                "CREATE TABLE IF NOT EXISTS searchqueries (user_id BIGINT, searched_at BIGINT, name VARCHAR(256), value VARCHAR(256), playlist BOOL) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                "CREATE TABLE IF NOT EXISTS userBalance (guild_id BIGINT, user_id BIGINT, balance BIGINT, PRIMARY KEY(guild_id, user_id))",
                "CREATE TABLE IF NOT EXISTS bookmarks (user_id BIGINT, added_at BIGINT, name VARCHAR(256), value VARCHAR(256), playlist BOOL, PRIMARY KEY(user_id, value)) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                "CREATE TABLE IF NOT EXISTS dismissable_contents (user_id BIGINT, identifier VARCHAR(128), state VARCHAR(128), PRIMARY KEY(user_id, identifier))",
                "CREATE TABLE IF NOT EXISTS banned_guilds (guild_id BIGINT PRIMARY KEY, reason VARCHAR(256))"
        };

        try (Connection connection = Database.getConnectionFromPool()) {
            if (connection == null) return;
            for (String command : commands) {
                PreparedStatement ps = connection.prepareStatement(command);
                ps.execute();
                ps.close();
            }
        } catch (Exception e) {
            LOGGER.error("Could not run command", e);
        }
    }
}
