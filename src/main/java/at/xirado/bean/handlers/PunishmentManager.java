package at.xirado.bean.handlers;

import at.xirado.bean.logging.Console;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class PunishmentManager
{
    public PunishmentManager()
    {

    }

    public void banPermanently(User target, Guild guild, Member moderator, String reason)
    {
        if(guild.getIdLong() != moderator.getGuild().getIdLong())
        {
            Console.logger.error("Member not from this guild!", new IllegalArgumentException());
            return;
        }

    }


}
