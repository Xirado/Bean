package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

public class EvalUtil {
    public static String get(String route) {
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        RestAction<DataObject> restAction = new RestActionImpl<>(jda, Route.get(route).compile(), (response, request) -> response.getObject());
        DataObject result = restAction.complete();
        return "```json\n" + result.toPrettyString() + "\n```";
    }

    public static String getArray(String route) {
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        RestAction<DataArray> restAction = new RestActionImpl<>(jda, Route.get(route).compile(), (response, request) -> response.getArray());
        DataArray result = restAction.complete();
        return "```json\n" + result.toPrettyString() + "\n```";
    }

    public static <T> String parseToString(T x) {
        if (x instanceof CharSequence charSequence)
            return "'" + charSequence + "'";
        if (x instanceof Number number)
            return String.valueOf(number);
        return String.valueOf(x);
    }
}
