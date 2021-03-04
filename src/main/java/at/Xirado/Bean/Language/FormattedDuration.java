package at.Xirado.Bean.Language;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedDuration
{
    public static final long ONE_DAY = 86400000L;
    public static final long ONE_WEEK = ONE_DAY*7;

    private static final Pattern periodPattern = Pattern.compile("([0-9]+)([a-z]+)");

    public static Long parsePeriod(String period){
        if(period == null) return null;
        period = period.toLowerCase(Locale.ENGLISH);
        Matcher matcher = periodPattern.matcher(period);
        Instant instant=Instant.EPOCH;
        while(matcher.find()){
            int num = Integer.parseInt(matcher.group(1));
            String typ = matcher.group(2);
            switch (typ) {
                case "seconds":
                case "sec":
                case "s":
                    instant=instant.plus(Duration.ofSeconds(num));
                    break;
                case "minutes":
                case "min":
                case "m":
                    instant=instant.plus(Duration.ofMinutes(num));
                    break;
                case "hr":
                case "hour":
                case "hours":
                case "h":
                    instant=instant.plus(Duration.ofHours(num));
                    break;
                case "day":
                case "days":
                case "d":
                    instant=instant.plus(Duration.ofDays(num));
                    break;
                case "w":
                    instant=instant.plus(Period.ofWeeks(num));
                    break;
                case "month":
                case "mon":
                    instant=instant.plus(Period.ofMonths(num));
                    break;
                case "year":
                case "yr":
                case "y":
                    instant=instant.plus(Period.ofYears(num));
                    break;
                default:
                    return null;
            }
        }
        return instant.toEpochMilli();
    }

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

    public static String getDuration(long seconds, boolean calcDifference)
    {
        if(calcDifference) seconds = (System.currentTimeMillis()/1000)-seconds;
        if(seconds < 0) throw new IllegalArgumentException("Negative Duration Value!");
        if(seconds < 10) return "now";
        if(seconds < 60) return "less than a minute ago";
        if(seconds < 3600) // less than an hour
        {
            int minutes = (int) (seconds/60);
            if(minutes == 1) return "a minute ago";
            return minutes+" minutes ago";
        }
        if(seconds < 86400) // less than a day
        {
            int hours = (int) (seconds/60)/60;
            if(hours == 1) return "an hour ago";
            return hours+" hours ago";
        }
        if(seconds < 604800) // less than a week
        {
            int days = (int) ((seconds/60)/60)/24;
            if(days == 1) return "yesterday";
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
        int years = (int) ((seconds/3600)/24)/365;
        if(years == 1) return "a year ago";
        return years+" years ago";

    }
}
