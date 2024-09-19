/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;

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
