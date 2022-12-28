package at.xirado.bean.misc;

public class FormatUtil {
    public static String formatTime(final long duration) {
        if (duration == Long.MAX_VALUE) {
            return "LIVE";
        }
        long seconds = Math.round(duration / 1000.0);
        final long hours = seconds / 3600L;
        seconds %= 3600L;
        final long minutes = seconds / 60L;
        seconds %= 60L;
        return ((hours > 0L) ? (hours + ":") : "") + ((minutes < 10L) ? ("0" + minutes) : Long.valueOf(minutes)) + ":" + ((seconds < 10L) ? ("0" + seconds) : Long.valueOf(seconds));
    }

    public static String filter(final String input) {
        return input.replace("\u202e", "").replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim();
    }
}
