package at.xirado.bean.data;

import net.dv8tion.jda.api.interactions.commands.Command;

public class SearchEntry
{
    public static final String MAGNIFYING_GLASS = "\uD83D\uDD0E";
    public static final String SCROLL = "\uD83D\uDCDC"; // indicates that this is a playlist

    private final String name; // The text the user entered (or a videos title if direct link)
    private final String value; // url or search term
    private final boolean playlist; // if this is a playlist or not

    public SearchEntry(String name, String value, boolean playlist)
    {
        this.name = name;
        this.value = value;
        this.playlist = playlist;
    }

    public boolean isPlaylist()
    {
        return playlist;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public String getFormattedString()
    {
        return MAGNIFYING_GLASS+(playlist ? SCROLL : "")+" "+name;
    }

    public Command.Choice toCommandAutocompleteChoice()
    {
        return new Command.Choice(getFormattedString(), value);
    }
}
