package at.xirado.bean.backend.requests;

import at.xirado.bean.Bean;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class Request
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    private final okhttp3.Request request;
    private final OkHttpClient client;

    public Request(okhttp3.Request request, OkHttpClient client)
    {
        this.request = request;
        this.client = client;
    }

    public Response complete() throws IOException
    {
        return client.newCall(request).execute();
    }

    public void queue(Consumer<Response> success, Consumer<Exception> failure)
    {
        Bean.getInstance().getExecutor().execute(() -> {
            try
            {
                Response response = client.newCall(request).execute();
                success.accept(response);
                response.close();
            } catch (Exception ex)
            {
                failure.accept(ex);
            }
        });
    }

    public void queue(Consumer<Response> success)
    {
        Bean.getInstance().getExecutor().execute(() -> {
            try
            {
                Response response = client.newCall(request).execute();
                success.accept(response);
                response.close();
            } catch (Exception ex)
            {
                LOGGER.error("Error occurred while executing Request!", ex);
            }
        });
    }
}
