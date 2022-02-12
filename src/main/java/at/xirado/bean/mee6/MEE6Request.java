package at.xirado.bean.mee6;

import org.jetbrains.annotations.NotNull;

public class MEE6Request implements Comparable<MEE6Request>
{
    private final long guildId;
    private final long authorId;

    private int page = 1;

    public MEE6Request(long guildId, long authorId)
    {
        this.guildId = guildId;
        this.authorId = authorId;
    }

    public MEE6Request setPage(int page)
    {
        this.page = page;
        return this;
    }

    public long getAuthorId()
    {
        return authorId;
    }

    public int getPage()
    {
        return page;
    }

    public long getGuildId()
    {
        return guildId;
    }

    @Override
    public int compareTo(@NotNull MEE6Request o)
    {
        if (this.page == o.page)
            return 0;
        else if (this.page > o.page)
            return 1;
        else
            return -1;
    }
}
