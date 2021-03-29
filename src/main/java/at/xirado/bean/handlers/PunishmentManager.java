package at.xirado.bean.handlers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PunishmentManager
{

    private static final Logger logger = LoggerFactory.getLogger(PunishmentManager.class);

    public PunishmentManager()
    {

    }

    public void banPermanently(User target, Guild guild, Member moderator, String reason)
    {
        if(guild.getIdLong() != moderator.getGuild().getIdLong())
        {
            logger.error("Member not from this guild!", new IllegalArgumentException());
            return;
        }

    }


}
