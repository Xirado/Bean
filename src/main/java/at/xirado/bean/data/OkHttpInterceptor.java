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

        Metrics.DISCORD_API_REQUESTS.labels(getLabel(response.code())).inc();

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
