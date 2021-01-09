package at.Xirado.Bean.Handlers;

import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Misc.SQL;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class LogChannelManager
{
    public ConcurrentHashMap<Long, Long> logChannel;

    public LogChannelManager()
    {
        this.logChannel = new ConcurrentHashMap<>();
    }

    public void setLogChannel(long guildID, long channelid)
    {
        try {
            PreparedStatement ps = SQL.con.prepareStatement("INSERT INTO logChannels (guildID,channelID) values (?,?) ON DUPLICATE KEY UPDATE channelID = ?");
            ps.setLong(1, guildID);
            ps.setLong(2, channelid);
            ps.setLong(3,channelid);
            ps.execute();
            if(this.logChannel.containsKey(guildID))
            {
                this.logChannel.remove(guildID);
                this.logChannel.put(guildID, channelid);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public TextChannel getLogChannel(long guildid)
    {
        TextChannel channel = null;

        if(this.logChannel.containsKey(guildid))
        {
            channel = DiscordBot.instance.jda.getTextChannelById(this.logChannel.get(guildid));
        }else
        {
            try {
                PreparedStatement ps = SQL.con.prepareStatement("SELECT channelID FROM logChannels WHERE guildID = ?");
                ps.setLong(1, guildid);
                ResultSet rs = ps.executeQuery();
                if(rs.next())
                {
                    channel = DiscordBot.instance.jda.getTextChannelById(rs.getLong("channelID"));
                    this.logChannel.put(guildid, channel.getIdLong());
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
        return channel;
    }
}
