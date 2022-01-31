package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import at.xirado.bean.interactions.ButtonPaginator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

import java.util.concurrent.TimeUnit;

public class EvalUtil
{
    public static String get(String route)
    {
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        RestAction<DataObject> restAction = new RestActionImpl<>(jda, Route.get(route).compile(), (response, request) -> response.getObject());
        DataObject result = restAction.complete();
        return "```json\n" + result.toPrettyString() + "\n```";
    }

    public static String getArray(String route)
    {
        JDA jda = Bean.getInstance().getShardManager().getShards().get(0);
        RestAction<DataArray> restAction = new RestActionImpl<>(jda, Route.get(route).compile(), (response, request) -> response.getArray());
        DataArray result = restAction.complete();
        return "```json\n" + result.toPrettyString() + "\n```";
    }

    public static void testPaginator(TextChannel channel)
    {
        ButtonPaginator.Builder builder = new ButtonPaginator.Builder(channel.getJDA())
                .useNumberedItems(true)
                .setItemsPerPage(10)
                .setTimeout(1, TimeUnit.MINUTES)
                .setTitle("Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam")
                .setColor(EmbedUtil.DEFAULT_COLOR)
                .setEventWaiter(Bean.getInstance().getEventWaiter())
                .addAllowedUsers(Bean.WHITELISTED_USERS.toArray(new Long[0]))
                .setFooter("Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam")
                .setItems(new String[]{"Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam", "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam", "erat sed diam voluptua. At vero eos et accusam et", "justo duo dolores et ea rebum. Stet clita kasd gubergren", "no sea takimata sanctus est Lorem ipsum dolor sit amet.", "Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam", "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam", "erat sed diam voluptua. At vero eos et accusam et", "justo duo dolores et ea rebum. Stet clita kasd gubergren", "no sea takimata sanctus est Lorem ipsum dolor sit amet.", "Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam", "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam", "erat sed diam voluptua. At vero eos et accusam et", "justo duo dolores et ea rebum. Stet clita kasd gubergren", "no sea takimata sanctus est Lorem ipsum dolor sit amet.", "Duis autem vel eum iriure dolor in hendrerit in vulputate", "velit esse molestie consequat vel illum dolore eu feugiat nulla", "facilisis at vero eros et accumsan et iusto odio dignissim", "qui blandit praesent luptatum zzril delenit augue duis dolore te", "feugait nulla facilisi. Lorem ipsum dolor sit amet consectetuer adipiscing", "elit sed diam nonummy nibh euismod tincidunt ut laoreet dolore", "magna aliquam erat volutpat. Ut wisi enim ad minim veniam", "quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip", "ex ea commodo consequat. Duis autem vel eum iriure dolor", "in hendrerit in vulputate velit esse molestie consequat vel illum", "dolore eu feugiat nulla facilisis at vero eros et accumsan", "et iusto odio dignissim qui blandit praesent luptatum zzril delenit", "augue duis dolore te feugait nulla facilisi. Nam liber tempor", "cum soluta nobis eleifend option congue nihil imperdiet doming id", "quod mazim placerat facer possim assum. Lorem ipsum dolor sit", "amet consectetuer adipiscing elit sed diam nonummy nibh euismod tincidunt", "ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim", "ad minim veniam quis nostrud exerci tation ullamcorper suscipit lobortis", "nisl ut aliquip ex ea commodo consequat. Duis autem vel", "eum iriure dolor in hendrerit in vulputate velit esse molestie", "consequat vel illum dolore eu feugiat nulla facilisis. At vero", "eos et accusam et justo duo dolores et ea rebum.", "Stet clita kasd gubergren no sea takimata sanctus est Lorem", "ipsum dolor sit amet. Lorem ipsum dolor sit amet consetetur", "sadipscing elitr sed diam nonumy eirmod tempor invidunt ut labore", "et dolore magna aliquyam erat sed diam voluptua. At vero", "eos et accusam et justo duo dolores et ea rebum.", "Stet clita kasd gubergren no sea takimata sanctus est Lorem", "ipsum dolor sit amet. Lorem ipsum dolor sit amet consetetur", "sadipscing elitr At accusam aliquyam diam diam dolore dolores duo", "eirmod eos erat et nonumy sed tempor et et invidunt", "justo labore Stet clita ea et gubergren kasd magna no", "rebum. sanctus sea sed takimata ut vero voluptua. est Lorem", "ipsum dolor sit amet. Lorem ipsum dolor sit amet consetetur", "sadipscing elitr sed diam nonumy eirmod tempor invidunt ut labore", "et dolore magna aliquyam erat. Consetetur sadipscing elitr sed diam", "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam", "erat sed diam voluptua. At vero eos et accusam et", "justo duo dolores et ea rebum. Stet clita kasd gubergren", "no sea takimata sanctus est Lorem ipsum dolor sit amet.", "Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam", "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam", "erat sed diam voluptua. At vero eos et accusam et", "justo duo dolores et ea rebum. Stet clita kasd gubergren", "no sea takimata sanctus est Lorem ipsum dolor sit amet.", "Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam", "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam", "erat sed diam voluptua. At vero eos et accusam et", "justo duo dolores et ea rebum. Stet clita kasd gubergren", "no sea takimata sanctus. Lorem ipsum dolor sit amet consetetur", "sadipscing elitr sed diam nonumy eirmod tempor invidunt ut labore", "et dolore magna aliquyam erat sed diam voluptua. At vero", "eos et accusam et justo duo dolores et ea rebum.", "Stet clita kasd gubergren no sea takimata sanctus est Lorem", "ipsum dolor sit amet. Lorem ipsum dolor sit amet consetetur", "sadipscing elitr sed diam nonumy eirmod tempor invidunt ut labore", "et dolore magna aliquyam erat sed diam voluptua. At vero", "eos et accusam et justo duo dolores et ea rebum.", "Stet clita kasd gubergren no sea takimata sanctus est Lorem", "ipsum dolor sit amet. Lorem ipsum dolor sit amet consetetur", "sadipscing elitr sed diam nonumy eirmod tempor invidunt ut labore", "et dolore magna aliquyam erat sed diam voluptua. At vero", "eos et accusam et justo duo dolores et ea rebum.", "Stet clita kasd gubergren no sea takimata sanctus est Lorem", "ipsum dolor sit amet. Duis autem vel eum iriure dolor", "in hendrerit in vulputate velit esse molestie consequat vel illum", "dolore eu feugiat nulla facilisis at vero eros et accumsan", "et iusto odio dignissim qui blandit praesent luptatum zzril delenit", "augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor", "sit amet consectetuer adipiscing elit sed diam nonummy nibh euismod", "tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi", "enim ad minim veniam quis nostrud exerci tation ullamcorper suscipit", "lobortis nisl ut aliquip ex ea commodo consequat. Duis autem", "vel eum iriure dolor in hendrerit in vulputate velit esse", "molestie consequat vel illum dolore eu feugiat nulla facilisis at", "vero eros et accumsan et iusto odio dignissim qui blandit", "praesent luptatum zzril delenit augue duis dolore te feugait nulla", "facilisi. Nam liber tempor cum soluta nobis eleifend option congue", "nihil imperdiet doming id quod mazim placerat facer possim assum.", "Lorem ipsum dolor sit amet consectetuer adipiscing elit sed diam", "nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat", "volutpat. Ut wisi enim ad minim veniam quis nostrud exerci", "tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo",});
        builder.build().paginate(channel.sendMessage("placeholder"), 1);
    }

    public static <T> String parseToString(T x)
    {
        if (x instanceof CharSequence charSequence)
            return "'" + charSequence + "'";
        if (x instanceof Number number)
            return String.valueOf(number);
        return String.valueOf(x);
    }
}
