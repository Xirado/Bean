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

    public static String getDuration(long seconds)
    {
        if(seconds < 10) return "now";
        if(seconds < 60) return "less than a minute ago";
        if(seconds < 3600) // less than a hour
        {
            int minutes = (int) (seconds/60);
            if(minutes == 1) return "a minute ago";
            return minutes+" minutes ago";
        }
        if(seconds < 86400) // less than a day
        {
            int hours = (int) (seconds/60)/60;
            if(hours == 1) return "a hour ago";
            return hours+" hours ago";
        }
        if(seconds < 604800) // less than a week
        {
            int days = (int) ((seconds/60)/60)/24;
            if(days == 1) return "a day ago";
            return days+" days ago";
        }
        if(seconds < 2.628e+6) // less than a month
        {
            int weeks = (int) (((seconds/60)/60)/24)/7;
            if(weeks == 1) return "a week ago";
            return weeks+" weeks ago";
        }
        if(seconds < 3.154e+7) // less than a year
        {
            int months = (int) (((seconds/60)/60)/24)/30;
            if(months == 1) return "a month ago";
            return months+" months ago";
        }
        int years = (int) ((((seconds/60)/60)/24)/30)/365;
        if(years == 1) return "a year ago";
        return years+" years ago";

    }
}
