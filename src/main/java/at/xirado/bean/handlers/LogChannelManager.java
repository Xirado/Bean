package at.xirado.bean.handlers;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.SQL;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class LogChannelManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LogChannelManager.class);

    public ConcurrentHashMap<Long, Long> logChannel;

    public LogChannelManager()
    {
        this.logChannel = new ConcurrentHashMap<>();
    }

    public void setLogChannel(long guildID, long channelid)
    {
        Connection connection = SQL.getConnectionFromPool();
        if(connection == null)
        {
            LOGGER.error("Could not set logchannel!", new Exception());
            return;
        }
        try(var ps = connection.prepareStatement("INSERT INTO logChannels (guildID,channelID) values (?,?) ON DUPLICATE KEY UPDATE channelID = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, channelid);
            ps.setLong(3, channelid);
            ps.execute();
            if(this.logChannel.containsKey(guildID))
            {
                this.logChannel.remove(guildID);
                this.logChannel.put(guildID, channelid);
            }
        } catch (SQLException throwables) {
            LOGGER.error("Could not set Log Channel!", throwables);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }
    public TextChannel getLogChannel(long guildid)
    {
        TextChannel channel = null;

        if(this.logChannel.containsKey(guildid))
        {
            channel = Bean.instance.jda.getTextChannelById(this.logChannel.get(guildid));
        }else
        {
            Connection connection = SQL.getConnectionFromPool();
            if(connection == null)
            {
                LOGGER.error("Could not get logchannel!", new Exception());
                return null;
            }
            try (PreparedStatement ps = connection.prepareStatement("SELECT channelID FROM logChannels WHERE guildID = ?"))
            {
                ps.setLong(1, guildid);
                ResultSet rs = ps.executeQuery();
                if(rs.next())
                {
                    channel = Bean.instance.jda.getTextChannelById(rs.getLong("channelID"));
                    this.logChannel.put(guildid, channel.getIdLong());
                }
            } catch (SQLException throwables) {
                LOGGER.error("Could not get logchannel!", throwables);
            } finally
            {
                Util.closeQuietly(connection);
            }
        }
        return channel;
    }
}
