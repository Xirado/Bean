package at.xirado.bean.data;

import at.xirado.bean.data.database.SQLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Hints {

    /**
     * Only local hint cache
     * If the member sees the hint, but doesn't click the "Don't show this again" button,
     * it will not show again until the bot is restarted.
     */
    private static final Map<Long, List<String>> LOCAL_HINTS = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Hints.class);


    public static void sentUserHint(long userId, String hint) {
        silentAcknowledge(userId, hint);
    }

    public static boolean hasAcknowledged(long userId, String hint) {
        if (hasSeen(userId, hint))
            return true;
        silentAcknowledge(userId, hint);
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM acknowledged_hints WHERE user_id = ? AND hint = ?", userId, hint).executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.warn("Could not check if user {} acknowledged hint {}", userId, hint);
            return true;
        }
    }

    public static boolean hasPressedAckButton(long userId, String hint) {
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM acknowledged_hints WHERE user_id = ? AND hint = ?", userId, hint).executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.warn("Could not check if user {} acknowledged hint {}", userId, hint);
            return true;
        }
    }

    private static void silentAcknowledge(long userId, String hint) {
        List<String> hints = LOCAL_HINTS.containsKey(userId) ? LOCAL_HINTS.get(userId) : new ArrayList<>();
        if (!hints.contains(hint))
            hints.add(hint);
        LOCAL_HINTS.put(userId, hints);
    }

    private static boolean hasSeen(long userId, String hint) {
        if (!LOCAL_HINTS.containsKey(userId))
            return false;
        List<String> hints = LOCAL_HINTS.get(userId);
        return hints.contains(hint);
    }

    public static void acknowledgeHint(long userId, String hint) {
        if (hasPressedAckButton(userId, hint)) return;
        try {
            new SQLBuilder("INSERT INTO acknowledged_hints (user_id, hint) values (?,?)", userId, hint)
                    .execute();
        } catch (SQLException ex) {
            LOGGER.warn("Could not acknowledge hint {} for user {}", hint, userId);
        }
    }
}
