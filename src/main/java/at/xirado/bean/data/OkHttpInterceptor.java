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
