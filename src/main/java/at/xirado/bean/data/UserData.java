package at.xirado.bean.data;

import net.dv8tion.jda.api.entities.User;

public class UserData
{
    private final User user;

    private UserData(User user)
    {
        this.user = user;
    }

    public static UserData getUserData(User user)
    {
        return new UserData(user);
    }
}
