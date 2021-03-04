package at.xirado.bean.handlers;

import at.xirado.bean.misc.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLHelper {

    public static void createTables()
    {
        executeSQL("CREATE TABLE IF NOT EXISTS blacklistedWords (guildID BIGINT, word VARCHAR(128))");
        executeSQL("CREATE TABLE IF NOT EXISTS logChannels (guildID BIGINT PRIMARY KEY, channelID BIGINT)");
        executeSQL("CREATE TABLE IF NOT EXISTS reactionRoles (messageID BIGINT, emoticon VARCHAR(500), roleID BIGINT)");
        executeSQL("CREATE TABLE IF NOT EXISTS punishLogs (guildID BIGINT, userID BIGINT, reason VARCHAR(256), time BIGINT, duration BIGINT, type VARCHAR(10))");
        executeSQL("CREATE TABLE IF NOT EXISTS commandPrefixes (guildID BIGINT PRIMARY KEY, prefix VARCHAR(10))");
        executeSQL("CREATE TABLE IF NOT EXISTS modcases (caseID VARCHAR(6) PRIMARY KEY, guildID BIGINT NOT NULL, targetID BIGINT NOT NULL, moderatorID BIGINT NOT NULL, caseType VARCHAR(20) NOT NULL, reason VARCHAR(512) NOT NULL, duration BIGINT NOT NULL, creationDate BIGINT NOT NULL, active BOOLEAN NOT NULL)");

    }

    public static void executeSQL(String query)
    {
        try {
            Connection connection = SQL.getConnectionFromPool();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.execute();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
