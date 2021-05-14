package at.xirado.bean.handlers;

import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SQLHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLHelper.class);

    public static void createTables()
    {
        String[] commands = new String[]{"CREATE TABLE IF NOT EXISTS blacklistedWords (guildID BIGINT, word VARCHAR(128))",
                                "CREATE TABLE IF NOT EXISTS logChannels (guildID BIGINT PRIMARY KEY, channelID BIGINT)",
                                "CREATE TABLE IF NOT EXISTS reactionRoles (messageID BIGINT, emoticon VARCHAR(500), roleID BIGINT)",
                                "CREATE TABLE IF NOT EXISTS commandPrefixes (guildID BIGINT PRIMARY KEY, prefix VARCHAR(10))",
                                "CREATE TABLE IF NOT EXISTS modCases (caseID VARCHAR(6) PRIMARY KEY, guildID BIGINT NOT NULL, targetID BIGINT NOT NULL, moderatorID BIGINT NOT NULL, caseType VARCHAR(20) NOT NULL, reason VARCHAR(512) NOT NULL, duration BIGINT NOT NULL, creationDate BIGINT NOT NULL, active BOOLEAN NOT NULL)",
                                "CREATE TABLE IF NOT EXISTS levels (guildID BIGINT, userID BIGINT, totalXP BIGINT)"
                                };
        executeAllCommands(commands);
    }

    public static void executeAllCommands(String[] queries)
    {
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return;
        try
        {
            for(String command : queries)
            {
                PreparedStatement ps = connection.prepareStatement(command);
                ps.execute();
                ps.close();
            }
        }catch (Exception e)
        {
            LOGGER.error("Could not run command", e);
        }finally
        {
            Util.closeQuietly(connection);
        }
    }
}
