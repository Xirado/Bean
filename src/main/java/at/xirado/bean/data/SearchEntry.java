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
        this.value = value.length() > 100 ? value.substring(0, 100) : value;
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
        String x = MAGNIFYING_GLASS+(playlist ? SCROLL : "")+" "+name;
        if (x.length() > 100)
        {
            String replaceString = "...";
            String substr = x.substring(0, 100 - replaceString.length());
            x = substr + replaceString;
        }
        return x;
    }

    public Command.Choice toCommandAutocompleteChoice()
    {
        return new Command.Choice(getFormattedString(), value);
    }
}
