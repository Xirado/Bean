package at.xirado.bean.data;

public class RankedUser {

    private final long totalXP;
    private final long guildID;
    private final long userID;

    private final String name;
    private final String discriminator;

    public RankedUser(long guildID, long userID, long totalXP, String name, String discriminator) {
        this.guildID = guildID;
        this.userID = userID;
        this.totalXP = totalXP;
        this.name = name;
        this.discriminator = discriminator;
    }

    @Override
    public String toString() {
        return userID + " (" + name + "#" + discriminator + ") " + totalXP;
    }

    public long getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public long getTotalXP() {
        return totalXP;
    }

    public long getGuildID() {
        return guildID;
    }
}
