package at.Xirado.Bean.Language;

public class FormattedDuration
{
    private long epochTime;
    private String format;

    public FormattedDuration(long epochTime, String format)
    {
        if(epochTime < 0) throw new IllegalArgumentException("Duration is smaller than 0");
        this.epochTime = epochTime;
        this.format = format;
    }
    public String toString()
    {
        long minutes;
        long hours;
        long days;
        long weeks;
        long months;
        long years;
        long seconds = this.epochTime/1000;
        return null;

    }
}
