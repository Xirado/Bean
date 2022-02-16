package at.xirado.bean.exception;

import net.dv8tion.jda.api.entities.User;

public class LocalizableDiscordException extends RuntimeException
{
    public LocalizableDiscordException(User user, String message)
    {

    }
}
