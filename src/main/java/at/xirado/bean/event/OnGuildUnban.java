package at.xirado.bean.event;

import at.xirado.bean.data.database.Database;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.moderation.CaseType;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OnGuildUnban extends ListenerAdapter
{
    private static final Logger logger = LoggerFactory.getLogger(OnGuildUnban.class);

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event)
    {
        long userId = event.getUser().getIdLong();
        long guildId = event.getGuild().getIdLong();
        String sql = "UPDATE modCases SET active = 0 WHERE (caseType = ? OR caseType = ?) AND user = ? AND guild = ? AND active = 1";
        var query = new SQLBuilder(sql)
                .addParameters(CaseType.BAN.getId(), CaseType.TEMPBAN.getId(), userId, guildId);
        try
        {
            query.execute();
        }
        catch (SQLException ex)
        {
            logger.error("Could not remove ban from database!", ex);
        }
    }
}