package at.xirado.bean.misc.objects;

import java.util.HashSet;
import java.util.Set;

public class TrackInfo
{
    private long requester;
    private Set<Long> voteSkips;


    public TrackInfo(long requester)
    {
        this.requester = requester;
    }


    public long getRequesterIdLong()
    {
        return requester;
    }


    public TrackInfo addVoteSkip(long userId)
    {
        if (voteSkips == null)
        {
            this.voteSkips = new HashSet<>();
            this.voteSkips.add(userId);
            return this;
        }
        this.voteSkips.add(userId);
        return this;
    }

    public TrackInfo removeVoteSkip(long userId)
    {
        if (voteSkips == null)
            return this;
        this.voteSkips.remove(userId);
        return this;
    }

    public Set<Long> getVoteSkips()
    {
        return voteSkips == null ? new HashSet<>() : voteSkips;
    }
}
