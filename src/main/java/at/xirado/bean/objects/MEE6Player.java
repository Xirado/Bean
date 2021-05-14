package at.xirado.bean.objects;

public class MEE6Player
{
    private String avatar;
    private long[] detailed_xp;
    private String discriminator;
    private String guild_id;
    private String id;
    private int level;
    private int message_count;
    private String username;
    private long xp;


    public void setXp(long xp)
    {
        this.xp = xp;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setMessage_count(int message_count)
    {
        this.message_count = message_count;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setGuild_id(String guild_id)
    {
        this.guild_id = guild_id;
    }

    public void setDiscriminator(String discriminator)
    {
        this.discriminator = discriminator;
    }

    public void setDetailed_xp(long[] detailed_xp)
    {
        this.detailed_xp = detailed_xp;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public long getXp()
    {
        return xp;
    }

    public String getUsername()
    {
        return username;
    }

    public int getMessage_count()
    {
        return message_count;
    }

    public int getLevel()
    {
        return level;
    }

    public String getId()
    {
        return id;
    }

    public String getGuild_id()
    {
        return guild_id;
    }

    public String getDiscriminator()
    {
        return discriminator;
    }

    public long[] getDetailed_xp()
    {
        return detailed_xp;
    }

    public String getAvatar()
    {
        return avatar;
    }
}
