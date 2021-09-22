package at.xirado.bean.event;

import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.moderation.CaseType;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class OnGuildUnban extends ListenerAdapter
{
    private static final Logger logger = LoggerFactory.getLogger(OnGuildUnban.class);

}
