package at.Xirado.Bean.Misc;

public class Tempban {
    private long guildid;
    private long userid;
    private long deadline;

    public Tempban(long guildid, long userid, long deadline)
    {
        this.guildid = guildid;
        this.userid = userid;
        this.deadline = deadline;
    }

    public long getGuildid() {
        return guildid;
    }

    public void setGuildid(long guildid) {
        this.guildid = guildid;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }
}
