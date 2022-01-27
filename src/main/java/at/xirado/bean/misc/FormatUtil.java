package at.xirado.bean.misc;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;

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

    public static String progressBar(final double percent) {
        String str = "";
        for (int i = 0; i < 12; ++i) {
            if (i == (int) (percent * 12.0)) {
                str += "\ud83d\udd18";
            } else {
                str += "\u25ac";
            }
        }
        return str;
    }

    public static String volumeIcon(final int volume) {
        if (volume == 0) {
            return "\ud83d\udd07";
        }
        if (volume < 30) {
            return "\ud83d\udd08";
        }
        if (volume < 70) {
            return "\ud83d\udd09";
        }
        return "\ud83d\udd0a";
    }

    public static String listOfTChannels(final List<TextChannel> list, final String query) {
        String out = " Multiple text channels found matching \"" + query + "\":";
        for (int i = 0; i < 6 && i < list.size(); ++i) {
            out = out + "\n - " + list.get(i).getName() + " (<#" + list.get(i).getId() + ">)";
        }
        if (list.size() > 6) {
            out = out + "\n**And " + (list.size() - 6) + " more...**";
        }
        return out;
    }

    public static String listOfVChannels(final List<VoiceChannel> list, final String query) {
        String out = " Multiple voice channels found matching \"" + query + "\":";
        for (int i = 0; i < 6 && i < list.size(); ++i) {
            out = out + "\n - " + list.get(i).getName() + " (ID:" + list.get(i).getId() + ")";
        }
        if (list.size() > 6) {
            out = out + "\n**And " + (list.size() - 6) + " more...**";
        }
        return out;
    }

    public static String listOfRoles(final List<Role> list, final String query) {
        String out = " Multiple text channels found matching \"" + query + "\":";
        for (int i = 0; i < 6 && i < list.size(); ++i) {
            out = out + "\n - " + list.get(i).getName() + " (ID:" + list.get(i).getId() + ")";
        }
        if (list.size() > 6) {
            out = out + "\n**And " + (list.size() - 6) + " more...**";
        }
        return out;
    }

    public static String filter(final String input) {
        return input.replace("\u202e", "").replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim();
    }
}
