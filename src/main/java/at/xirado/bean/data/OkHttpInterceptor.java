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

package at.xirado.bean.data;

import at.xirado.bean.misc.Metrics;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OkHttpInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        long duration = response.receivedResponseAtMillis() - response.sentRequestAtMillis();

        Metrics.DISCORD_API_REQUESTS.labels(getLabel(response.code())).inc();
        Metrics.DISCORD_REST_PING.set(duration);

        return response;
    }

    private String getLabel(int code) {
        if (code < 400)
            return "Ok";
        else if (code < 500)
            return "Client";
        else
            return "Server";
    }
}
