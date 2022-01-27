package at.xirado.bean.log;

import javax.annotation.Nonnull;

public class ConsoleUtils {

    public static String error(@Nonnull String text) {
        return MCColor.translate("&8[&cError&8] &c" + text);
    }

    public static String warn(@Nonnull String text) {
        return MCColor.translate("&8[&eWarning&8] &e" + text);
    }
}
