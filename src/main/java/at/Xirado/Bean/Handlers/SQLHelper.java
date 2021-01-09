package at.Xirado.Bean.Handlers;

import at.Xirado.Bean.Misc.SQL;

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
        executeSQL("CREATE TABLE IF NOT EXISTS modCases (caseID VARCHAR(6) PRIMARY KEY, guildID BIGINT NOT NULL, targetID BIGINT NOT NULL, moderatorID BIGINT NOT NULL, caseType VARCHAR(20) NOT NULL, reason VARCHAR(512) NOT NULL, duration BIGINT NOT NULL, creationDate BIGINT NOT NULL)");

    }

    public static void executeSQL(String query)
    {
        try {
            PreparedStatement ps = SQL.con.prepareStatement(query);
            ps.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
